package com.cinema.dto.response;

import com.cinema.model.Movie;
import com.cinema.model.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về thông tin Movie cho client.
 * Không expose internal fields không cần thiết.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private String id;
    private String title;
    private List<String> genres;
    private LocalDate releaseDate;
    private int durationMinutes;
    private MovieStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Movie entity sang MovieResponse DTO.
     * Dùng static factory method thay vì MapStruct để giữ code đơn giản.
     */
    public static MovieResponse fromEntity(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .genres(movie.getGenres())
                .releaseDate(movie.getReleaseDate())
                .durationMinutes(movie.getDurationMinutes())
                .status(movie.getStatus())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
