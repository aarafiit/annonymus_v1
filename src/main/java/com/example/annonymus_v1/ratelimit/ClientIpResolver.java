package com.example.annonymus_v1.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Works out which address to attribute a request to.
 *
 * <p>The subtlety here is that {@code X-Forwarded-For} is written by the client.
 * Reading it unconditionally means an attacker sends a different value on every
 * request and mints a fresh identity each time, which defeats both rate limiting
 * and vote de-duplication. So the header is honoured only when the request
 * actually arrived from a proxy we deployed, and even then we take the rightmost
 * address our proxies did not add - everything to the left of that was supplied
 * by the caller and is unverifiable.
 */
@Slf4j
@Component
public class ClientIpResolver {

    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final String REAL_IP = "X-Real-IP";

    private final List<IpAddressMatcher> trustedProxies;

    public ClientIpResolver(RateLimitProperties properties) {
        this.trustedProxies = new ArrayList<>();
        for (String cidr : properties.getTrustedProxies()) {
            if (!StringUtils.hasText(cidr)) {
                continue;
            }
            try {
                this.trustedProxies.add(new IpAddressMatcher(cidr.trim()));
            } catch (IllegalArgumentException ex) {
                log.warn("Ignoring malformed trusted-proxy entry '{}'", cidr, ex);
            }
        }
        if (this.trustedProxies.isEmpty()) {
            log.info("No trusted proxies configured; forwarded headers will be ignored. "
                    + "Set app.rate-limit.trusted-proxies once this runs behind a proxy.");
        }
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null) {
            return "unknown";
        }
        if (!isTrusted(remoteAddr)) {
            // Direct connection, or an untrusted hop. The socket address is the
            // only thing the caller cannot forge, so nothing else is considered.
            return remoteAddr;
        }

        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (StringUtils.hasText(forwardedFor)) {
            String[] hops = forwardedFor.split(",");
            // Walk right to left: our proxies append, so the rightmost entry that
            // is not one of ours is the furthest address we can still vouch for.
            for (int i = hops.length - 1; i >= 0; i--) {
                String hop = hops[i].trim();
                if (StringUtils.hasText(hop) && !isTrusted(hop)) {
                    return hop;
                }
            }
        }

        String realIp = request.getHeader(REAL_IP);
        if (StringUtils.hasText(realIp) && !isTrusted(realIp.trim())) {
            return realIp.trim();
        }

        return remoteAddr;
    }

    private boolean isTrusted(String address) {
        for (IpAddressMatcher matcher : trustedProxies) {
            try {
                if (matcher.matches(address)) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                // Not a parsable address - treat as untrusted rather than throwing.
                return false;
            }
        }
        return false;
    }
}
