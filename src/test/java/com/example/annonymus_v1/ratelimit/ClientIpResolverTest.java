package com.example.annonymus_v1.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The forwarded-header rules are the difference between a limiter that works and one
 * an attacker steps around by sending a new header value on each request.
 */
class ClientIpResolverTest {

    private static ClientIpResolver resolverTrusting(String... cidrs) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setTrustedProxies(List.of(cidrs));
        return new ClientIpResolver(properties);
    }

    @Test
    void ignoresForwardedHeaderWhenNoProxyIsTrusted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "1.1.1.1");

        assertThat(resolverTrusting().resolve(request))
                .as("a forged header must not override the socket address")
                .isEqualTo("203.0.113.9");
    }

    @Test
    void ignoresForwardedHeaderFromAnUntrustedPeer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "1.1.1.1");

        assertThat(resolverTrusting("10.0.0.0/8").resolve(request))
                .isEqualTo("203.0.113.9");
    }

    @Test
    void honoursForwardedHeaderFromATrustedProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Forwarded-For", "198.51.100.7");

        assertThat(resolverTrusting("10.0.0.0/8").resolve(request))
                .isEqualTo("198.51.100.7");
    }

    @Test
    void takesTheRightmostAddressOurProxiesDidNotAdd() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        // The caller supplied "1.1.1.1" themselves; our proxies appended the rest.
        request.addHeader("X-Forwarded-For", "1.1.1.1, 198.51.100.7, 10.0.0.5");

        assertThat(resolverTrusting("10.0.0.0/8").resolve(request))
                .as("everything left of the last untrusted hop is caller-supplied")
                .isEqualTo("198.51.100.7");
    }

    @Test
    void fallsBackToRemoteAddressWhenEveryHopIsOurOwn() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Forwarded-For", "10.0.0.4, 10.0.0.5");

        assertThat(resolverTrusting("10.0.0.0/8").resolve(request))
                .isEqualTo("10.0.0.5");
    }
}
