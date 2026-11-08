package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê tổng hợp của một bộ phim dùng cho analytics.
 * Tất cả metrics được tính từ collection "showtimes" (không cần Ticket/Transaction).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieStatsResponse {

    private String   movieId;
    private String   movieTitle;

    /** Tổng số suất chiếu của phim này */
    private long     totalShowtimes;

    /** Tổng số ghế đã bán (= tổng bookedSeats của tất cả showtime) */
    private long     totalBookedSeats;

    /** Tổng sức chứa (= tổng totalSeats của tất cả showtime) */
    private long     totalCapacity;

    /**
     * Tỷ lệ lấp đầy trung bình (0.0 – 1.0).
     * Ví dụ: 0.72 = 72% trung bình.
     */
    private double   avgOccupancyRate;

    /** Suất chiếu có occupancy cao nhất (%) */
    private double   maxOccupancyRate;

    /** Suất chiếu có occupancy thấp nhất (%) */
    private double   minOccupancyRate;

    /**
     * Doanh thu ước tính (VND) — Điểm tích hợp (Integration Point) với Module Transaction của Người 2.
     * Mặc định là null / 0.0 cho đến khi Người 2 hoàn thành module Transaction/Revenue.
     */
    private Double   revenue;
}
