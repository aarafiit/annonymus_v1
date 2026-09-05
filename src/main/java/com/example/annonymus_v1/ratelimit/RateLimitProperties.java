package com.example.annonymus_v1.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Rate limit budgets, bound from {@code app.rate-limit.*}.
 *
 * <p>Budgets live in configuration rather than in code so they can be tightened
 * during an incident by restarting with a new environment variable, without a
 * rebuild and redeploy.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** Turns the filter off entirely. Useful for load tests against a staging box. */
    private boolean enabled = true;

    /**
     * Addresses or CIDR blocks of proxies permitted to set forwarded headers.
     * Empty means trust nothing, in which case the socket address is the only
     * address used. See {@link ClientIpResolver} for why this must not be open.
     */
    private List<String> trustedProxies = new ArrayList<>();

    /** Applied to any request no rule matches. */
    private Rule fallback = new Rule();

    /** Evaluated in declaration order; the first match wins. */
    private List<Rule> rules = new ArrayList<>();

    @Getter
    @Setter
    public static class Rule {

        /** Identifies the bucket. Requests under different rules never share a budget. */
        private String name = "default";

        /** HTTP methods this rule covers. Empty matches any method. */
        private Set<String> methods = new LinkedHashSet<>();

        /** Spring path patterns such as the like and dislike sub-paths. Empty matches any path. */
        private List<String> patterns = new ArrayList<>();

        /** Maximum tokens held, which is the largest burst a client may spend at once. */
        private int capacity = 120;

        /** Tokens restored every {@link #refillPeriod}. */
        private int refillTokens = 120;

        /** How often {@link #refillTokens} are restored. */
        private Duration refillPeriod = Duration.ofMinutes(1);
    }
}
