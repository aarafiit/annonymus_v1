package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.dto.VoteResponseDto;
import com.example.annonymus_v1.entity.Review;
import com.example.annonymus_v1.entity.UsersIp;
import com.example.annonymus_v1.enumurator.VoteType;
import com.example.annonymus_v1.exception.BaseTranslatableRuntimeException;
import com.example.annonymus_v1.mapper.ReviewMapper;
import com.example.annonymus_v1.repository.InstituteRepository;
import com.example.annonymus_v1.repository.ReviewRepository;
import com.example.annonymus_v1.repository.UserIpRepository;
import com.example.annonymus_v1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final InstituteRepository instituteRepository;
    private final UserIpRepository userIpRepository;

    /**
     * {@inheritDoc}
     *
     * <p>An empty result is returned as an empty page rather than thrown. A listing
     * with nothing in it is an ordinary answer - a filter that matches no entries yet,
     * or an institute nobody has written about - and answering 500 to it forced every
     * caller to treat "no rows" as a failure.
     */
    @Override
    public Page<ReviewDto> getAllReviews(String searchParam, Long instituteId, int pageNumber, int PageSize,
                                         String clientIdentifier) {

        Pageable pageable = PageRequest.of(pageNumber, PageSize);

        Page<ReviewDto> reviewList = reviewRepository.findAllByDeletedIsFalse(
                searchParam, instituteId == null ? 0L : instituteId, pageable);

        applyVotes(reviewList.getContent(), clientIdentifier);
        return reviewList;
    }

    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        if (reviewDto.getInstituteId() == null) {
            throw new BaseTranslatableRuntimeException(
                    "institute.id.missing",
                    "University name is required",
                    null);
        }
        if (reviewDto.getTitle() == null || reviewDto.getTitle().isEmpty()) {
            throw new BaseTranslatableRuntimeException(
                    "review.text.missing",
                    "Review Title is required",
                    null);
        }
        if (reviewDto.getDescription() == null || reviewDto.getDescription().isEmpty()) {
            throw new BaseTranslatableRuntimeException(
                    "review.description.missing",
                    "Review description is required",
                    null);
        }
        if (reviewDto.getReviewType() == null) {
            throw new BaseTranslatableRuntimeException(
                    "review.type.missing",
                    "Review type is required",
                    null);
        }

        Review reviewEntity = ReviewMapper.toEntity(reviewDto);

        if (reviewEntity.getLikeCount() == null) {
            reviewEntity.setLikeCount(0L);
        }
        if (reviewEntity.getDislikeCount() == null) {
            reviewEntity.setDislikeCount(0L);
        }
        reviewEntity = reviewRepository.save(reviewEntity);
        return ReviewMapper.toDto(reviewEntity);
    }

    @Override
    public ReviewDto getReviewById(UUID id, String clientIdentifier) {
        Optional<ReviewDto> reviewDtoOptional = reviewRepository.getReviewDetailsById(id);
        if (reviewDtoOptional.isEmpty()) {
            throw new BaseTranslatableRuntimeException(
                    "review.not.found",
                    "Review with ID %s not found".formatted(id),
                    new Object[]{id}
            );
        }
        ReviewDto reviewDto = reviewDtoOptional.get();
        applyVotes(List.of(reviewDto), clientIdentifier);
        return reviewDto;
    }

    @Override
    public void deleteReviewById(UUID id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            review.get().setDeleted(true);
            reviewRepository.save(review.get());
        } else {
            throw new BaseTranslatableRuntimeException(
                    "review.not.found",
                    "Review with ID %s not found".formatted(id),
                    new Object[]{id}
            );
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>One row per (review, reader) holds the whole state, so the three outcomes -
     * cast, switch, withdraw - are decided by comparing the incoming vote with the
     * stored one. Doing it this way rather than with two independent counters is
     * what makes "agree" and "disagree" mutually exclusive by construction: there is
     * nowhere to record both.
     *
     * <p>Transactional so the row and the two tallies it moves cannot disagree if
     * anything fails midway.
     */
    @Override
    @Transactional
    public VoteResponseDto vote(UUID reviewId, String clientIdentifier, VoteType voteType) {
        Review review = reviewRepository.findById(reviewId)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getDeleted()))
                .orElseThrow(() -> new BaseTranslatableRuntimeException(
                        "review.not.found",
                        "Review with ID %s not found".formatted(reviewId),
                        new Object[]{reviewId}));

        Optional<UsersIp> existing = userIpRepository.findByReviewIdAndUserIp(reviewId, clientIdentifier);
        VoteType current = existing.map(row -> VoteType.fromLikeFlag(row.getLike())).orElse(null);

        VoteType next;
        if (current == voteType) {
            // Pressing the button you already chose takes the vote back.
            userIpRepository.delete(existing.get());
            adjustTally(review, voteType, -1);
            next = null;
        } else {
            if (current != null) {
                // Moving from one side to the other: the old side must give up its count.
                adjustTally(review, current, -1);
                UsersIp row = existing.get();
                row.setLike(voteType.toLikeFlag());
                userIpRepository.save(row);
            } else {
                saveNewVote(reviewId, clientIdentifier, voteType);
            }
            adjustTally(review, voteType, 1);
            next = voteType;
        }

        reviewRepository.save(review);
        return new VoteResponseDto(reviewId, review.getLikeCount(), review.getDislikeCount(), next);
    }

    @Override
    public Map<UUID, VoteType> getVotes(Collection<UUID> reviewIds, String clientIdentifier) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }
        return userIpRepository.findByUserIpAndReviewIdIn(clientIdentifier, reviewIds).stream()
                .filter(row -> row.getLike() != null)
                .collect(Collectors.toMap(UsersIp::getReviewId, row -> VoteType.fromLikeFlag(row.getLike()),
                        (first, second) -> first));
    }

    /**
     * Fills each row's {@code userVote} for the caller in a single extra query.
     *
     * <p>Without this the client would have to remember its own votes, and a reader
     * returning on another day would see every button unselected while the server
     * still refused to count their vote again.
     */
    private void applyVotes(List<ReviewDto> reviews, String clientIdentifier) {
        if (clientIdentifier == null || reviews.isEmpty()) {
            return;
        }
        List<UUID> ids = reviews.stream().map(ReviewDto::getId).filter(java.util.Objects::nonNull).toList();
        Map<UUID, VoteType> votes = getVotes(ids, clientIdentifier);
        reviews.forEach(review -> review.setUserVote(votes.get(review.getId())));
    }

    /**
     * A vote is unique on (review, reader) in the database. Two clicks racing each
     * other therefore end with one insert and one violation rather than a double
     * count, and the loser simply re-reads the winner's row.
     */
    private void saveNewVote(UUID reviewId, String clientIdentifier, VoteType voteType) {
        try {
            userIpRepository.saveAndFlush(UsersIp.builder()
                    .reviewId(reviewId)
                    .userIp(clientIdentifier)
                    .like(voteType.toLikeFlag())
                    .build());
        } catch (DataIntegrityViolationException concurrentVote) {
            userIpRepository.findByReviewIdAndUserIp(reviewId, clientIdentifier)
                    .ifPresent(row -> {
                        row.setLike(voteType.toLikeFlag());
                        userIpRepository.save(row);
                    });
        }
    }

    /** Tallies never go below zero, however inconsistent the rows they were seeded from. */
    private void adjustTally(Review review, VoteType voteType, int delta) {
        if (voteType == VoteType.AGREE) {
            review.setLikeCount(Math.max(0L, orZero(review.getLikeCount()) + delta));
        } else {
            review.setDislikeCount(Math.max(0L, orZero(review.getDislikeCount()) + delta));
        }
    }

    private static long orZero(Long value) {
        return value == null ? 0L : value;
    }
}
