package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO trả về thống kê chi tiết của một suất chiếu phục vụ Showtime Analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeAnalyticsResponse {

    private String    showtimeId;
    private String    movieId;
    private String    movieTitle;
    private String    roomId;
    private LocalDate showDate;
    private String    startTime;
    private String    endTime;
    private int       bookedSeats;
    private int       totalSeats;

    /** Tỷ lệ lấp đầy (0.0 - 1.0) */
    private double    occupancyRate;

    /** Phân loại nhu cầu: HIGH / MEDIUM / LOW */
    private String    demand;
}
