package com.cinema.dto.response;

import com.cinema.model.Showtime;
import com.cinema.model.enums.ShowtimeFormat;
import com.cinema.model.enums.ShowtimeLanguage;
import com.cinema.model.enums.ShowtimeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin Showtime cho client.
 * Bổ sung thêm field availableSeats và occupancyRate (tính toán từ entity).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {

    private String id;
    private String movieId;
    private String roomId;
    private LocalDate showDate;
    private String startTime;
    private String endTime;
    private ShowtimeFormat format;
    private ShowtimeLanguage language;
    private double basePrice;
    private int totalSeats;
    private int bookedSeats;

    /** Tính từ totalSeats - bookedSeats */
    private int availableSeats;

    /**
     * Tỷ lệ lấp đầy (0.0 - 1.0).
     * Ví dụ: 0.75 = 75% ghế đã được đặt.
     * Dùng cho analytics cơ bản.
     */
    private double occupancyRate;

    private ShowtimeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Showtime entity sang ShowtimeResponse DTO.
     * Tính availableSeats và occupancyRate tại đây.
     */
    public static ShowtimeResponse fromEntity(Showtime showtime) {
        int available = showtime.getTotalSeats() - showtime.getBookedSeats();
        double occupancy = showtime.getTotalSeats() > 0
                ? (double) showtime.getBookedSeats() / showtime.getTotalSeats()
                : 0.0;

        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovieId())
                .roomId(showtime.getRoomId())
                .showDate(showtime.getShowDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .format(showtime.getFormat())
                .language(showtime.getLanguage())
                .basePrice(showtime.getBasePrice())
                .totalSeats(showtime.getTotalSeats())
                .bookedSeats(showtime.getBookedSeats())
                .availableSeats(available)
                .occupancyRate(occupancy)
                .status(showtime.getStatus())
                .createdAt(showtime.getCreatedAt())
                .updatedAt(showtime.getUpdatedAt())
                .build();
    }
}
