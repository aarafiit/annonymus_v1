package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.entity.Review;
import com.example.annonymus_v1.mapper.ReviewMapper;
import com.example.annonymus_v1.repository.ReviewRepository;
import com.example.annonymus_v1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Page<ReviewDto> getAllReviews(Pageable pageable) {

        Page<ReviewDto> reviewList =  reviewRepository.findAllByDeletedIsFalse(pageable);

        if(!reviewList.isEmpty()) {
            return reviewList;
        }
        else {
            throw new RuntimeException("No reviews found");
        }
    }

    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        Review reviewEntity = ReviewMapper.toEntity(reviewDto);
        reviewEntity = reviewRepository.save(reviewEntity);
        return ReviewMapper.toDto(reviewEntity);
    }

    @Override
    public ReviewDto getReviewById(UUID id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            return ReviewMapper.toDto(review.get());
        }
        else {
            throw new RuntimeException("Review not found");
        }
    }

    @Override
    public void deleteReviewById(UUID id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            review.get().setDeleted(true);
            reviewRepository.save(review.get());
        }
        else {
            throw new RuntimeException("Review not found");
        }
    }
}
