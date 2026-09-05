package com.example.annonymus_v1.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Rejects requests that have exhausted their budget, before they reach a controller.
 *
 * <p>Deliberately a servlet filter rather than a check inside each handler. A handler
 * check protects only the handlers somebody remembered to annotate, and it runs after
 * request parsing and after Spring has done the work of dispatching - which is most of
 * the cost the limiter is trying to avoid paying. A filter covers every endpoint,
 * including ones added later, and rejects for the price of one Redis round trip.
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final TokenBucketRateLimiter rateLimiter;
    private final RateLimitPolicyResolver policyResolver;
    private final ClientIpResolver clientIpResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        RateLimitProperties.Rule rule = policyResolver.resolve(request);

        // The bucket is keyed on the client address alone, never on anything the
        // caller can vary at will. Folding the fingerprint header into the key would
        // let a client mint unlimited budgets simply by changing it on each request.
        String clientKey = clientIpResolver.resolve(request);

        RateLimitDecision decision = rateLimiter.tryConsume(rule, clientKey);

        if (decision.remainingTokens() >= 0) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(rule.getCapacity()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remainingTokens()));
            response.setHeader("X-RateLimit-Policy", decision.policy());
        }

        if (decision.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1,
                Duration.ofMillis(decision.retryAfterMillis()).toSeconds());

        log.warn("Rate limit '{}' exceeded by {} for {} {}",
                decision.policy(), clientKey, request.getMethod(), request.getRequestURI());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"about:blank",\
                "title":"Too Many Requests",\
                "status":429,\
                "detail":"Rate limit exceeded. Retry in %d second(s).",\
                "policy":"%s"}"""
                .formatted(retryAfterSeconds, decision.policy()));
    }

    /**
     * Preflight requests carry no application work and are issued by the browser, not
     * the page - charging them would let a legitimate client exhaust its own budget
     * before sending a single real request.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }
}
