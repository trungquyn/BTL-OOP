package com.cinema.controller.analytics;

import com.cinema.dto.response.ApiResponse;
import com.cinema.dto.response.analytics.*;
import com.cinema.service.analytics.OccupancyAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * OccupancyAnalyticsController — REST API cho phân tích tỷ lệ lấp đầy.
 *
 * Base URL: /api/analytics/occupancy
 */
@RestController
@RequestMapping("/api/analytics/occupancy")
@RequiredArgsConstructor
public class OccupancyAnalyticsController {

    private final OccupancyAnalyticsService occupancyAnalyticsService;

    /**
     * GET /api/analytics/occupancy
     * Tổng quan số liệu lấp đầy toàn hệ thống (tổng suất chiếu, tổng vé, tổng sức chứa, overall occupancy, phân nhóm demand).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<OccupancySummaryResponse>> getOccupancySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        OccupancySummaryResponse summary = occupancyAnalyticsService.getOccupancySummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy tổng quan tỷ lệ lấp đầy thành công", summary));
    }

    /**
     * GET /api/analytics/occupancy/daily
     * Xu hướng tỷ lệ lấp đầy theo từng ngày (phục vụ biểu đồ đường / Line Chart).
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyOccupancyResponse>>> getDailyOccupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyOccupancyResponse> trend = occupancyAnalyticsService.getDailyOccupancyTrend(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy xu hướng lấp đầy theo ngày thành công", trend));
    }

    /**
     * GET /api/analytics/occupancy/day-of-week
     * Thống kê tỷ lệ lấp đầy theo ngày trong tuần (Thứ 2 đến Chủ nhật).
     */
    @GetMapping("/day-of-week")
    public ResponseEntity<ApiResponse<List<DayOfWeekOccupancyResponse>>> getDayOfWeekOccupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DayOfWeekOccupancyResponse> list = occupancyAnalyticsService.getDayOfWeekOccupancy(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê theo thứ trong tuần thành công", list));
    }

    /**
     * GET /api/analytics/occupancy/time-slots
     * Thống kê tỷ lệ lấp đầy theo khung giờ trong ngày (Morning, Afternoon, Evening, Night).
     */
    @GetMapping("/time-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotOccupancyResponse>>> getTimeSlotOccupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TimeSlotOccupancyResponse> list = occupancyAnalyticsService.getTimeSlotOccupancy(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê theo khung giờ thành công", list));
    }

    /**
     * GET /api/analytics/occupancy/heatmap
     * Ma trận 2D Heatmap (thứ trong tuần × khung giờ bắt đầu).
     */
    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse<List<HeatmapCellResponse>>> getOccupancyHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<HeatmapCellResponse> heatmap = occupancyAnalyticsService.getOccupancyHeatmap(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy ma trận Heatmap thành công", heatmap));
    }
}
