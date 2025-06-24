package com.example.annonymus_v1.mapper;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.entity.Review;

public class ReviewMapper {

    public static ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDto dto = new ReviewDto();
        dto.setDescription(review.getDescription());
        dto.setInstituteId(review.getInstituteId());
        dto.setTotalRatingSum(review.getTotalRatingSum());
        dto.setNumberOfRating(review.getNumberOfRating());
        dto.setReviewType(review.getReviewType());
        dto.setTitle(review.getTitle());
        dto.setLikeCount(review.getLikeCount());
        dto.setDislikeCount(review.getDislikeCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setDeleted(review.getDeleted());
        return dto;
    }

    public static Review toEntity(ReviewDto dto) {
        if (dto == null) {
            return null;
        }

        Review review = new Review();
        review.setInstituteId(dto.getInstituteId());
        review.setDescription(dto.getDescription());
        review.setTotalRatingSum(dto.getTotalRatingSum());
        review.setNumberOfRating(dto.getNumberOfRating());
        review.setReviewType(dto.getReviewType());
        review.setTitle(dto.getTitle());
        review.setLikeCount(dto.getLikeCount());
        review.setDislikeCount(dto.getDislikeCount());
        review.setCreatedAt(dto.getCreatedAt());
        review.setUpdatedAt(dto.getUpdatedAt());
        review.setDeleted(dto.getDeleted());
        return review;
    }
}
