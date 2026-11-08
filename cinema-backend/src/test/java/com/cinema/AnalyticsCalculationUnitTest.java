package com.cinema;

import com.cinema.config.AnalyticsConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsCalculationUnitTest {

    @Test
    @DisplayName("Demand Classification: High demand khi occupancy >= 80%")
    void testHighDemand() {
        assertEquals("HIGH", AnalyticsConstants.classifyDemand(0.80));
        assertEquals("HIGH", AnalyticsConstants.classifyDemand(0.85));
        assertEquals("HIGH", AnalyticsConstants.classifyDemand(1.00));
    }

    @Test
    @DisplayName("Demand Classification: Medium demand khi 40% <= occupancy < 80%")
    void testMediumDemand() {
        assertEquals("MEDIUM", AnalyticsConstants.classifyDemand(0.40));
        assertEquals("MEDIUM", AnalyticsConstants.classifyDemand(0.55));
        assertEquals("MEDIUM", AnalyticsConstants.classifyDemand(0.799));
    }

    @Test
    @DisplayName("Demand Classification: Low demand khi occupancy < 40%")
    void testLowDemand() {
        assertEquals("LOW", AnalyticsConstants.classifyDemand(0.00));
        assertEquals("LOW", AnalyticsConstants.classifyDemand(0.15));
        assertEquals("LOW", AnalyticsConstants.classifyDemand(0.399));
    }

    @Test
    @DisplayName("Safe Occupancy Calculation: Xử lý an toàn mẫu số = 0 (tránh division by zero)")
    void testSafeOccupancy_ZeroTotalSeats() {
        assertEquals(0.0, AnalyticsConstants.safeOccupancyRate(50, 0));
        assertEquals(0.0, AnalyticsConstants.safeOccupancyRate(0, 0));
    }

    @Test
    @DisplayName("Safe Occupancy Calculation: Xử lý an toàn số ghế đã đặt = 0")
    void testSafeOccupancy_ZeroBookedSeats() {
        assertEquals(0.0, AnalyticsConstants.safeOccupancyRate(0, 100));
    }

    @Test
    @DisplayName("Safe Occupancy Calculation: Tính đúng tỷ lệ và làm tròn")
    void testSafeOccupancy_NormalCalculation() {
        // 75 / 100 = 0.75
        assertEquals(0.75, AnalyticsConstants.safeOccupancyRate(75, 100));
        // 1 / 3 = 0.3333
        assertEquals(0.3333, AnalyticsConstants.safeOccupancyRate(1, 3));
        // 120 / 120 = 1.0
        assertEquals(1.0, AnalyticsConstants.safeOccupancyRate(120, 120));
    }
}
