package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê occupancy theo định dạng chiếu (2D / 3D / IMAX / 4DX).
 * Dùng cho biểu đồ pie chart / bar chart phân bố format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatStatsResponse {

    /** Tên format: "_2D", "_3D", "IMAX", "_4DX" */
    private String format;

    /** Số suất chiếu theo format này */
    private long   totalShowtimes;

    /** Tổng ghế đã bán */
    private long   totalBooked;

    /** Tỷ lệ lấp đầy trung bình */
    private double avgOccupancyRate;
}
