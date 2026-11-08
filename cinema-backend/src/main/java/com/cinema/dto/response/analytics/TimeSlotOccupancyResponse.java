package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê occupancy theo khung giờ:
 * - Morning: 06:00–11:59
 * - Afternoon: 12:00–16:59
 * - Evening: 17:00–20:59
 * - Night: 21:00–23:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotOccupancyResponse {

    /** Tên khung giờ (Morning, Afternoon, Evening, Night) */
    private String slotName;

    /** Khoảng thời gian cụ thể (ví dụ: "06:00–11:59") */
    private String timeRange;

    /** Tổng số suất chiếu */
    private long   totalShowtimes;

    /** Tổng số ghế đã đặt */
    private long   totalBooked;

    /** Tổng sức chứa */
    private long   totalCapacity;

    /** Tỷ lệ lấp đầy trung bình (0.0 - 1.0) */
    private double avgOccupancyRate;
}
