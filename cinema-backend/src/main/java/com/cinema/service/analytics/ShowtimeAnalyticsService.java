package com.cinema.service.analytics;

import com.cinema.config.AnalyticsConstants;
import com.cinema.dto.response.analytics.FormatStatsResponse;
import com.cinema.dto.response.analytics.ShowtimeAnalyticsResponse;
import com.cinema.model.Movie;
import com.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * ShowtimeAnalyticsService — Phân tích dữ liệu suất chiếu sử dụng MongoDB Aggregation.
 *
 * Các chức năng:
 * - Thống kê chi tiết từng suất chiếu kèm tỷ lệ lấp đầy và phân nhóm nhu cầu (HIGH, MEDIUM, LOW)
 * - Lọc theo khoảng ngày (startDate, endDate), movieId, roomId, demand level
 * - Danh sách suất chiếu High-demand (>=80%) và Low-demand (<40%)
 * - So sánh giữa ngày thường (Weekday) và cuối tuần (Weekend)
 * - Thống kê theo định dạng chiếu (Format stats)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeAnalyticsService {

    private final MongoTemplate   mongoTemplate;
    private final MovieRepository movieRepository;

    /**
     * Lấy danh sách phân tích các suất chiếu có hỗ trợ lọc theo ngày, phim, phòng, và mức nhu cầu.
     */
    public List<ShowtimeAnalyticsResponse> getShowtimeAnalytics(
            LocalDate startDate, LocalDate endDate, String movieId, String roomId, String demandFilter) {

        log.debug("[ShowtimeAnalytics] Querying showtimes with filters: start={}, end={}, movie={}, room={}, demand={}",
                startDate, endDate, movieId, roomId, demandFilter);

        // 1. Tạo Criteria lọc
        List<Criteria> matchCriteria = new ArrayList<>();
        if (startDate != null && endDate != null) {
            matchCriteria.add(Criteria.where("showDate").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            matchCriteria.add(Criteria.where("showDate").gte(startDate));
        } else if (endDate != null) {
            matchCriteria.add(Criteria.where("showDate").lte(endDate));
        }
        if (movieId != null && !movieId.isBlank()) {
            matchCriteria.add(Criteria.where("movieId").is(movieId));
        }
        if (roomId != null && !roomId.isBlank()) {
            matchCriteria.add(Criteria.where("roomId").is(roomId));
        }

        // 2. Aggregation Pipeline: tính occupancy cho từng suất chiếu
        List<AggregationOperation> operations = new ArrayList<>();
        if (!matchCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(matchCriteria.toArray(new Criteria[0]))));
        }

        operations.add(project("movieId", "roomId", "showDate", "startTime", "endTime", "bookedSeats", "totalSeats", "format")
                .and(ConditionalOperators.when(ComparisonOperators.valueOf("totalSeats").greaterThanValue(0))
                        .then(ArithmeticOperators.valueOf("bookedSeats").divideBy("totalSeats"))
                        .otherwise(0.0)).as("occupancyRate"));

        operations.add(sort(Sort.Direction.DESC, "occupancyRate"));

        Aggregation aggregation = newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);
        List<Document> docs = results.getMappedResults();

        // 3. Tra cứu tiêu đề phim để hiển thị thân thiện trên UI
        Map<String, String> movieTitleMap = movieRepository.findAll().stream()
                .collect(Collectors.toMap(Movie::getId, Movie::getTitle, (a, b) -> a));

        List<ShowtimeAnalyticsResponse> responseList = new ArrayList<>();
        for (Document doc : docs) {
            String showtimeId = doc.get("_id") != null ? doc.get("_id").toString() : null;
            String mId        = doc.getString("movieId");
            String rId        = doc.getString("roomId");
            String startT     = doc.getString("startTime");
            String endT       = doc.getString("endTime");
            int booked        = getIntValue(doc.get("bookedSeats"));
            int total         = getIntValue(doc.get("totalSeats"));
            double occ        = getDoubleValue(doc.get("occupancyRate"));

            LocalDate sDate = extractLocalDate(doc.get("showDate"));

            double safeOcc = AnalyticsConstants.safeOccupancyRate(booked, total);
            String demand = AnalyticsConstants.classifyDemand(safeOcc);

            // Áp dụng bộ lọc demand (nếu có yêu cầu)
            if (demandFilter != null && !demandFilter.isBlank()) {
                if (!demand.equalsIgnoreCase(demandFilter.trim())) {
                    continue;
                }
            }

            responseList.add(ShowtimeAnalyticsResponse.builder()
                    .showtimeId(showtimeId)
                    .movieId(mId)
                    .movieTitle(movieTitleMap.getOrDefault(mId, "Chưa rõ"))
                    .roomId(rId)
                    .showDate(sDate)
                    .startTime(startT)
                    .endTime(endT)
                    .bookedSeats(booked)
                    .totalSeats(total)
                    .occupancyRate(safeOcc)
                    .demand(demand)
                    .build());
        }

        return responseList;
    }

    /**
     * Lấy danh sách suất chiếu High-demand (occupancy >= 80%).
     */
    public List<ShowtimeAnalyticsResponse> getHighDemandShowtimes(int limit, LocalDate startDate, LocalDate endDate) {
        return getShowtimeAnalytics(startDate, endDate, null, null, "HIGH").stream()
                .limit(limit > 0 ? limit : 10)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách suất chiếu Low-demand (occupancy < 40%).
     */
    public List<ShowtimeAnalyticsResponse> getLowDemandShowtimes(int limit, LocalDate startDate, LocalDate endDate) {
        return getShowtimeAnalytics(startDate, endDate, null, null, "LOW").stream()
                .sorted(Comparator.comparingDouble(ShowtimeAnalyticsResponse::getOccupancyRate))
                .limit(limit > 0 ? limit : 10)
                .collect(Collectors.toList());
    }

    /**
     * So sánh thống kê giữa ngày thường (Weekday: T2-T6) và cuối tuần (Weekend: T7-CN) dùng MongoDB Aggregation.
     */
    public Map<String, Object> getWeekdayVsWeekendStats(LocalDate startDate, LocalDate endDate) {
        List<ShowtimeAnalyticsResponse> all = getShowtimeAnalytics(startDate, endDate, null, null, null);

        List<ShowtimeAnalyticsResponse> weekdays = new ArrayList<>();
        List<ShowtimeAnalyticsResponse> weekends = new ArrayList<>();

        for (ShowtimeAnalyticsResponse item : all) {
            if (item.getShowDate() != null) {
                java.time.DayOfWeek dow = item.getShowDate().getDayOfWeek();
                if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
                    weekends.add(item);
                } else {
                    weekdays.add(item);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weekday", summarizeList("weekday", weekdays));
        result.put("weekend", summarizeList("weekend", weekends));
        return result;
    }

    /**
     * Thống kê theo định dạng chiếu (Format stats) dùng MongoDB Aggregation.
     */
    public List<FormatStatsResponse> getStatsByFormat(LocalDate startDate, LocalDate endDate) {
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

        ops.add(group("format")
                .count().as("totalShowtimes")
                .sum("bookedSeats").as("totalBooked")
                .sum("totalSeats").as("totalCapacity"));

        Aggregation aggregation = newAggregation(ops);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);

        List<FormatStatsResponse> list = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            String formatStr = doc.get("_id") != null ? doc.get("_id").toString() : "STANDARD";
            long showtimes   = getLongValue(doc.get("totalShowtimes"));
            long booked      = getLongValue(doc.get("totalBooked"));
            long capacity    = getLongValue(doc.get("totalCapacity"));

            list.add(FormatStatsResponse.builder()
                    .format(formatStr)
                    .totalShowtimes(showtimes)
                    .totalBooked(booked)
                    .avgOccupancyRate(AnalyticsConstants.safeOccupancyRate(booked, capacity))
                    .build());
        }

        list.sort(Comparator.comparingDouble(FormatStatsResponse::getAvgOccupancyRate).reversed());
        return list;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> summarizeList(String label, List<ShowtimeAnalyticsResponse> list) {
        long booked   = list.stream().mapToLong(ShowtimeAnalyticsResponse::getBookedSeats).sum();
        long capacity = list.stream().mapToLong(ShowtimeAnalyticsResponse::getTotalSeats).sum();
        double occ    = AnalyticsConstants.safeOccupancyRate(booked, capacity);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", label);
        m.put("totalShowtimes", list.size());
        m.put("totalBooked", booked);
        m.put("totalCapacity", capacity);
        m.put("avgOccupancyRate", occ);
        return m;
    }

    private int getIntValue(Object obj) {
        if (obj instanceof Number num) return num.intValue();
        return 0;
    }

    private long getLongValue(Object obj) {
        if (obj instanceof Number num) return num.longValue();
        return 0L;
    }

    private double getDoubleValue(Object obj) {
        if (obj instanceof Number num) return num.doubleValue();
        return 0.0;
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
