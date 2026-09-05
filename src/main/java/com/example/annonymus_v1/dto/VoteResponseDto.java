package com.example.annonymus_v1.dto;

import com.example.annonymus_v1.enumurator.VoteType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The state of one review's tallies after a vote, together with the caller's own
 * standing on it.
 *
 * <p>{@code userVote} is null when the caller holds no vote. Returning it means the
 * client never has to guess which button should read as selected, so a reload or a
 * second device shows the same thing the server believes.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoteResponseDto {
    private UUID reviewId;
    private Long likeCount;
    private Long dislikeCount;
    private VoteType userVote;
}
