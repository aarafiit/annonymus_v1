package com.example.annonymus_v1.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Picks the budget that applies to a request.
 *
 * <p>Patterns are parsed once at startup rather than per request, because a limiter
 * that allocates on every call adds load to the path it is meant to protect.
 */
@Component
public class RateLimitPolicyResolver {

    private final List<CompiledRule> rules;
    private final RateLimitProperties.Rule fallback;

    public RateLimitPolicyResolver(RateLimitProperties properties) {
        PathPatternParser parser = PathPatternParser.defaultInstance;
        this.rules = properties.getRules().stream()
                .map(rule -> new CompiledRule(
                        rule,
                        rule.getMethods().stream()
                                .map(method -> method.toUpperCase(Locale.ROOT))
                                .collect(Collectors.toSet()),
                        rule.getPatterns().stream().map(parser::parse).toList()))
                .toList();
        this.fallback = properties.getFallback();
    }

    public RateLimitProperties.Rule resolve(HttpServletRequest request) {
        PathContainer path = PathContainer.parsePath(pathWithinApplication(request));
        String method = request.getMethod().toUpperCase(Locale.ROOT);

        for (CompiledRule rule : rules) {
            if (rule.matches(method, path)) {
                return rule.rule();
            }
        }
        return fallback;
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }

    private record CompiledRule(RateLimitProperties.Rule rule,
                                Set<String> methods,
                                List<PathPattern> patterns) {

        boolean matches(String method, PathContainer path) {
            if (!methods.isEmpty() && !methods.contains(method)) {
                return false;
            }
            if (patterns.isEmpty()) {
                return true;
            }
            for (PathPattern pattern : patterns) {
                if (pattern.matches(path)) {
                    return true;
                }
            }
            return false;
        }
    }
}
