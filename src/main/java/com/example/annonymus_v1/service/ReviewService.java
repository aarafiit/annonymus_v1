package com.example.annonymus_v1.service;

import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.dto.VoteResponseDto;
import com.example.annonymus_v1.enumurator.VoteType;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ReviewService {
    Page<ReviewDto> getAllReviews(String searchParam, Long instituteId, int pageNumber, int pageSize,
                                  String clientIdentifier);

    ReviewDto createReview(ReviewDto reviewDto);

    ReviewDto getReviewById(UUID id, String clientIdentifier);

    void deleteReviewById(UUID id);

    /**
     * Records, switches or withdraws the caller's vote on a review.
     *
     * <p>Voting the way you already voted withdraws that vote, and voting the other
     * way moves it, so a reader always holds either nothing, an agree, or a
     * disagree - never both.
     */
    VoteResponseDto vote(UUID reviewId, String clientIdentifier, VoteType voteType);

    Map<UUID, VoteType> getVotes(Collection<UUID> reviewIds, String clientIdentifier);
}
