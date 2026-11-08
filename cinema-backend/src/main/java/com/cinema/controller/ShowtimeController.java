package com.cinema.controller;

import com.cinema.dto.request.ShowtimeRequest;
import com.cinema.dto.response.ApiResponse;
import com.cinema.dto.response.ShowtimeResponse;
import com.cinema.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ShowtimeController — REST API cho Showtime.
 *
 * Base URL: /api/showtimes
 *
 * Nguyên tắc:
 * - Chỉ nhận request, validate format (@Valid), gọi Service, trả response
 * - Không viết business logic ở đây
 * - Không truy cập Repository hoặc MongoDB trực tiếp
 */
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    /**
     * GET /api/showtimes
     * Lấy tất cả suất chiếu.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getAllShowtimes() {
        List<ShowtimeResponse> showtimes = showtimeService.getAllShowtimes();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách suất chiếu thành công", showtimes));
    }

    /**
     * GET /api/showtimes/{id}
     * Lấy chi tiết một suất chiếu.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> getShowtimeById(@PathVariable String id) {
        ShowtimeResponse showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin suất chiếu thành công", showtime));
    }

    /**
     * GET /api/showtimes/movie/{movieId}
     * Lấy tất cả suất chiếu của một bộ phim.
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getShowtimesByMovie(
            @PathVariable String movieId) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByMovie(movieId);
        return ResponseEntity.ok(ApiResponse.success("Lấy suất chiếu theo phim thành công", showtimes));
    }

    /**
     * GET /api/showtimes/date/{showDate}
     * Lấy suất chiếu theo ngày cụ thể.
     * Định dạng date: yyyy-MM-dd (ví dụ: 2025-11-15)
     */
    @GetMapping("/date/{showDate}")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getShowtimesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByDate(showDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy suất chiếu theo ngày thành công", showtimes));
    }

    /**
     * GET /api/showtimes/range?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd
     * Lấy suất chiếu trong khoảng ngày.
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getShowtimesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy suất chiếu theo khoảng ngày thành công", showtimes));
    }

    /**
     * POST /api/showtimes
     * Tạo suất chiếu mới.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ShowtimeResponse>> createShowtime(
            @Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse created = showtimeService.createShowtime(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo suất chiếu thành công", created));
    }

    /**
     * PUT /api/showtimes/{id}
     * Cập nhật suất chiếu.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> updateShowtime(
            @PathVariable String id,
            @Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse updated = showtimeService.updateShowtime(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật suất chiếu thành công", updated));
    }

    /**
     * DELETE /api/showtimes/{id}
     * Xóa suất chiếu.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteShowtime(@PathVariable String id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa suất chiếu thành công", null));
    }
}
