package com.cinema.service.analytics;

import com.cinema.config.AnalyticsConstants;
import com.cinema.dto.response.analytics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * OccupancyAnalyticsService — Phân tích chi tiết tỷ lệ lấp đầy sử dụng MongoDB Aggregation.
 *
 * Các biểu đồ hỗ trợ:
 * 1. Trend theo ngày (Daily trend) -> Line Chart
 * 2. Phân bố theo thứ trong tuần (Day of week) -> Bar Chart
 * 3. Phân bố theo khung giờ (Time slots) -> Bar Chart / Donut Chart
 * 4. Heatmap ma trận 2D (dayOfWeek × hour) -> Heatmap Grid
 * 5. Tổng quan số liệu (Summary) -> StatCards
 *
 * Tất cả đều hỗ trợ lọc theo khoảng thời gian (startDate, endDate) và xử lý chia cho 0 an toàn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OccupancyAnalyticsService {

    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final Map<Integer, String> DAY_LABELS = Map.of(
            1, "Thứ 2",
            2, "Thứ 3",
            3, "Thứ 4",
            4, "Thứ 5",
            5, "Thứ 6",
            6, "Thứ 7",
            7, "Chủ nhật"
    );

    // ─── 1. Trend theo ngày (Daily Occupancy) ────────────────────────────────

    /**
     * Lấy xu hướng tỷ lệ lấp đầy theo từng ngày sử dụng MongoDB Aggregation ($group by showDate).
     */
    public List<DailyOccupancyResponse> getDailyOccupancyTrend(LocalDate startDate, LocalDate endDate) {
        log.debug("[OccupancyAnalytics] Daily trend from {} to {}", startDate, endDate);

        List<AggregationOperation> ops = buildDateFilterOperations(startDate, endDate);

        // Group theo showDate
        ops.add(group("showDate")
                .count().as("totalShowtimes")
                .sum("bookedSeats").as("totalBooked")
                .sum("totalSeats").as("totalCapacity"));

        ops.add(sort(Sort.Direction.ASC, "_id"));

        Aggregation aggregation = newAggregation(ops);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);

        List<DailyOccupancyResponse> list = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            LocalDate date = extractLocalDate(doc.get("_id"));
            if (date == null) continue;

            long showtimes = getLongValue(doc.get("totalShowtimes"));
            long booked    = getLongValue(doc.get("totalBooked"));
            long capacity  = getLongValue(doc.get("totalCapacity"));

            list.add(DailyOccupancyResponse.builder()
                    .date(date.format(DATE_FMT))
                    .totalShowtimes(showtimes)
                    .totalBooked(booked)
                    .totalCapacity(capacity)
                    .avgOccupancyRate(AnalyticsConstants.safeOccupancyRate(booked, capacity))
                    .build());
        }

        list.sort(Comparator.comparing(DailyOccupancyResponse::getDate));
        return list;
    }

    // ─── 2. Phân bố theo thứ trong tuần (Day of Week) ────────────────────────

    /**
     * Thống kê occupancy theo ngày trong tuần (Thứ 2 đến Chủ nhật) sử dụng MongoDB Aggregation.
     */
    public List<DayOfWeekOccupancyResponse> getDayOfWeekOccupancy(LocalDate startDate, LocalDate endDate) {
        log.debug("[OccupancyAnalytics] Day of week analytics");

        List<AggregationOperation> ops = buildDateFilterOperations(startDate, endDate);

        // Stage $project để trích xuất ISO DayOfWeek (1=Mon ... 7=Sun)
        ops.add(project("bookedSeats", "totalSeats")
                .andExpression("isoDayOfWeek(showDate)").as("dayOfWeek"));

        // Stage $group theo dayOfWeek
        ops.add(group("dayOfWeek")
                .count().as("totalShowtimes")
                .sum("bookedSeats").as("totalBooked")
                .sum("totalSeats").as("totalCapacity"));

        ops.add(sort(Sort.Direction.ASC, "_id"));

        Aggregation aggregation = newAggregation(ops);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);

        Map<Integer, Document> docMap = new HashMap<>();
        for (Document doc : results.getMappedResults()) {
            int dow = getIntValue(doc.get("_id"));
            if (dow >= 1 && dow <= 7) {
                docMap.put(dow, doc);
            }
        }

        // Đảm bảo đủ 7 ngày từ Thứ 2 đến Chủ nhật
        List<DayOfWeekOccupancyResponse> responseList = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Document d = docMap.get(i);
            long showtimes = d != null ? getLongValue(d.get("totalShowtimes")) : 0;
            long booked    = d != null ? getLongValue(d.get("totalBooked")) : 0;
            long capacity  = d != null ? getLongValue(d.get("totalCapacity")) : 0;

            responseList.add(DayOfWeekOccupancyResponse.builder()
                    .dayOfWeek(i)
                    .dayLabel(DAY_LABELS.getOrDefault(i, "Thứ " + i))
                    .totalShowtimes(showtimes)
                    .totalBooked(booked)
                    .totalCapacity(capacity)
                    .avgOccupancyRate(AnalyticsConstants.safeOccupancyRate(booked, capacity))
                    .build());
        }

        return responseList;
    }

    // ─── 3. Phân bố theo Khung giờ (Time Slots) ──────────────────────────────

    /**
     * Thống kê theo 4 khung giờ thực tế:
     * - Morning: 06:00–11:59
     * - Afternoon: 12:00–16:59
     * - Evening: 17:00–20:59
     * - Night: 21:00–23:59
     */
    public List<TimeSlotOccupancyResponse> getTimeSlotOccupancy(LocalDate startDate, LocalDate endDate) {
        log.debug("[OccupancyAnalytics] Time slot analytics");

        List<AggregationOperation> ops = buildDateFilterOperations(startDate, endDate);

        // Project lấy giờ bắt đầu
        ops.add(project("startTime", "bookedSeats", "totalSeats"));

        Aggregation aggregation = newAggregation(ops);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);

        long[] showtimesBySlot = new long[4];
        long[] bookedBySlot    = new long[4];
        long[] capacityBySlot  = new long[4];

        for (Document doc : results.getMappedResults()) {
            String startTime = doc.getString("startTime");
            if (startTime == null || !startTime.contains(":")) continue;

            int hour = Integer.parseInt(startTime.split(":")[0]);
            long booked   = getLongValue(doc.get("bookedSeats"));
            long capacity = getLongValue(doc.get("totalSeats"));

            int slotIdx;
            if (hour >= 6 && hour < 12) {
                slotIdx = 0; // Morning
            } else if (hour >= 12 && hour < 17) {
                slotIdx = 1; // Afternoon
            } else if (hour >= 17 && hour < 21) {
                slotIdx = 2; // Evening
            } else {
                slotIdx = 3; // Night
            }

            showtimesBySlot[slotIdx]++;
            bookedBySlot[slotIdx] += booked;
            capacityBySlot[slotIdx] += capacity;
        }

        return List.of(
                buildSlot("Morning", "06:00–11:59", showtimesBySlot[0], bookedBySlot[0], capacityBySlot[0]),
                buildSlot("Afternoon", "12:00–16:59", showtimesBySlot[1], bookedBySlot[1], capacityBySlot[1]),
                buildSlot("Evening", "17:00–20:59", showtimesBySlot[2], bookedBySlot[2], capacityBySlot[2]),
                buildSlot("Night", "21:00–23:59", showtimesBySlot[3], bookedBySlot[3], capacityBySlot[3])
        );
    }

    private TimeSlotOccupancyResponse buildSlot(String name, String range, long showtimes, long booked, long capacity) {
        return TimeSlotOccupancyResponse.builder()
                .slotName(name)
                .timeRange(range)
                .totalShowtimes(showtimes)
                .totalBooked(booked)
                .totalCapacity(capacity)
                .avgOccupancyRate(AnalyticsConstants.safeOccupancyRate(booked, capacity))
                .build();
    }

    // ─── 4. Heatmap ma trận 2D (dayOfWeek × hour) ────────────────────────────

    /**
     * Dữ liệu Heatmap 2D (thứ trong tuần × giờ bắt đầu) dùng MongoDB Aggregation.
     */
    public List<HeatmapCellResponse> getOccupancyHeatmap(LocalDate startDate, LocalDate endDate) {
        log.debug("[OccupancyAnalytics] Generating 2D Heatmap");

        List<AggregationOperation> ops = buildDateFilterOperations(startDate, endDate);
        ops.add(project("showDate", "startTime", "bookedSeats", "totalSeats"));

        Aggregation aggregation = newAggregation(ops);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);

        record CellKey(int dayOfWeek, int hour) {}
        Map<CellKey, long[]> accumulator = new HashMap<>(); // [count, booked, capacity]

        for (Document doc : results.getMappedResults()) {
            LocalDate date = extractLocalDate(doc.get("showDate"));
            String startTime = doc.getString("startTime");
            if (date == null || startTime == null || !startTime.contains(":")) continue;

            int dow = date.getDayOfWeek().getValue(); // 1=Mon..7=Sun
            int hour = Integer.parseInt(startTime.split(":")[0]);
            long booked   = getLongValue(doc.get("bookedSeats"));
            long capacity = getLongValue(doc.get("totalSeats"));

            CellKey key = new CellKey(dow, hour);
            accumulator.computeIfAbsent(key, k -> new long[3]);
            accumulator.get(key)[0]++;
            accumulator.get(key)[1] += booked;
            accumulator.get(key)[2] += capacity;
        }

        List<HeatmapCellResponse> cells = new ArrayList<>();
        for (Map.Entry<CellKey, long[]> entry : accumulator.entrySet()) {
            CellKey key = entry.getKey();
            long[] vals = entry.getValue();

            cells.add(HeatmapCellResponse.builder()
                    .dayOfWeek(key.dayOfWeek())
                    .dayLabel(DAY_LABELS.getOrDefault(key.dayOfWeek(), "?"))
                    .hour(key.hour())
                    .hourLabel(String.format("%02d:00", key.hour()))
                    .count(vals[0])
                    .avgOccupancyRate(AnalyticsConstants.safeOccupancyRate(vals[1], vals[2]))
                    .build());
        }

        cells.sort(Comparator.comparingInt(HeatmapCellResponse::getDayOfWeek)
                .thenComparingInt(HeatmapCellResponse::getHour));
        return cells;
    }

    // ─── 5. Tổng quan số liệu (Occupancy Summary) ────────────────────────────

    /**
     * Tổng hợp số liệu toàn hệ thống kèm phân nhóm High/Medium/Low demand.
     */
    public OccupancySummaryResponse getOccupancySummary(LocalDate startDate, LocalDate endDate) {
        log.debug("[OccupancyAnalytics] System occupancy summary");

        List<AggregationOperation> ops = buildDateFilterOperations(startDate, endDate);
        ops.add(project("bookedSeats", "totalSeats"));

        Aggregation aggregation = newAggregation(ops);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);

        long totalShowtimes = 0;
        long totalBooked    = 0;
        long totalCapacity  = 0;
        long highCount      = 0;
        long mediumCount    = 0;
        long lowCount       = 0;

        for (Document doc : results.getMappedResults()) {
            long booked   = getLongValue(doc.get("bookedSeats"));
            long capacity = getLongValue(doc.get("totalSeats"));

            totalShowtimes++;
            totalBooked   += booked;
            totalCapacity += capacity;

            double occ = AnalyticsConstants.safeOccupancyRate(booked, capacity);
            String demand = AnalyticsConstants.classifyDemand(occ);

            switch (demand) {
                case "HIGH"   -> highCount++;
                case "MEDIUM" -> mediumCount++;
                default       -> lowCount++;
            }
        }

        return OccupancySummaryResponse.builder()
                .totalShowtimes(totalShowtimes)
                .totalBooked(totalBooked)
                .totalCapacity(totalCapacity)
                .overallOccupancyRate(AnalyticsConstants.safeOccupancyRate(totalBooked, totalCapacity))
                .highDemandCount(highCount)
                .mediumDemandCount(mediumCount)
                .lowDemandCount(lowCount)
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<AggregationOperation> buildDateFilterOperations(LocalDate startDate, LocalDate endDate) {
        List<Criteria> matchCriteria = new ArrayList<>();
        if (startDate != null && endDate != null) {
            matchCriteria.add(Criteria.where("showDate").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            matchCriteria.add(Criteria.where("showDate").gte(startDate));
        } else if (endDate != null) {
            matchCriteria.add(Criteria.where("showDate").lte(endDate));
        }

        List<AggregationOperation> ops = new ArrayList<>();
        if (!matchCriteria.isEmpty()) {
            ops.add(match(new Criteria().andOperator(matchCriteria.toArray(new Criteria[0]))));
        }
        return ops;
    }

    private int getIntValue(Object obj) {
        if (obj instanceof Number num) return num.intValue();
        return 0;
    }

    private long getLongValue(Object obj) {
        if (obj instanceof Number num) return num.longValue();
        return 0L;
    }

    private LocalDate extractLocalDate(Object obj) {
        if (obj instanceof LocalDate ld) return ld;
        if (obj instanceof Date d) {
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (obj instanceof String s) {
            try { return LocalDate.parse(s); } catch (Exception ignored) {}
        }
        return null;
    }
}
