package com.example.annonymus_v1.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Route matching decides which budget a request is charged against, so a mistake here
 * silently gives an expensive endpoint the generous read budget.
 */
class RateLimitPolicyResolverTest {

    private static RateLimitProperties.Rule rule(String name, String method, String... patterns) {
        RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
        rule.setName(name);
        rule.setMethods(Set.of(method));
        rule.setPatterns(List.of(patterns));
        rule.setCapacity(5);
        rule.setRefillTokens(5);
        rule.setRefillPeriod(Duration.ofMinutes(1));
        return rule;
    }

    private static RateLimitPolicyResolver resolver() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setRules(List.of(
                rule("review-create", "POST", "/reviews"),
                rule("vote", "POST", "/reviews/*/like", "/reviews/*/dislike"),
                rule("comment-create", "POST", "/reviews/*/comment"),
                rule("review-delete", "DELETE", "/reviews/*"),
                rule("analytics", "GET", "/analytics")));
        properties.getFallback().setName("default");
        return new RateLimitPolicyResolver(properties);
    }

    private static String policyFor(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        return resolver().resolve(request).getName();
    }

    @Test
    void chargesReviewCreationToItsOwnBudget() {
        assertThat(policyFor("POST", "/reviews")).isEqualTo("review-create");
    }

    @Test
    void distinguishesVotesFromReviewCreation() {
        assertThat(policyFor("POST", "/reviews/8f14e45f-e0d4-4b3f-9a1d-2c3b4a5d6e7f/like"))
                .isEqualTo("vote");
        assertThat(policyFor("POST", "/reviews/8f14e45f-e0d4-4b3f-9a1d-2c3b4a5d6e7f/dislike"))
                .isEqualTo("vote");
    }

    @Test
    void distinguishesCommentsFromVotes() {
        assertThat(policyFor("POST", "/reviews/8f14e45f-e0d4-4b3f-9a1d-2c3b4a5d6e7f/comment"))
                .isEqualTo("comment-create");
    }

    @Test
    void separatesDeleteFromPostOnTheSamePath() {
        assertThat(policyFor("DELETE", "/reviews/8f14e45f-e0d4-4b3f-9a1d-2c3b4a5d6e7f"))
                .isEqualTo("review-delete");
    }

    @Test
    void capsTheExpensiveAnalyticsEndpoint() {
        assertThat(policyFor("GET", "/analytics")).isEqualTo("analytics");
    }

    @Test
    void fallsBackForReadsWithNoSpecificRule() {
        assertThat(policyFor("GET", "/reviews")).isEqualTo("default");
        assertThat(policyFor("GET", "/institutes")).isEqualTo("default");
        assertThat(policyFor("GET", "/reviews/8f14e45f-e0d4-4b3f-9a1d-2c3b4a5d6e7f"))
                .isEqualTo("default");
    }
}
