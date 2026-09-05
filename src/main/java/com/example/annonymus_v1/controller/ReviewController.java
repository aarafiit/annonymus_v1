package com.example.annonymus_v1.controller;


import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.dto.VoteResponseDto;
import com.example.annonymus_v1.enumurator.VoteType;
import com.example.annonymus_v1.ratelimit.ClientIpResolver;
import com.example.annonymus_v1.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private static final String FINGERPRINT_HEADER = "X-Client-Fingerprint";

    private final ReviewService reviewService;
    private final ClientIpResolver clientIpResolver;

    /**
     * Shared secret required to remove an entry. Empty by default, which disables
     * the endpoint outright - see {@link #deleteReviewById}.
     */
    @Value("${app.admin.delete-token:}")
    private String adminDeleteToken;

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String searchParam,
            @RequestParam(required = false) Long instituteId,
            @RequestHeader(value = FINGERPRINT_HEADER, required = false) String fingerprint,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest request
    ) {
        Page<ReviewDto> reviewPage = reviewService.getAllReviews(
                searchParam, instituteId, page, size, clientIdentifier(request, userAgent, fingerprint));
        return ResponseEntity.ok(reviewPage);
    }

    @PostMapping("/reviews")
    public ReviewDto createReview(@RequestBody(required = true) ReviewDto reviewDto) {
        return reviewService.createReview(reviewDto);
    }

    @GetMapping("/reviews/{id}")
    public ReviewDto getReviewById(
            @PathVariable UUID id,
            @RequestHeader(value = FINGERPRINT_HEADER, required = false) String fingerprint,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest request) {
        return reviewService.getReviewById(id, clientIdentifier(request, userAgent, fingerprint));
    }

    /**
     * Agreeing is a toggle, not an increment.
     *
     * <p>It used to answer 429 when the caller had already voted, which left the
     * reader with no way to change their mind and made an ordinary second click look
     * like abuse. Now the same call withdraws an existing agree and replaces an
     * existing disagree, and the response says which state the reader ended in.
     */
    @PostMapping("/reviews/{id}/like")
    public ResponseEntity<VoteResponseDto> likePost(
            @PathVariable UUID id,
            @RequestHeader(value = FINGERPRINT_HEADER, required = false) String fingerprint,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest request) {

        return ResponseEntity.ok(reviewService.vote(
                id, clientIdentifier(request, userAgent, fingerprint), VoteType.AGREE));
    }

    @PostMapping("/reviews/{id}/dislike")
    public ResponseEntity<VoteResponseDto> disLikePost(
            @PathVariable UUID id,
            @RequestHeader(value = FINGERPRINT_HEADER, required = false) String fingerprint,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest request) {

        return ResponseEntity.ok(reviewService.vote(
                id, clientIdentifier(request, userAgent, fingerprint), VoteType.DISAGREE));
    }

    /**
     * Removes an entry. Restricted to an operator holding the configured secret.
     *
     * <p>This was previously open: anyone who could reach the API could delete any
     * review, and on an anonymous site that is every visitor. Moderation still has
     * to be possible, so rather than dropping the route it now requires
     * {@code X-Admin-Token} to match {@code app.admin.delete-token}. That property
     * is empty unless an operator sets it, so on a default deployment the route
     * accepts nothing at all.
     *
     * <p>Both the missing-secret and wrong-secret cases answer 404 rather than 401 or
     * 403, so probing cannot distinguish "no token configured" from "wrong token" -
     * or learn that the route exists.
     */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReviewById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {

        if (!isAuthorisedOperator(adminToken)) {
            log.warn("Rejected unauthorised delete attempt for review {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        reviewService.deleteReviewById(id);
        return ResponseEntity.noContent().build();
    }

    /** Constant-time comparison, so the secret cannot be recovered a byte at a time. */
    private boolean isAuthorisedOperator(String presentedToken) {
        if (adminDeleteToken == null || adminDeleteToken.isBlank() || presentedToken == null) {
            return false;
        }
        return MessageDigest.isEqual(
                adminDeleteToken.getBytes(StandardCharsets.UTF_8),
                presentedToken.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Derives the pseudonymous identity used to de-duplicate votes.
     *
     * <p>The address comes from {@link ClientIpResolver} rather than being read from
     * the request here, so vote de-duplication and rate limiting agree on who the
     * caller is and both honour the same trusted-proxy configuration. The result is a
     * one-way digest: it is enough to recognise a repeat voter, and it never stores
     * the address, agent or fingerprint it was derived from.
     */
    private String clientIdentifier(HttpServletRequest request, String userAgent, String fingerprint) {
        return DigestUtils.sha256Hex(clientIpResolver.resolve(request)
                + (userAgent == null ? "" : userAgent)
                + (fingerprint == null ? "" : fingerprint));
    }
}
