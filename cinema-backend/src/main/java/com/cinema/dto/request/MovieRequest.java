package com.cinema.dto.request;

import com.cinema.model.enums.MovieStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO nhận dữ liệu từ client khi tạo hoặc cập nhật Movie.
 * Có validation cơ bản bằng Bean Validation (@NotBlank, @Min...).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotBlank(message = "Tên phim không được để trống")
    private String title;

    private List<String> genres;

    @NotNull(message = "Ngày khởi chiếu không được để trống")
    private LocalDate releaseDate;

    @Min(value = 1, message = "Thời lượng phim phải >= 1 phút")
    private int durationMinutes;

    @NotNull(message = "Trạng thái phim không được để trống")
    private MovieStatus status;
}
