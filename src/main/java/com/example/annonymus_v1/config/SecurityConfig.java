package com.example.annonymus_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Supplier;

@Configuration
public class SecurityConfig {

    private static final String ALLOWED_IP = "10.44.77.70";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .access(swaggerIpRestriction())
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> swaggerIpRestriction() {
        return (authentication, context) -> {
            HttpServletRequest request = context.getRequest();
            String remoteIp = request.getRemoteAddr();
            boolean allowed = ALLOWED_IP.equals(remoteIp);
            return new AuthorizationDecision(allowed);
        };
    }
}
