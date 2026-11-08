package com.cinema.controller.analytics;

import com.cinema.dto.response.ApiResponse;
import com.cinema.dto.response.analytics.FormatStatsResponse;
import com.cinema.dto.response.analytics.ShowtimeAnalyticsResponse;
import com.cinema.service.analytics.ShowtimeAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ShowtimeAnalyticsController — REST API cho phân tích dữ liệu suất chiếu.
 *
 * Base URL: /api/analytics/showtimes
 */
@RestController
@RequestMapping("/api/analytics/showtimes")
@RequiredArgsConstructor
public class ShowtimeAnalyticsController {

    private final ShowtimeAnalyticsService showtimeAnalyticsService;

    /**
     * GET /api/analytics/showtimes
     * Lấy danh sách thống kê các suất chiếu kèm occupancy và phân nhóm demand.
     * Query params: ?startDate=...&endDate=...&movieId=...&roomId=...&demand=HIGH|MEDIUM|LOW
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeAnalyticsResponse>>> getShowtimeAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String demand) {

        List<ShowtimeAnalyticsResponse> list = showtimeAnalyticsService.getShowtimeAnalytics(
                startDate, endDate, movieId, roomId, demand);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thống kê suất chiếu thành công", list));
    }

    /**
     * GET /api/analytics/showtimes/high-demand
     * Lấy các suất chiếu có nhu cầu cao (occupancy >= 80%).
     */
    @GetMapping("/high-demand")
    public ResponseEntity<ApiResponse<List<ShowtimeAnalyticsResponse>>> getHighDemandShowtimes(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ShowtimeAnalyticsResponse> list = showtimeAnalyticsService.getHighDemandShowtimes(limit, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách suất chiếu High Demand thành công", list));
    }

    /**
     * GET /api/analytics/showtimes/low-demand
     * Lấy các suất chiếu có nhu cầu thấp (occupancy < 40%).
     */
    @GetMapping("/low-demand")
    public ResponseEntity<ApiResponse<List<ShowtimeAnalyticsResponse>>> getLowDemandShowtimes(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ShowtimeAnalyticsResponse> list = showtimeAnalyticsService.getLowDemandShowtimes(limit, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách suất chiếu Low Demand thành công", list));
    }

    /**
     * GET /api/analytics/showtimes/weekday-weekend
     * So sánh thống kê suất chiếu giữa ngày thường và cuối tuần.
     */
    @GetMapping("/weekday-weekend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeekdayVsWeekend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> stats = showtimeAnalyticsService.getWeekdayVsWeekendStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("So sánh ngày thường và cuối tuần thành công", stats));
    }

    /**
     * GET /api/analytics/showtimes/by-format
     * Thống kê theo định dạng phòng chiếu (2D, 3D, IMAX, 4DX).
     */
    @GetMapping("/by-format")
    public ResponseEntity<ApiResponse<List<FormatStatsResponse>>> getStatsByFormat(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<FormatStatsResponse> list = showtimeAnalyticsService.getStatsByFormat(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Thống kê theo định dạng thành công", list));
    }
}
