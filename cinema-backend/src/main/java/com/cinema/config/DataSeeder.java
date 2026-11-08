package com.cinema.config;

import com.cinema.model.Movie;
import com.cinema.model.Showtime;
import com.cinema.model.enums.MovieStatus;
import com.cinema.model.enums.ShowtimeFormat;
import com.cinema.model.enums.ShowtimeLanguage;
import com.cinema.model.enums.ShowtimeStatus;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * DataSeeder — Tự động chèn dữ liệu mẫu khi ứng dụng khởi động lần đầu.
 *
 * Cơ chế: Nếu collection "movies" đã có dữ liệu → bỏ qua, không seed lại.
 * Dữ liệu trải dài: 14/09/2026 → 11/10/2026 (28 ngày, 4 tuần).
 *
 * Chỉ tạo dữ liệu trong 2 collection: "movies" và "showtimes".
 * Không tạo collection nào khác.
 *
 * Rooms dùng tạm (roomId dạng String, chưa có Room module):
 *   ROOM-01: 150 ghế — phòng VIP/IMAX
 *   ROOM-02: 120 ghế — phòng tiêu chuẩn lớn
 *   ROOM-03: 100 ghế — phòng tiêu chuẩn vừa
 *   ROOM-04:  80 ghế — phòng tiêu chuẩn nhỏ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    // ─── Cấu hình phòng chiếu ────────────────────────────────────────────────

    private static final String[] ROOM_IDS    = {"ROOM-01", "ROOM-02", "ROOM-03", "ROOM-04"};
    private static final int[]    ROOM_SEATS  = {150,        120,       100,        80};
    private static final double[] ROOM_PRICES = {200_000,    150_000,   130_000,    100_000};

    // ─── Khoảng thời gian seed ────────────────────────────────────────────────

    private static final LocalDate SEED_START = LocalDate.of(2026, 9, 14);
    private static final LocalDate SEED_END   = LocalDate.of(2026, 10, 11);

    /**
     * Cấu hình lịch chiếu: mỗi record = một màn chiếu (screen assignment).
     *
     * dayFilter:
     *   0 = mọi ngày (All days)
     *   1 = chỉ cuối tuần (Sat + Sun)
     *   2 = chỉ ngày thường (Mon–Fri)
     *
     * popularity (quyết định tỷ lệ lấp đầy nền):
     *   5 = Blockbuster   → ~82% base occupancy
     *   4 = Phổ biến      → ~63% base occupancy
     *   3 = Trung bình    → ~47% base occupancy
     *   2 = Thấp          → ~28% base occupancy
     */
    private record ScreenSchedule(int movieIdx, int roomIdx, int popularity,
                                   String[] slots, int dayFilter) {}

    private static final List<ScreenSchedule> SCHEDULES = List.of(
        //                   movie room  pop  time slots                     day
        new ScreenSchedule(  0,    0,    5,  new String[]{"19:00", "21:30"}, 0),  // Deadpool    ROOM-01 tối       | mọi ngày
        new ScreenSchedule(  0,    2,    5,  new String[]{"14:00"},          0),  // Deadpool    ROOM-03 chiều     | mọi ngày
        new ScreenSchedule(  1,    1,    5,  new String[]{"09:00","14:00","19:00"}, 0), // Inside Out 2 ROOM-02   | mọi ngày
        new ScreenSchedule(  2,    2,    4,  new String[]{"09:00", "19:00"}, 0),  // Gladiator II ROOM-03 sáng+tối | mọi ngày
        new ScreenSchedule(  3,    3,    4,  new String[]{"16:30", "21:30"}, 0),  // Alien: Romulus ROOM-04       | mọi ngày
        new ScreenSchedule(  4,    3,    3,  new String[]{"11:30"},          1),  // Transformers ROOM-04 trưa    | cuối tuần
        new ScreenSchedule(  5,    3,    3,  new String[]{"09:00"},          0),  // Venom       ROOM-04 sáng sớm | mọi ngày
        new ScreenSchedule(  6,    3,    2,  new String[]{"14:00"},          1),  // Joker       ROOM-04 chiều    | cuối tuần
        new ScreenSchedule(  7,    3,    2,  new String[]{"11:30"},          2)   // Wild Robot  ROOM-04 trưa     | ngày thường
    );

    // ─── Entry point ─────────────────────────────────────────────────────────

    @Override
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("[DataSeeder] Dữ liệu đã tồn tại ({} phim, {} suất chiếu). Bỏ qua seed.",
                    movieRepository.count(), showtimeRepository.count());
            return;
        }

        log.info("[DataSeeder] ▶ Bắt đầu seed dữ liệu ({} → {})", SEED_START, SEED_END);
        List<Movie> savedMovies   = seedMovies();
        int         showtimeCount = seedShowtimes(savedMovies);

        log.info("[DataSeeder] ✅ Hoàn thành: {} phim, {} suất chiếu.", savedMovies.size(), showtimeCount);
        logSummary(savedMovies, showtimeCount);
    }

    // ─── Seed Movies ──────────────────────────────────────────────────────────

    private List<Movie> seedMovies() {
        List<Movie> movies = List.of(

            // ── NOW_SHOWING ─────────────────────────────────────────────────

            // Index 0 — Blockbuster (pop 5) — nhiều suất, occupancy cao
            buildMovie("Deadpool & Wolverine",
                List.of("Hành động", "Hài"),
                LocalDate.of(2026, 7, 26), 127, MovieStatus.NOW_SHOWING),

            // Index 1 — Blockbuster (pop 5) — phim gia đình, chiếu nhiều suất
            buildMovie("Inside Out 2",
                List.of("Hoạt hình", "Gia đình", "Hài"),
                LocalDate.of(2026, 6, 14), 100, MovieStatus.NOW_SHOWING),

            // Index 2 — Popular (pop 4) — nhiều suất, occupancy trung bình-cao
            buildMovie("Gladiator II",
                List.of("Hành động", "Chính kịch", "Lịch sử"),
                LocalDate.of(2026, 8, 22), 148, MovieStatus.NOW_SHOWING),

            // Index 3 — Popular (pop 4) — phim kinh dị, occupancy trung bình-cao
            buildMovie("Alien: Romulus",
                List.of("Kinh dị", "Khoa học viễn tưởng", "Giật gân"),
                LocalDate.of(2026, 8, 16), 119, MovieStatus.NOW_SHOWING),

            // Index 4 — Medium (pop 3) — chỉ cuối tuần, occupancy trung bình
            buildMovie("Transformers One",
                List.of("Hành động", "Hoạt hình", "Khoa học viễn tưởng"),
                LocalDate.of(2026, 9, 6), 104, MovieStatus.NOW_SHOWING),

            // Index 5 — Medium (pop 3) — suất sáng sớm, occupancy thấp-trung bình
            buildMovie("Venom: The Last Dance",
                List.of("Hành động", "Khoa học viễn tưởng"),
                LocalDate.of(2026, 9, 1), 111, MovieStatus.NOW_SHOWING),

            // Index 6 — Low (pop 2) — chỉ cuối tuần, occupancy thấp
            buildMovie("Joker: Folie à Deux",
                List.of("Chính kịch", "Tội phạm", "Giật gân"),
                LocalDate.of(2026, 9, 5), 138, MovieStatus.NOW_SHOWING),

            // Index 7 — Low (pop 2) — ngày thường, occupancy rất thấp
            buildMovie("The Wild Robot",
                List.of("Hoạt hình", "Gia đình", "Khoa học viễn tưởng"),
                LocalDate.of(2026, 9, 12), 102, MovieStatus.NOW_SHOWING),

            // ── COMING_SOON (không có showtime) ────────────────────────────

            // Index 8 — Coming soon, high expected demand
            buildMovie("Moana 2",
                List.of("Hoạt hình", "Gia đình", "Phiêu lưu"),
                LocalDate.of(2026, 11, 27), 100, MovieStatus.COMING_SOON),

            // Index 9 — Coming soon, low expected demand
            buildMovie("Kraven the Hunter",
                List.of("Hành động", "Khoa học viễn tưởng"),
                LocalDate.of(2026, 12, 13), 127, MovieStatus.COMING_SOON)
        );

        List<Movie> saved = movieRepository.saveAll(movies);
        log.info("[DataSeeder] Đã lưu {} phim.", saved.size());
        return saved;
    }

    private Movie buildMovie(String title, List<String> genres,
                             LocalDate release, int duration, MovieStatus status) {
        return Movie.builder()
                .title(title)
                .genres(genres)
                .releaseDate(release)
                .durationMinutes(duration)
                .status(status)
                .build();
    }

    // ─── Seed Showtimes ───────────────────────────────────────────────────────

    private int seedShowtimes(List<Movie> movies) {
        List<Showtime> all = new ArrayList<>();

        // Random với seed cố định → kết quả luôn giống nhau mỗi lần seed
        Random rng = new Random(42);

        LocalDate date = SEED_START;
        while (!date.isAfter(SEED_END)) {
            DayOfWeek dow   = date.getDayOfWeek();
            boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            boolean weekday = !weekend;                   // Mon–Fri
            boolean friday  = dow == DayOfWeek.FRIDAY;

            for (ScreenSchedule sched : SCHEDULES) {
                // Áp dụng bộ lọc ngày
                if (sched.dayFilter() == 1 && !weekend) continue;  // Chỉ cuối tuần
                if (sched.dayFilter() == 2 && !weekday) continue;  // Chỉ ngày thường

                Movie  movie      = movies.get(sched.movieIdx());
                String roomId     = ROOM_IDS[sched.roomIdx()];
                int    totalSeats = ROOM_SEATS[sched.roomIdx()];
                double basePrice  = ROOM_PRICES[sched.roomIdx()];

                for (String slot : sched.slots()) {
                    // Phụ thu buổi tối 20.000 VND (từ 18:00 trở đi)
                    double finalPrice = basePrice + (slot.compareTo("18:00") >= 0 ? 20_000 : 0);

                    int booked = calcBookedSeats(
                            totalSeats, sched.popularity(), weekend, friday, slot, rng);

                    all.add(Showtime.builder()
                            .movieId(movie.getId())
                            .roomId(roomId)
                            .showDate(date)
                            .startTime(slot)
                            .endTime(calcEndTime(slot, movie.getDurationMinutes()))
                            .format(chooseFormat(sched.movieIdx(), sched.roomIdx()))
                            .language(chooseLanguage(sched.movieIdx()))
                            .basePrice(finalPrice)
                            .totalSeats(totalSeats)
                            .bookedSeats(booked)
                            .status(determineStatus(date, slot))
                            .build());
                }
            }
            date = date.plusDays(1);
        }

        showtimeRepository.saveAll(all);
        log.info("[DataSeeder] Đã lưu {} suất chiếu.", all.size());
        return all.size();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Tính số ghế đã đặt dựa trên:
     *   - Mức độ phổ biến của phim (popularity)
     *   - Ngày trong tuần (cuối tuần/thứ Sáu sẽ cao hơn)
     *   - Khung giờ (tối > chiều > sáng)
     *   - Biến động ngẫu nhiên ±8% (seed cố định để tái tạo)
     *
     * Đảm bảo: 0 ≤ bookedSeats ≤ totalSeats
     */
    private int calcBookedSeats(int total, int pop, boolean weekend,
                                boolean friday, String slot, Random rng) {
        // Tỷ lệ nền theo mức độ phổ biến
        double rate = switch (pop) {
            case 5 -> 0.82;   // Blockbuster: ~82%
            case 4 -> 0.63;   // Phổ biến:    ~63%
            case 3 -> 0.47;   // Trung bình:  ~47%
            case 2 -> 0.28;   // Thấp:        ~28%
            default -> 0.15;
        };

        // Boost cuối tuần / thứ Sáu
        if (weekend) rate += 0.13;
        else if (friday) rate += 0.07;

        // Điều chỉnh theo khung giờ
        rate += switch (slot) {
            case "09:00" -> -0.12;  // Sáng sớm: thấp nhất
            case "11:30" -> -0.06;  // Sáng muộn: hơi thấp
            case "14:00" ->  0.00;  // Chiều: chuẩn
            case "16:30" ->  0.04;  // Chiều muộn: hơi cao
            case "19:00" ->  0.10;  // Tối: cao nhất
            case "21:30" ->  0.02;  // Khuya: trung bình
            default      ->  0.00;
        };

        // Biến động ngẫu nhiên ±8% (để dữ liệu không đều đặn một cách giả tạo)
        rate += (rng.nextDouble() - 0.5) * 0.16;

        // Giới hạn trong khoảng [5%, 100%]
        rate = Math.max(0.05, Math.min(1.0, rate));

        return Math.min((int) Math.round(total * rate), total);
    }

    /**
     * Tính giờ kết thúc = startTime + durationMinutes + 15 phút (quảng cáo/preview).
     * Trả về chuỗi "HH:mm". Xử lý vượt qua nửa đêm.
     */
    private String calcEndTime(String startTime, int durationMinutes) {
        String[] parts   = startTime.split(":");
        int startMinutes = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        int endMinutes   = (startMinutes + durationMinutes + 15) % 1440; // 1440 = 24h
        return String.format("%02d:%02d", endMinutes / 60, endMinutes % 60);
    }

    /**
     * Xác định trạng thái suất chiếu.
     * Mốc cố định: ngày seed là 07/10/2026, ~18:30.
     */
    private ShowtimeStatus determineStatus(LocalDate showDate, String startTime) {
        final LocalDate TODAY = LocalDate.of(2026, 10, 7);

        if (showDate.isBefore(TODAY)) return ShowtimeStatus.COMPLETED;
        if (showDate.isAfter(TODAY))  return ShowtimeStatus.SCHEDULED;

        // Hôm nay: suất trước 18:00 đã kết thúc, 19:00 đang chiếu, sau đó scheduled
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        if (startHour <= 16) return ShowtimeStatus.COMPLETED;
        if (startHour == 19) return ShowtimeStatus.ONGOING;
        return ShowtimeStatus.SCHEDULED;
    }

    /**
     * Chọn định dạng chiếu:
     *   ROOM-01 → IMAX  (phòng VIP)
     *   Movie 0–3 (bom tấn/phổ biến) → 3D
     *   Còn lại → 2D
     */
    private ShowtimeFormat chooseFormat(int movieIdx, int roomIdx) {
        if (roomIdx == 0)   return ShowtimeFormat.IMAX;
        if (movieIdx <= 3)  return ShowtimeFormat._3D;
        return ShowtimeFormat._2D;
    }

    /**
     * Chọn ngôn ngữ:
     *   Deadpool (0) và Gladiator II (2) → Phụ đề (SUBTITLED)
     *   Còn lại → Lồng tiếng (DUBBED)
     */
    private ShowtimeLanguage chooseLanguage(int movieIdx) {
        return (movieIdx == 0 || movieIdx == 2) ? ShowtimeLanguage.SUBTITLED : ShowtimeLanguage.DUBBED;
    }

    /**
     * In thống kê seed để kiểm tra nhanh.
     */
    private void logSummary(List<Movie> movies, int showtimeCount) {
        log.info("──────────────────────────────────────────────────────");
        log.info("[DataSeeder] TỔNG KẾT SEED DATA:");
        log.info("  Số phim       : {}", movies.size());
        log.info("  Số suất chiếu : {}", showtimeCount);
        log.info("  Khoảng ngày   : {} → {}", SEED_START, SEED_END);
        log.info("  Room IDs      : ROOM-01 (150), ROOM-02 (120), ROOM-03 (100), ROOM-04 (80)");
        log.info("  Phim đang chiếu (NOW_SHOWING): {}",
                movies.stream().filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING).count());
        log.info("  Phim sắp chiếu (COMING_SOON): {}",
                movies.stream().filter(m -> m.getStatus() == MovieStatus.COMING_SOON).count());
        log.info("──────────────────────────────────────────────────────");
    }
}
