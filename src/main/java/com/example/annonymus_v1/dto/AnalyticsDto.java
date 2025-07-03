package com.example.annonymus_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsDto {
    private Long instituteId;
    private String instituteName;
    private Long totalReviews;
    private Long totalPositiveReviews;
    private Long totalNegativeReviews;
    private Long totalMixedReviews;
    private Double positiveReviewPercentage;
    private Double negativeReviewPercentage;
    private Double mixedReviewPercentage;
}
