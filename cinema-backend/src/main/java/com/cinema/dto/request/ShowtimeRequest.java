package com.cinema.dto.request;

import com.cinema.model.enums.ShowtimeFormat;
import com.cinema.model.enums.ShowtimeLanguage;
import com.cinema.model.enums.ShowtimeStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO nhận dữ liệu từ client khi tạo hoặc cập nhật Showtime.
 *
 * Các validation quan trọng:
 * - movieId phải tồn tại → kiểm tra trong ShowtimeService
 * - startTime < endTime  → kiểm tra trong ShowtimeService
 * - bookedSeats <= totalSeats → kiểm tra trong ShowtimeService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeRequest {

    @NotBlank(message = "movieId không được để trống")
    private String movieId;

    /**
     * roomId có thể null (chưa có Room module).
     * Sẽ validate sau khi Người 3 tích hợp Room.
     */
    private String roomId;

    @NotNull(message = "Ngày chiếu không được để trống")
    private LocalDate showDate;

    @NotBlank(message = "Giờ bắt đầu không được để trống")
    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$",
             message = "Giờ bắt đầu phải đúng định dạng HH:mm (ví dụ: 09:00)")
    private String startTime;

    @NotBlank(message = "Giờ kết thúc không được để trống")
    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$",
             message = "Giờ kết thúc phải đúng định dạng HH:mm (ví dụ: 11:30)")
    private String endTime;

    private ShowtimeFormat format;

    private ShowtimeLanguage language;

    @Min(value = 0, message = "Giá vé phải >= 0")
    private double basePrice;

    @Min(value = 1, message = "Tổng số ghế phải >= 1")
    private int totalSeats;

    @NotNull(message = "Trạng thái suất chiếu không được để trống")
    private ShowtimeStatus status;
}
