package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.entity.Review;
import com.example.annonymus_v1.exception.BaseTranslatableRuntimeException;
import com.example.annonymus_v1.mapper.ReviewMapper;
import com.example.annonymus_v1.repository.ReviewRepository;
import com.example.annonymus_v1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Cacheable(value = "reviews", key = "#searchParam + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ReviewDto> getAllReviews(String searchParam, Pageable pageable) {
        log.info("Fetching reviews from database with search param: {} and page: {}", searchParam, pageable.getPageNumber());
        Page<ReviewDto> reviewList = reviewRepository.findAllByDeletedIsFalse(searchParam, pageable);

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
    @CacheEvict(value = {"reviews", "analytics"}, allEntries = true)
    public ReviewDto createReview(ReviewDto reviewDto) {
        log.info("Creating new review and clearing cache");
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
    @Cacheable(value = "review", key = "#id")
    public ReviewDto getReviewById(UUID id) {
        log.info("Fetching review from database with id: {}", id);
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            return ReviewMapper.toDto(review.get());
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
    @CacheEvict(value = {"review", "reviews", "analytics"}, key = "#id")
    public void deleteReviewById(UUID id) {
        log.info("Deleting review with id: {} and clearing cache", id);
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
    @CachePut(value = "review", key = "#id")
    @CacheEvict(value = {"reviews", "analytics"}, allEntries = true)
    public ReviewDto likeReview(UUID id) {
        log.info("Liking review with id: {} and updating cache", id);
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
    @CachePut(value = "review", key = "#id")
    @CacheEvict(value = {"reviews", "analytics"}, allEntries = true)
    public ReviewDto dislikeReview(UUID id) {
        log.info("Disliking review with id: {} and updating cache", id);
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
}
