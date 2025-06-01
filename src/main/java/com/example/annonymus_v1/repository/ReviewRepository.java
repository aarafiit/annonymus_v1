package com.example.annonymus_v1.repository;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query("""
    SELECT NEW com.example.annonymus_v1.dto.ReviewDto(
        review.description,
        review.rating,
        institute.id,
        institute.name,
        review.createdAt,
        review.updatedAt,
        review.deleted
    )
    FROM Review review
    JOIN Institute institute ON review.instituteId = institute.id
    WHERE review.deleted = FALSE or review.deleted IS NULL
    order by review.createdAt desc
""")
    Page<ReviewDto> findAllByDeletedIsFalse(Pageable pageable);
}
