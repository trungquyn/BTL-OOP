package com.cinema.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tổng hợp toàn diện về tỷ lệ lấp đầy (Occupancy Summary).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancySummaryResponse {

    private long   totalShowtimes;
    private long   totalBooked;
    private long   totalCapacity;
    private double overallOccupancyRate;
    private long   highDemandCount;
    private long   mediumDemandCount;
    private long   lowDemandCount;
}
