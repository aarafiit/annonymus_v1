package com.example.annonymus_v1.controller;

import com.example.annonymus_v1.dto.AnalyticsDto;
import com.example.annonymus_v1.dto.AnalyticsProjection;
import com.example.annonymus_v1.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(defaultValue = "") String searchParam
    ) {
        Page<AnalyticsProjection> page = analyticsService.getAnalytics(searchParam, pageNumber, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", pageNumber);

        return ResponseEntity.ok(response);
    }

}
