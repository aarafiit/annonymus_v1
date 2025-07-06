package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.AnalyticsDto;
import com.example.annonymus_v1.dto.AnalyticsProjection;
import com.example.annonymus_v1.repository.ReviewRepository;
import com.example.annonymus_v1.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ReviewRepository reviewRepository;

    @Override
    @Cacheable(value = "analytics", key = "#searchParam.isEmpty() ? 'all' : #searchParam")
    public List<AnalyticsDto> getAnalytics(String searchParam) {
        log.info("Fetching analytics from database with search param: {}", searchParam);
        List<AnalyticsProjection> projections = reviewRepository.getAllAnalytics(searchParam);
        return projections.stream().map(projection -> new AnalyticsDto(
                projection.getInstituteId(),
                projection.getInstituteName(),
                projection.getTotalReviews(),
                projection.getTotalPositiveReviews(),
                projection.getTotalNegativeReviews(),
                projection.getTotalMixedReviews(),
                projection.getPositiveReviewPercentage(),
                projection.getNegativeReviewPercentage(),
                projection.getMixedReviewPercentage()
        )).toList();
    }
}
