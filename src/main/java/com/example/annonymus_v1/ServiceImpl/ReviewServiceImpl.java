package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.entity.Institute;
import com.example.annonymus_v1.entity.Review;
import com.example.annonymus_v1.entity.UsersIp;
import com.example.annonymus_v1.exception.BaseTranslatableRuntimeException;
import com.example.annonymus_v1.mapper.ReviewMapper;
import com.example.annonymus_v1.repository.InstituteRepository;
import com.example.annonymus_v1.repository.ReviewRepository;
import com.example.annonymus_v1.repository.UserIpRepository;
import com.example.annonymus_v1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final InstituteRepository instituteRepository;
    private final UserIpRepository userIpRepository;

    @Override
    public Page<ReviewDto> getAllReviews(String searchParam,int pageNumber,int PageSize) {

        Pageable pageable = PageRequest.of(pageNumber,PageSize);

        Page<ReviewDto> reviewList =  reviewRepository.findAllByDeletedIsFalse(searchParam,pageable);

        if(!reviewList.isEmpty()) {
            return reviewList;
        }
        else {
            throw new BaseTranslatableRuntimeException("no.reviews.found",
                    "No reviews found",
                    new Object[] {searchParam});
        }
    }

    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        if(reviewDto.getInstituteId() == null) {
            throw new BaseTranslatableRuntimeException(
                    "institute.id.missing",
                    "University name is required",
                    null);
        }
        if(reviewDto.getTitle() == null || reviewDto.getTitle().isEmpty()) {
            throw new BaseTranslatableRuntimeException(
                    "review.text.missing",
                    "Review Title is required",
                    null);
        }
        if(reviewDto.getDescription() == null || reviewDto.getDescription().isEmpty()) {
            throw new BaseTranslatableRuntimeException(
                    "review.description.missing",
                    "Review description is required",
                    null);
        }
        if(reviewDto.getReviewType() == null) {
            throw new BaseTranslatableRuntimeException(
                    "review.type.missing",
                    "Review type is required",
                    null);
        }

        Review reviewEntity = ReviewMapper.toEntity(reviewDto);

        if(reviewEntity.getLikeCount() == null) {
            reviewEntity.setLikeCount(0L);
        }
        else if(reviewEntity.getDislikeCount() == null) {
            reviewEntity.setDislikeCount(0L);
        }
        reviewEntity = reviewRepository.save(reviewEntity);
        return ReviewMapper.toDto(reviewEntity);
    }

    @Override
    public ReviewDto getReviewById(UUID id) {
        Optional<ReviewDto> reviewDtoOptional = reviewRepository.getReviewDetailsById(id);
        if (reviewDtoOptional.isEmpty()) {
            throw new BaseTranslatableRuntimeException(
                    "review.not.found",
                    "Review with ID %s not found".formatted(id),
                    new Object[] {id}
            );
        }
        return reviewDtoOptional.get();
    }

    @Override
    public void deleteReviewById(UUID id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            review.get().setDeleted(true);
            reviewRepository.save(review.get());
        }
        else {
            throw new BaseTranslatableRuntimeException(
                    "review.not.found",
                    "Review with ID %s not found".formatted(id),
                    new Object[] {id}
            );
        }
    }

    @Override
    public ReviewDto likeReview(UUID id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            Review reviewEntity = review.get();
            reviewEntity.setLikeCount(reviewEntity.getLikeCount() + 1);
            reviewRepository.save(reviewEntity);
            return ReviewMapper.toDto(reviewEntity);
        }
        else {
            throw new BaseTranslatableRuntimeException(
                    "review.not.found",
                    "Review with ID %s not found".formatted(id),
                    new Object[] {id}
            );
        }
    }

    @Override
    public ReviewDto dislikeReview(UUID id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            Review reviewEntity = review.get();
            reviewEntity.setDislikeCount(reviewEntity.getDislikeCount() + 1);
            reviewRepository.save(reviewEntity);
            return ReviewMapper.toDto(reviewEntity);
        } else {
            throw new BaseTranslatableRuntimeException(
                    "review.not.found",
                    "Review with ID %s not found".formatted(id),
                    new Object[] {id}
            );
        }
    }

    @Override
    public boolean isLikedBefore(UUID id, String clientIdentifier,Boolean likeOrDislike) {
        Optional<UsersIp> isExists = userIpRepository.findByReviewIdAndUserIp(id, clientIdentifier);
        if (isExists.isPresent() && isExists.get().getLike().equals(likeOrDislike)) {
            return true;
        }
        UsersIp usersIp = UsersIp.builder()
                .reviewId(id)
                .userIp(clientIdentifier)
                .like(likeOrDislike)
                .build();
        userIpRepository.save(usersIp);
        return false;
    }
}
