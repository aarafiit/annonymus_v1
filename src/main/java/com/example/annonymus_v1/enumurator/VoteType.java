package com.example.annonymus_v1.enumurator;

/**
 * How a reader has responded to an entry.
 *
 * <p>A reader holds at most one of these per review, which is what makes agreeing
 * and disagreeing mutually exclusive rather than two independent counters.
 */
public enum VoteType {
    AGREE,
    DISAGREE;

    public static VoteType fromLikeFlag(Boolean liked) {
        if (liked == null) {
            return null;
        }
        return liked ? AGREE : DISAGREE;
    }

    public boolean toLikeFlag() {
        return this == AGREE;
    }
}
