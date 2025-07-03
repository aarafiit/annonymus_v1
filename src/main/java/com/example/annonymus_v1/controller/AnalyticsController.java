package com.example.annonymus_v1.controller;

import com.example.annonymus_v1.dto.AnalyticsDto;
import com.example.annonymus_v1.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public List<AnalyticsDto> getAnalytics(
            @RequestParam(required = false,defaultValue = "" ) String searchParam
    ) {
        return analyticsService.getAnalytics(searchParam);
    }
}
