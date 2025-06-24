package com.example.annonymus_v1.service;

import com.example.annonymus_v1.dto.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    Page<ReviewDto> getAllReviews(String searchParam,Pageable pageable);

    ReviewDto createReview(ReviewDto reviewDto);

    ReviewDto getReviewById(UUID id);

    void deleteReviewById(UUID id);

    ReviewDto likeReview(UUID id);

    ReviewDto dislikeReview(UUID id);
}
