package com.example.annonymus_v1.config;

import com.example.annonymus_v1.ratelimit.ClientIpResolver;
import com.example.annonymus_v1.ratelimit.RateLimitFilter;
import com.example.annonymus_v1.ratelimit.RateLimitPolicyResolver;
import com.example.annonymus_v1.ratelimit.RateLimitProperties;
import com.example.annonymus_v1.ratelimit.TokenBucketRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    /**
     * Loaded once at startup. Spring Data sends the script by SHA and only ships the
     * body when Redis reports it does not have it cached, so the per-request cost is
     * a single EVALSHA rather than the script text.
     */
    @Bean
    public RedisScript<List> tokenBucketScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        script.setResultType(List.class);
        return script;
    }

    /**
     * Registered explicitly rather than as a {@code @Component} so the order relative
     * to the CORS filter is stated rather than inherited. It must run after CORS: a
     * 429 emitted before the CORS headers are attached reaches the browser as an
     * opaque cross-origin failure, so the client sees a network error instead of the
     * rate limit it needs to back off from.
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.rate-limit", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            TokenBucketRateLimiter rateLimiter,
            RateLimitPolicyResolver policyResolver,
            ClientIpResolver clientIpResolver) {

        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(
                new RateLimitFilter(rateLimiter, policyResolver, clientIpResolver));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");
        registration.setName("rateLimitFilter");
        return registration;
    }
}
