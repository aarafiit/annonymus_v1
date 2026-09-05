package com.example.annonymus_v1.ratelimit;

/**
 * Outcome of one bucket consumption attempt.
 *
 * @param allowed          whether the request may proceed
 * @param remainingTokens  budget left, surfaced to clients so a well-behaved one can back off
 * @param retryAfterMillis how long until the deficit refills; zero when allowed
 * @param policy           name of the rule that made the decision, for logging and headers
 */
public record RateLimitDecision(
        boolean allowed,
        long remainingTokens,
        long retryAfterMillis,
        String policy) {

    /** Used when the limiter cannot reach Redis; see the fail-open note in the filter. */
    static RateLimitDecision unrestricted(String policy) {
        return new RateLimitDecision(true, -1, 0, policy);
    }
}
