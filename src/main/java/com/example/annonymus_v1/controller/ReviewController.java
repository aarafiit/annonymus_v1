package com.example.annonymus_v1.controller;


import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDto> reviewPage = reviewService.getAllReviews(pageable);
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

    @DeleteMapping("/reviews/{id}")
    public void deleteReviewById(@PathVariable UUID id) {
        reviewService.deleteReviewById(id);
    }

}
