package com.example.annonymus_v1.repository;

import com.example.annonymus_v1.dto.AnalyticsDto;
import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query("""
                SELECT new com.example.annonymus_v1.dto.ReviewDto(
                    review.id,
                    review.title,
                    review.description,
                    review.instituteId,
                    institute.name,
                    review.reviewType,
                    review.likeCount,
                    review.dislikeCount,
                    review.createdAt,
                    review.updatedAt,
                    review.deleted,
                    (SELECT COUNT(comment.id) FROM Comment comment
                      WHERE comment.reviewId = review.id
                        AND (comment.deleted = FALSE OR comment.deleted IS NULL))
                )
                FROM Review review
                JOIN Institute institute ON review.instituteId = institute.id
                WHERE (review.deleted = FALSE OR review.deleted IS NULL)
                  AND (:instituteId = 0L OR review.instituteId = :instituteId)
                  AND (
                      LOWER(institute.name) LIKE LOWER(CONCAT('%', :searchParam, '%')) OR
                      LOWER(institute.alias) LIKE LOWER(CONCAT('%', :searchParam, '%'))
                  )
                ORDER BY review.createdAt DESC
            """)
    /**
     * @param instituteId narrows the listing to one institute; {@code 0} means every
     *                    institute. A sentinel rather than a nullable parameter
     *                    because {@code :param IS NULL} leaves PostgreSQL with
     *                    nothing to infer the bind type from, and institute ids are
     *                    generated from 1 so zero can never collide with a real one.
     *                    Callers pass {@code null} to the service, which translates.
     */
    Page<ReviewDto> findAllByDeletedIsFalse(
            @Param("searchParam") String searchParam,
            @Param("instituteId") long instituteId,
            Pageable pageable
    );


    @Query(nativeQuery = true, value = """
        SELECT
            u.id AS instituteId,
            u.name AS instituteName,
            COUNT(r.id) AS totalReviews,
            SUM(CASE WHEN r.review_type = 1 THEN 1 ELSE 0 END) AS totalPositiveReviews,
            SUM(CASE WHEN r.review_type = 0 THEN 1 ELSE 0 END) AS totalNegativeReviews,
            SUM(CASE WHEN r.review_type = 2 THEN 1 ELSE 0 END) AS totalMixedReviews,
            ROUND(100.0 * SUM(CASE WHEN r.review_type = 1 THEN 1 ELSE 0 END) / NULLIF(COUNT(r.id), 0), 2) AS positiveReviewPercentage,
            ROUND(100.0 * SUM(CASE WHEN r.review_type = 0 THEN 1 ELSE 0 END) / NULLIF(COUNT(r.id), 0), 2) AS negativeReviewPercentage,
            ROUND(100.0 * SUM(CASE WHEN r.review_type = 2 THEN 1 ELSE 0 END) / NULLIF(COUNT(r.id), 0), 2) AS mixedReviewPercentage
        FROM
            universities u
        JOIN
            reviews r ON r.institute_id = u.id
        WHERE
            (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchParam, '%')) OR
            LOWER(u.alias) LIKE LOWER(CONCAT('%', :searchParam, '%')))
            AND (r.deleted = false OR r.deleted IS NULL)
            AND (u.deleted = false OR u.deleted IS NULL)
        GROUP BY
            u.id, u.name
        ORDER BY
            totalReviews DESC
        """)
    Page<com.example.annonymus_v1.dto.AnalyticsProjection> getAllAnalytics(
            @Param("searchParam") String searchParam,
            Pageable pageable);

    @Query("""
                SELECT new com.example.annonymus_v1.dto.ReviewDto(
                    review.id,
                    review.title,
                    review.description,
                    review.instituteId,
                    institute.name,
                    review.reviewType,
                    review.likeCount,
                    review.dislikeCount,
                    review.createdAt,
                    review.updatedAt,
                    review.deleted,
                    (SELECT COUNT(comment.id) FROM Comment comment
                      WHERE comment.reviewId = review.id
                        AND (comment.deleted = FALSE OR comment.deleted IS NULL))
                )
                FROM Review review
                JOIN Institute institute ON review.instituteId = institute.id
                WHERE (review.deleted = FALSE OR review.deleted IS NULL)
                AND review.id = :id
            """)
    Optional<ReviewDto> getReviewDetailsById(UUID id);
}
