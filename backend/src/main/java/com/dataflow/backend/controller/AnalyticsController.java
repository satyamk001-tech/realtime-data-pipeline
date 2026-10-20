package com.dataflow.backend.controller;

import com.dataflow.backend.dto.AnalyticsOverviewDto;
import com.dataflow.backend.dto.CuisineAnalyticsDto;
import com.dataflow.backend.dto.LocationAnalyticsDto;
import com.dataflow.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "High-level restaurant metrics and dimensional aggregations")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get system-wide restaurant analytics overview KPIs")
    public ResponseEntity<AnalyticsOverviewDto> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    @GetMapping("/locations")
    @Operation(summary = "Get restaurant distributions and average costs by city")
    public ResponseEntity<List<LocationAnalyticsDto>> getLocationAnalytics() {
        return ResponseEntity.ok(analyticsService.getLocationAnalytics());
    }

    @GetMapping("/cuisines")
    @Operation(summary = "Get top cuisine rankings and average ratings")
    public ResponseEntity<List<CuisineAnalyticsDto>> getCuisineAnalytics() {
        return ResponseEntity.ok(analyticsService.getCuisineAnalytics());
    }
}
