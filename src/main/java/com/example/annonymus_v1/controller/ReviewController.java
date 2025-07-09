package com.example.annonymus_v1.controller;


import com.example.annonymus_v1.ServiceImpl.RateLimitService;
import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final RateLimitService rateLimitService;

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String searchParam
    ) {
        Page<ReviewDto> reviewPage = reviewService.getAllReviews(searchParam,page, size);
        return ResponseEntity.ok(reviewPage);
    }

    @PostMapping("/reviews")
    public ReviewDto createReview(@RequestBody(required = true) ReviewDto reviewDto) {
        return reviewService.createReview(reviewDto);
    }

    @GetMapping("/reviews/{id}")
    public ReviewDto getReviewById(@PathVariable UUID id) {
        return reviewService.getReviewById(id);
    }

   /* @PostMapping("/reviews/{id}/like")
    public ReviewDto likeReview(@PathVariable UUID id) {
        return reviewService.likeReview(id);
    }*/

    @PostMapping("/reviews/{id}/like")
    public ResponseEntity<?> likePost(
            @PathVariable UUID id,
            @RequestHeader("X-Client-Fingerprint") String fingerprint,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest request) {

        // Get client IP (consider proxy headers if behind load balancer)
        String ip = getClientIp(request);

        // Create unique client identifier
        String clientIdentifier = DigestUtils.sha256Hex(ip + userAgent + fingerprint);

        if (!rateLimitService.allowAction(id, clientIdentifier)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("You've already performed this action recently");
        }

        reviewService.likeReview(id);

        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0]; // In case of multiple IPs, take the first one
    }



   /* @PostMapping("/reviews/{id}/dislike")
    public ReviewDto dislikeReview(@PathVariable UUID id) {
        return reviewService.dislikeReview(id);
    }*/

    @PostMapping("/reviews/{id}/dislike")
    public ResponseEntity<?> disLikePost(
            @PathVariable UUID id,
            @RequestHeader("X-Client-Fingerprint") String fingerprint,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest request) {

        // Get client IP (consider proxy headers if behind load balancer)
        String ip = getClientIp(request);

        // Create unique client identifier
        String clientIdentifier = DigestUtils.sha256Hex(ip + userAgent + fingerprint);

        if (!rateLimitService.allowAction(id, clientIdentifier)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("You've already performed this action recently");
        }

        reviewService.dislikeReview(id);

        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/reviews/{id}")
    public void deleteReviewById(@PathVariable UUID id) {
        reviewService.deleteReviewById(id);
    }

}
