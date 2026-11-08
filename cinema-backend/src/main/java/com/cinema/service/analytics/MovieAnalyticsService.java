package com.cinema.service.analytics;

import com.cinema.config.AnalyticsConstants;
import com.cinema.dto.response.analytics.MovieStatsResponse;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * MovieAnalyticsService — Phân tích dữ liệu phim sử dụng MongoDB Aggregation Pipeline.
 *
 * Các chỉ số phân tích:
 * - Số suất chiếu theo phim
 * - Tổng số ghế đã đặt theo phim
 * - Tổng sức chứa theo phim
 * - Tỷ lệ lấp đầy trung bình, cao nhất, thấp nhất theo phim
 * - Top phim theo tỷ lệ lấp đầy và theo số suất chiếu
 *
 * Hỗ trợ lọc theo khoảng thời gian (startDate, endDate).
 * Đã xử lý an toàn các edge case: chia cho 0, phim không có suất chiếu, ngày không có dữ liệu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieAnalyticsService {

    private final MongoTemplate   mongoTemplate;
    private final MovieRepository movieRepository;

    /**
     * Lấy thống kê của tất cả phim sử dụng MongoDB Aggregation.
     *
     * @param startDate ngày bắt đầu lọc (nullable)
     * @param endDate   ngày kết thúc lọc (nullable)
     * @return danh sách MovieStatsResponse sắp xếp theo tỷ lệ lấp đầy giảm dần
     */
    public List<MovieStatsResponse> getAllMovieStats(LocalDate startDate, LocalDate endDate) {
        log.debug("[MovieAnalytics] Aggregating movie stats from {} to {}", startDate, endDate);

        // 1. Tạo Criteria lọc theo ngày (nếu có)
        List<Criteria> matchCriteria = new ArrayList<>();
        if (startDate != null && endDate != null) {
            matchCriteria.add(Criteria.where("showDate").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            matchCriteria.add(Criteria.where("showDate").gte(startDate));
        } else if (endDate != null) {
            matchCriteria.add(Criteria.where("showDate").lte(endDate));
        }

        // 2. Xây dựng Aggregation Pipeline trên collection "showtimes"
        List<AggregationOperation> operations = new ArrayList<>();

        if (!matchCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(matchCriteria.toArray(new Criteria[0]))));
        }

        // Stage: Tính occupancy từng showtime an toàn ($cond tránh chia cho 0)
        operations.add(project("movieId", "bookedSeats", "totalSeats")
                .and(ConditionalOperators.when(ComparisonOperators.valueOf("totalSeats").greaterThanValue(0))
                        .then(ArithmeticOperators.valueOf("bookedSeats").divideBy("totalSeats"))
                        .otherwise(0.0)).as("occupancyRate"));

        // Stage: Group theo movieId
        operations.add(group("movieId")
                .count().as("totalShowtimes")
                .sum("bookedSeats").as("totalBookedSeats")
                .sum("totalSeats").as("totalCapacity")
                .avg("occupancyRate").as("avgOccupancyRate")
                .max("occupancyRate").as("maxOccupancyRate")
                .min("occupancyRate").as("minOccupancyRate"));

        // Stage: Sắp xếp theo avgOccupancyRate giảm dần
        operations.add(sort(Sort.Direction.DESC, "avgOccupancyRate"));

        Aggregation aggregation = newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "showtimes", Document.class);
        List<Document> docs = results.getMappedResults();

        // 3. Map kết quả sang MovieStatsResponse kèm tên phim từ collection movies
        Map<String, Document> aggregatedMap = new HashMap<>();
        for (Document doc : docs) {
            String mId = doc.getString("_id");
            if (mId != null) {
                aggregatedMap.put(mId, doc);
            }
        }

        // Đảm bảo cả những phim chưa có suất chiếu cũng được hiển thị (edge case)
        List<Movie> allMovies = movieRepository.findAll();
        List<MovieStatsResponse> responseList = new ArrayList<>();

        for (Movie movie : allMovies) {
            Document statDoc = aggregatedMap.get(movie.getId());
            if (statDoc != null) {
                long totalShowtimes = getLongValue(statDoc.get("totalShowtimes"));
                long totalBooked    = getLongValue(statDoc.get("totalBookedSeats"));
                long totalCapacity  = getLongValue(statDoc.get("totalCapacity"));
                double avgOcc       = getDoubleValue(statDoc.get("avgOccupancyRate"));
                double maxOcc       = getDoubleValue(statDoc.get("maxOccupancyRate"));
                double minOcc       = getDoubleValue(statDoc.get("minOccupancyRate"));

                responseList.add(MovieStatsResponse.builder()
                        .movieId(movie.getId())
                        .movieTitle(movie.getTitle())
                        .totalShowtimes(totalShowtimes)
                        .totalBookedSeats(totalBooked)
                        .totalCapacity(totalCapacity)
                        .avgOccupancyRate(AnalyticsConstants.safeOccupancyRate(totalBooked, totalCapacity))
                        .maxOccupancyRate(Math.min(1.0, Math.round(maxOcc * 10000.0) / 10000.0))
                        .minOccupancyRate(Math.max(0.0, Math.round(minOcc * 10000.0) / 10000.0))
                        .revenue(null) // Integration point với Module 2 của Người 2 (Transaction)
                        .build());
            } else {
                // Phim chưa có suất chiếu nào trong khoảng thời gian lọc
                responseList.add(MovieStatsResponse.builder()
                        .movieId(movie.getId())
                        .movieTitle(movie.getTitle())
                        .totalShowtimes(0)
                        .totalBookedSeats(0)
                        .totalCapacity(0)
                        .avgOccupancyRate(0.0)
                        .maxOccupancyRate(0.0)
                        .minOccupancyRate(0.0)
                        .revenue(null)
                        .build());
            }
        }

        // Sắp xếp lại theo avgOccupancyRate giảm dần
        responseList.sort(Comparator.comparingDouble(MovieStatsResponse::getAvgOccupancyRate).reversed());
        return responseList;
    }

    /**
     * Lấy Top N phim theo tỷ lệ lấp đầy cao nhất.
     */
    public List<MovieStatsResponse> getTopMoviesByOccupancy(int limit, LocalDate startDate, LocalDate endDate) {
        return getAllMovieStats(startDate, endDate).stream()
                .limit(limit > 0 ? limit : 5)
                .collect(Collectors.toList());
    }

    /**
     * Lấy Top N phim theo số lượng suất chiếu nhiều nhất.
     */
    public List<MovieStatsResponse> getTopMoviesByShowtimeCount(int limit, LocalDate startDate, LocalDate endDate) {
        return getAllMovieStats(startDate, endDate).stream()
                .sorted(Comparator.comparingLong(MovieStatsResponse::getTotalShowtimes).reversed())
                .limit(limit > 0 ? limit : 5)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thống kê cho một phim cụ thể.
     */
    public Optional<MovieStatsResponse> getMovieStats(String movieId, LocalDate startDate, LocalDate endDate) {
        return getAllMovieStats(startDate, endDate).stream()
                .filter(m -> m.getMovieId().equals(movieId))
                .findFirst();
    }

    // ─── Helper xử lý kiểu dữ liệu từ Bson Document an toàn ────────────────────

    private long getLongValue(Object obj) {
        if (obj instanceof Number num) {
            return num.longValue();
        }
        return 0L;
    }

    private double getDoubleValue(Object obj) {
        if (obj instanceof Number num) {
            return num.doubleValue();
        }
        return 0.0;
    }
}
