package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê occupancy theo từng ngày — dùng cho line chart / trend chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyOccupancyResponse {

    /** Ngày chiếu, dạng "yyyy-MM-dd" */
    private String date;

    /** Tổng số suất chiếu trong ngày */
    private long   totalShowtimes;

    /** Tổng số ghế đã bán trong ngày */
    private long   totalBooked;

    /** Tổng sức chứa trong ngày */
    private long   totalCapacity;

    /** Tỷ lệ lấp đầy trung bình trong ngày (0.0 – 1.0) */
    private double avgOccupancyRate;
}
