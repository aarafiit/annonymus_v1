package com.example.annonymus_v1.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Token bucket limiter backed by Redis.
 *
 * <p>Redis rather than an in-memory map because the limit has to hold across every
 * application instance: a local counter behind a load balancer with three replicas
 * silently permits three times the intended rate. Redis is also the only component
 * here with native key expiry, so idle buckets clean themselves up.
 *
 * <p>A token bucket rather than a fixed window because it allows a short legitimate
 * burst - a reader opening several reviews at once - while still enforcing the
 * average rate, and because it has no window boundary for an attacker to straddle.
 */
@Slf4j
@Service
public class TokenBucketRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> tokenBucketScript;

    public TokenBucketRateLimiter(RedisTemplate<String, String> redisTemplate,
                                  RedisScript<List> tokenBucketScript) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    /**
     * Spends one token from the bucket identified by {@code policy} and {@code clientKey}.
     *
     * <p>Fails open. A limiter exists to protect the service from excess load, so
     * letting a Redis outage take the whole API down would cause exactly the damage
     * the limiter is there to prevent - and the durable protections behind it, the
     * unique constraint on votes in particular, still hold. An endpoint guarding
     * something irreversible would want the opposite choice.
     */
    public RateLimitDecision tryConsume(RateLimitProperties.Rule rule, String clientKey) {
        String key = "rl:" + rule.getName() + ":" + clientKey;
        try {
            List<?> result = redisTemplate.execute(
                    tokenBucketScript,
                    List.of(key),
                    String.valueOf(rule.getCapacity()),
                    String.valueOf(rule.getRefillTokens()),
                    String.valueOf(rule.getRefillPeriod().toMillis()),
                    "1");

            if (result == null || result.size() < 3) {
                log.warn("Unexpected rate limit script result for key {}: {}", key, result);
                return RateLimitDecision.unrestricted(rule.getName());
            }

            return new RateLimitDecision(
                    toLong(result.get(0)) == 1L,
                    toLong(result.get(1)),
                    toLong(result.get(2)),
                    rule.getName());

        } catch (DataAccessException | IllegalStateException ex) {
            log.error("Rate limiter unavailable, allowing request for key {}", key, ex);
            return RateLimitDecision.unrestricted(rule.getName());
        }
    }

    private static long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
