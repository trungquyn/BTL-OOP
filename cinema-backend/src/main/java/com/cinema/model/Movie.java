package com.cinema.model;

import com.cinema.model.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Movie Entity — maps to MongoDB collection "movies".
 *
 * Quan hệ: Movie (1) → (N) Showtime
 * Showtime tham chiếu Movie bằng movieId (không nhúng Movie vào Showtime).
 *
 * Các field tối thiểu cho Phase 1.
 * Sau này có thể thêm: posterUrl, trailerUrl, director, cast, country, ageRating...
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class Movie {

    @Id
    private String id;

    @Indexed
    private String title;

    private List<String> genres;

    private LocalDate releaseDate;

    private int durationMinutes;

    private MovieStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
