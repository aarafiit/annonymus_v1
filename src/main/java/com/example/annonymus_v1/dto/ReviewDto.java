package com.example.annonymus_v1.dto;

import com.example.annonymus_v1.enumurator.VoteType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDto {

    private UUID id;
    private String title;
    private String description;
    private Long instituteId;
    private String instituteName;
    private Long reviewType;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

    /**
     * Replies on this entry, counted in the same query that fetches it.
     *
     * <p>The listing shows this next to the tallies so a reader can see where the
     * discussion is without opening every entry to find out.
     */
    private Long commentCount;

    /**
     * The calling reader's own standing on this entry, or null if they hold none.
     *
     * <p>Not read from the database with the row - it depends on who is asking, so
     * it is filled in per request. See {@code ReviewService#applyVotes}.
     */
    private VoteType userVote;

    /** Full constructor, kept for the JPQL projection queries. */
    public ReviewDto(UUID id, String title, String description, Long instituteId, String instituteName,
                     Long reviewType, Long likeCount, Long dislikeCount, LocalDateTime createdAt,
                     LocalDateTime updatedAt, Boolean deleted, Long commentCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instituteId = instituteId;
        this.instituteName = instituteName;
        this.reviewType = reviewType;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.commentCount = commentCount;
    }

    public ReviewDto(UUID id, String title, String description, Long instituteId, String instituteName,
                     Long reviewType, Long likeCount, Long dislikeCount, LocalDateTime createdAt,
                     LocalDateTime updatedAt, Boolean deleted) {
        this(id, title, description, instituteId, instituteName, reviewType, likeCount, dislikeCount,
                createdAt, updatedAt, deleted, 0L);
    }
}
