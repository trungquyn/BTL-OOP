package com.cinema.repository;

import com.cinema.model.Movie;
import com.cinema.model.enums.MovieStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho collection "movies".
 * Kế thừa MongoRepository để có sẵn: findAll, findById, save, deleteById, count...
 *
 * Các query được tạo tự động từ tên method (Spring Data Query Derivation).
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    /**
     * Tìm tất cả phim theo trạng thái.
     * Ví dụ: findByStatus(MovieStatus.NOW_SHOWING)
     */
    List<Movie> findByStatus(MovieStatus status);

    /**
     * Tìm phim theo tên (không phân biệt hoa/thường, tìm kiếm gần đúng).
     * Ví dụ: findByTitleContainingIgnoreCase("avatar")
     */
    List<Movie> findByTitleContainingIgnoreCase(String title);

    /**
     * Kiểm tra phim có tồn tại hay không (dùng trong ShowtimeService để validate movieId).
     */
    boolean existsById(String id);
}
