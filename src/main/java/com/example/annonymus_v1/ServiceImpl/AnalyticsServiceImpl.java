package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.AnalyticsDto;
import com.example.annonymus_v1.dto.AnalyticsProjection;
import com.example.annonymus_v1.repository.ReviewRepository;
import com.example.annonymus_v1.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ReviewRepository reviewRepository;

    @Override
    public Page<AnalyticsProjection> getAnalytics(String searchParam,int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<AnalyticsProjection> projections = reviewRepository.getAllAnalytics(searchParam, pageable);
        return projections;
    }
}
