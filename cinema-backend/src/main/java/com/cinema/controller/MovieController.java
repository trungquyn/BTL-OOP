package com.cinema.controller;

import com.cinema.dto.request.MovieRequest;
import com.cinema.dto.response.ApiResponse;
import com.cinema.dto.response.MovieResponse;
import com.cinema.model.enums.MovieStatus;
import com.cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MovieController — REST API cho Movie.
 *
 * Base URL: /api/movies
 *
 * Nguyên tắc:
 * - Chỉ nhận request, validate format (@Valid), gọi Service, trả response
 * - Không viết business logic ở đây
 * - Không truy cập Repository trực tiếp
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /**
     * GET /api/movies
     * Lấy danh sách tất cả phim.
     * Query param ?status= để lọc theo trạng thái (tùy chọn).
     * Query param ?q= để tìm kiếm theo tên (tùy chọn).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies(
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(required = false) String q) {

        List<MovieResponse> movies;

        if (status != null) {
            movies = movieService.getMoviesByStatus(status);
        } else if (q != null && !q.isBlank()) {
            movies = movieService.searchMoviesByTitle(q);
        } else {
            movies = movieService.getAllMovies();
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phim thành công", movies));
    }

    /**
     * GET /api/movies/{id}
     * Lấy chi tiết một phim.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable String id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phim thành công", movie));
    }

    /**
     * POST /api/movies
     * Tạo phim mới.
     * @Valid kích hoạt Bean Validation trên MovieRequest.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(
            @Valid @RequestBody MovieRequest request) {
        MovieResponse created = movieService.createMovie(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phim thành công", created));
    }

    /**
     * PUT /api/movies/{id}
     * Cập nhật thông tin phim.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieRequest request) {
        MovieResponse updated = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phim thành công", updated));
    }

    /**
     * DELETE /api/movies/{id}
     * Xóa phim.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phim thành công", null));
    }
}
