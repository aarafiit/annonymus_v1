package com.example.annonymus_v1.service;

import com.example.annonymus_v1.dto.AnalyticsDto;

import java.util.List;

public interface AnalyticsService {
    List<AnalyticsDto> getAnalytics(String searchParam);
}
