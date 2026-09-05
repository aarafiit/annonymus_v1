package com.example.annonymus_v1.ratelimit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the limiter against a real Redis, because the behaviour under test is
 * Redis behaviour: script atomicity, continuous refill and key expiry. A mock would
 * assert only that the code calls the methods it calls.
 */
@Testcontainers
class TokenBucketRateLimiterTest {

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7").withExposedPorts(6379);

    static LettuceConnectionFactory connectionFactory;
    static TokenBucketRateLimiter limiter;

    @BeforeAll
    static void setUp() {
        connectionFactory = new LettuceConnectionFactory(
                REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        script.setResultType(List.class);

        limiter = new TokenBucketRateLimiter(template, (RedisScript<List>) script);
    }

    @AfterAll
    static void tearDown() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    private static RateLimitProperties.Rule rule(int capacity, int refillTokens, Duration period) {
        RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
        rule.setName("test-" + UUID.randomUUID());
        rule.setCapacity(capacity);
        rule.setRefillTokens(refillTokens);
        rule.setRefillPeriod(period);
        return rule;
    }

    @Test
    void spendsTheFullBurstThenRejects() {
        RateLimitProperties.Rule rule = rule(5, 5, Duration.ofMinutes(1));

        for (int i = 0; i < 5; i++) {
            RateLimitDecision decision = limiter.tryConsume(rule, "1.2.3.4");
            assertThat(decision.allowed())
                    .as("request %d of the burst should be allowed", i + 1)
                    .isTrue();
            assertThat(decision.remainingTokens()).isEqualTo(4 - i);
        }

        RateLimitDecision rejected = limiter.tryConsume(rule, "1.2.3.4");
        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.remainingTokens()).isZero();
        assertThat(rejected.retryAfterMillis())
                .as("a rejected caller must be told when to come back")
                .isPositive();
    }

    @Test
    void refillsContinuouslyRatherThanInWindows() throws InterruptedException {
        // Ten tokens a second, so one token is restored roughly every 100ms.
        RateLimitProperties.Rule rule = rule(2, 10, Duration.ofSeconds(1));

        assertThat(limiter.tryConsume(rule, "5.6.7.8").allowed()).isTrue();
        assertThat(limiter.tryConsume(rule, "5.6.7.8").allowed()).isTrue();
        assertThat(limiter.tryConsume(rule, "5.6.7.8").allowed())
                .as("bucket is empty")
                .isFalse();

        TimeUnit.MILLISECONDS.sleep(400);

        assertThat(limiter.tryConsume(rule, "5.6.7.8").allowed())
                .as("tokens should accrue with elapsed time, not at a window boundary")
                .isTrue();
    }

    @Test
    void keepsSeparateBudgetsPerClient() {
        RateLimitProperties.Rule rule = rule(1, 1, Duration.ofMinutes(1));

        assertThat(limiter.tryConsume(rule, "10.0.0.1").allowed()).isTrue();
        assertThat(limiter.tryConsume(rule, "10.0.0.1").allowed()).isFalse();

        assertThat(limiter.tryConsume(rule, "10.0.0.2").allowed())
                .as("one client exhausting its budget must not throttle another")
                .isTrue();
    }

    @Test
    void keepsSeparateBudgetsPerPolicy() {
        RateLimitProperties.Rule writes = rule(1, 1, Duration.ofMinutes(1));
        RateLimitProperties.Rule reads = rule(1, 1, Duration.ofMinutes(1));

        assertThat(limiter.tryConsume(writes, "10.0.0.9").allowed()).isTrue();
        assertThat(limiter.tryConsume(writes, "10.0.0.9").allowed()).isFalse();

        assertThat(limiter.tryConsume(reads, "10.0.0.9").allowed())
                .as("exhausting the write budget must not close the read budget")
                .isTrue();
    }

    /**
     * The reason the refill lives in a Lua script rather than in Java. Executed as
     * read-modify-write from the application, concurrent callers would each observe
     * the same token count and all be admitted.
     */
    @Test
    void admitsExactlyCapacityUnderConcurrentLoad() throws InterruptedException {
        int capacity = 50;
        int threads = 200;
        RateLimitProperties.Rule rule = rule(capacity, capacity, Duration.ofMinutes(5));

        AtomicInteger allowed = new AtomicInteger();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(32);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    if (limiter.tryConsume(rule, "192.168.1.1").allowed()) {
                        allowed.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        startGate.countDown();
        assertThat(finished.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(allowed.get())
                .as("%d concurrent callers against a %d token bucket", threads, capacity)
                .isEqualTo(capacity);
    }
}
