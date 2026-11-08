package com.cinema.config;

/**
 * Các ngưỡng và hằng số phân loại Analytics.
 * Tập trung tại một file duy nhất để dễ bảo trì và thay đổi khi cần.
 */
public final class AnalyticsConstants {

    private AnalyticsConstants() {
        // Utility class
    }

    /** Ngưỡng High Demand: Occupancy >= 80% (0.80) */
    public static final double HIGH_DEMAND_THRESHOLD = 0.80;

    /** Ngưỡng Medium Demand: 40% <= Occupancy < 80% (0.40) */
    public static final double MEDIUM_DEMAND_THRESHOLD = 0.40;

    /**
     * Phân loại mức nhu cầu (Demand Level) dựa trên tỷ lệ lấp đầy.
     *
     * @param occupancyRate tỷ lệ lấp đầy (0.0 - 1.0)
     * @return "HIGH", "MEDIUM", hoặc "LOW"
     */
    public static String classifyDemand(double occupancyRate) {
        if (occupancyRate >= HIGH_DEMAND_THRESHOLD) {
            return "HIGH";
        } else if (occupancyRate >= MEDIUM_DEMAND_THRESHOLD) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Tính toán tỷ lệ lấp đầy an toàn (tránh division by zero).
     *
     * @param bookedSeats số ghế đã đặt
     * @param totalSeats  tổng số ghế
     * @return giá trị làm tròn 4 chữ số thập phân (ví dụ: 0.7500 = 75%)
     */
    public static double safeOccupancyRate(long bookedSeats, long totalSeats) {
        if (totalSeats <= 0 || bookedSeats <= 0) {
            return 0.0;
        }
        double rate = (double) bookedSeats / totalSeats;
        return Math.min(1.0, Math.round(rate * 10000.0) / 10000.0);
    }
}
