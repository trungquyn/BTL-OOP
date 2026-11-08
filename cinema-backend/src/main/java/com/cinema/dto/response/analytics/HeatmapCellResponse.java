package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Một ô trong heatmap occupancy: (dayOfWeek × hour) → avgOccupancy.
 *
 * Dùng để vẽ heatmap 2D trên Frontend:
 *   - X axis: giờ trong ngày (0–23)
 *   - Y axis: thứ trong tuần (1=Mon … 7=Sun)
 *   - Màu sắc: avgOccupancyRate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapCellResponse {

    /**
     * Thứ trong tuần (ISO: 1=Mon, 2=Tue, ..., 7=Sun).
     * Dùng nhãn tiếng Việt: "Thứ 2", "Thứ 3", ..., "Chủ nhật".
     */
    private int    dayOfWeek;
    private String dayLabel;     // "Thứ 2", "Thứ 3", ..., "Chủ nhật"

    /** Giờ bắt đầu của suất chiếu (0–23) */
    private int    hour;
    private String hourLabel;    // "09:00", "14:00", "19:00", ...

    /** Số suất chiếu trong ô này */
    private long   count;

    /** Tỷ lệ lấp đầy trung bình trong ô này (0.0 – 1.0) */
    private double avgOccupancyRate;
}
