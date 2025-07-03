package com.example.annonymus_v1.dto;

public interface AnalyticsProjection {
    Long getInstituteId();
    String getInstituteName();
    Long getTotalReviews();
    Long getTotalPositiveReviews();
    Long getTotalNegativeReviews();
    Long getTotalMixedReviews();
    Double getPositiveReviewPercentage();
    Double getNegativeReviewPercentage();
    Double getMixedReviewPercentage();
}
