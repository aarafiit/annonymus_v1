package com.example.annonymus_v1.repository;

import com.example.annonymus_v1.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Newest reply first.
     *
     * <p>The ordering is part of the query rather than something the caller passes
     * in, because the paging is "show me the first five" - if the sort were left to
     * the caller and omitted, page one would return an arbitrary five rows and the
     * newest reply could be on any page.
     *
     * <p>{@code deleted IS NULL} is tolerated because rows written before the column
     * existed carry no value, and those are live comments, not removed ones.
     */
    @Query("""
            SELECT comment FROM Comment comment
            WHERE comment.reviewId = :reviewId
              AND (comment.deleted = FALSE OR comment.deleted IS NULL)
            ORDER BY comment.createdAt DESC, comment.id DESC
            """)
    Page<Comment> findAllByReviewId(@Param("reviewId") UUID reviewId, Pageable pageable);
}
