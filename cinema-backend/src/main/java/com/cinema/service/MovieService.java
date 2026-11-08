package com.cinema.service;

import com.cinema.dto.request.MovieRequest;
import com.cinema.dto.response.MovieResponse;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.model.Movie;
import com.cinema.model.enums.MovieStatus;
import com.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MovieService — Business logic layer cho Movie.
 *
 * Nguyên tắc:
 * - Không truy cập MongoDB trực tiếp → dùng MovieRepository
 * - Không nhận/trả HttpRequest/HttpResponse → Controller làm việc đó
 * - Mọi validation business logic (không phải format) đặt ở đây
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    // ─────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────

    /**
     * Lấy danh sách tất cả phim.
     */
    public List<MovieResponse> getAllMovies() {
        log.debug("Lấy tất cả phim");
        return movieRepository.findAll()
                .stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một phim theo id.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy
     */
    public MovieResponse getMovieById(String id) {
        log.debug("Lấy phim với id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
        return MovieResponse.fromEntity(movie);
    }

    /**
     * Lọc phim theo trạng thái.
     */
    public List<MovieResponse> getMoviesByStatus(MovieStatus status) {
        log.debug("Lấy phim theo trạng thái: {}", status);
        return movieRepository.findByStatus(status)
                .stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Tìm phim theo tên (không phân biệt hoa/thường).
     */
    public List<MovieResponse> searchMoviesByTitle(String title) {
        log.debug("Tìm phim với tên: {}", title);
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // WRITE
    // ─────────────────────────────────────────────────────────────────

    /**
     * Tạo phim mới.
     */
    public MovieResponse createMovie(MovieRequest request) {
        log.debug("Tạo phim mới: {}", request.getTitle());

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .genres(request.getGenres())
                .releaseDate(request.getReleaseDate())
                .durationMinutes(request.getDurationMinutes())
                .status(request.getStatus())
                .build();

        Movie saved = movieRepository.save(movie);
        log.info("Đã tạo phim: id={}, title={}", saved.getId(), saved.getTitle());
        return MovieResponse.fromEntity(saved);
    }

    /**
     * Cập nhật thông tin phim.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy phim
     */
    public MovieResponse updateMovie(String id, MovieRequest request) {
        log.debug("Cập nhật phim: id={}", id);

        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));

        existing.setTitle(request.getTitle());
        existing.setGenres(request.getGenres());
        existing.setReleaseDate(request.getReleaseDate());
        existing.setDurationMinutes(request.getDurationMinutes());
        existing.setStatus(request.getStatus());

        Movie updated = movieRepository.save(existing);
        log.info("Đã cập nhật phim: id={}", updated.getId());
        return MovieResponse.fromEntity(updated);
    }

    /**
     * Xóa phim theo id.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy phim
     */
    public void deleteMovie(String id) {
        log.debug("Xóa phim: id={}", id);

        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie", id);
        }

        movieRepository.deleteById(id);
        log.info("Đã xóa phim: id={}", id);
    }
}
