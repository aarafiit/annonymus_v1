package com.example.annonymus_v1.service;

import com.example.annonymus_v1.dto.AnalyticsDto;
import com.example.annonymus_v1.dto.AnalyticsProjection;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AnalyticsService {
    Page<AnalyticsProjection> getAnalytics(String searchParam, int pageNumber, int pageSize);
}
