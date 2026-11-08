package com.cinema.controller.analytics;

import com.cinema.dto.response.ApiResponse;
import com.cinema.dto.response.analytics.MovieStatsResponse;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.service.analytics.MovieAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * MovieAnalyticsController — REST API cho phân tích dữ liệu phim.
 *
 * Base URL: /api/analytics/movies
 */
@RestController
@RequestMapping("/api/analytics/movies")
@RequiredArgsConstructor
public class MovieAnalyticsController {

    private final MovieAnalyticsService movieAnalyticsService;

    /**
     * GET /api/analytics/movies
     * Lấy thống kê của tất cả phim (suất chiếu, vé đã đặt, sức chứa, occupancy).
     * Query params: ?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieStatsResponse>>> getAllMovieStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MovieStatsResponse> stats = movieAnalyticsService.getAllMovieStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê phim thành công", stats));
    }

    /**
     * GET /api/analytics/movies/top
     * Lấy Top N phim theo occupancy hoặc số suất chiếu.
     * Query params: ?by=occupancy|showtimes&limit=5&startDate=...&endDate=...
     */
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<MovieStatsResponse>>> getTopMovies(
            @RequestParam(defaultValue = "occupancy") String by,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MovieStatsResponse> topMovies;
        if ("showtimes".equalsIgnoreCase(by)) {
            topMovies = movieAnalyticsService.getTopMoviesByShowtimeCount(limit, startDate, endDate);
        } else {
            topMovies = movieAnalyticsService.getTopMoviesByOccupancy(limit, startDate, endDate);
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy Top phim thành công", topMovies));
    }

    /**
     * GET /api/analytics/movies/{movieId}
     * Lấy thống kê chi tiết của một bộ phim cụ thể.
     */
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResponse<MovieStatsResponse>> getMovieStats(
            @PathVariable String movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        MovieStatsResponse stats = movieAnalyticsService.getMovieStats(movieId, startDate, endDate)
                .orElseThrow(() -> new ResourceNotFoundException("Phim với ID: " + movieId));

        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê phim thành công", stats));
    }
}
