package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê occupancy theo ngày trong tuần (Thứ 2 đến Chủ nhật).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayOfWeekOccupancyResponse {

    /** Thứ trong tuần (1 = Thứ 2, ..., 7 = Chủ nhật) */
    private int    dayOfWeek;

    /** Tên thứ (ví dụ: "Thứ 2", "Thứ 3", ..., "Chủ nhật") */
    private String dayLabel;

    /** Tổng số suất chiếu */
    private long   totalShowtimes;

    /** Tổng số ghế đã đặt */
    private long   totalBooked;

    /** Tổng sức chứa */
    private long   totalCapacity;

    /** Tỷ lệ lấp đầy trung bình (0.0 - 1.0) */
    private double avgOccupancyRate;
}
