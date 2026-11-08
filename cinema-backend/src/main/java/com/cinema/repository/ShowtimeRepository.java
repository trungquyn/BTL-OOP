package com.cinema.repository;

import com.cinema.model.Showtime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho collection "showtimes".
 *
 * Tất cả query dùng compound index (movieId+showDate) đã khai báo trong @CompoundIndexes.
 * Không viết MongoDB query phức tạp ở đây — Aggregation để trong AnalyticsService (Phase 2).
 */
@Repository
public interface ShowtimeRepository extends MongoRepository<Showtime, String> {

    /** Tìm tất cả suất chiếu của một bộ phim */
    List<Showtime> findByMovieId(String movieId);

    /** Tìm suất chiếu theo phòng (dùng khi Room module được tích hợp) */
    List<Showtime> findByRoomId(String roomId);

    /** Tìm suất chiếu theo ngày cụ thể */
    List<Showtime> findByShowDate(LocalDate showDate);

    /** Tìm suất chiếu trong khoảng ngày [startDate, endDate] */
    List<Showtime> findByShowDateBetween(LocalDate startDate, LocalDate endDate);

    /** Tìm suất chiếu của một phim trong khoảng ngày */
    List<Showtime> findByMovieIdAndShowDateBetween(String movieId, LocalDate startDate, LocalDate endDate);

    /** Tìm suất chiếu của một phim vào một ngày cụ thể */
    List<Showtime> findByMovieIdAndShowDate(String movieId, LocalDate showDate);

    /** Đếm số suất chiếu của một bộ phim (dùng cho analytics) */
    long countByMovieId(String movieId);
}
