# Cinema Backend — Hệ thống quản lý và đặt vé rạp chiếu phim

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Database | MongoDB (cinema_db) |
| Build | Maven |

## Cấu trúc package

```
com.cinema/
├── config/          # MongoConfig, WebConfig, DataSeeder
├── model/           # Movie, Showtime + enums/
├── repository/      # MovieRepository, ShowtimeRepository
├── service/         # MovieService, ShowtimeService
│   └── analytics/   # (Phase 2 — sắp triển khai)
├── controller/      # MovieController, ShowtimeController
│   └── analytics/   # (Phase 2 — sắp triển khai)
├── dto/
│   ├── request/     # MovieRequest, ShowtimeRequest
│   └── response/    # ApiResponse, MovieResponse, ShowtimeResponse
│       └── analytics/ # (Phase 2 — sắp triển khai)
└── exception/       # GlobalExceptionHandler, ResourceNotFoundException, BadRequestException
```

## Chạy ứng dụng

### Yêu cầu

- Java 21
- MongoDB đang chạy tại `localhost:27017`

### Khởi động MongoDB (Windows)

```powershell
# Nếu cài bằng winget/MSI:
net start MongoDB

# Hoặc chạy thủ công:
"C:\Program Files\MongoDB\Server\9.0\bin\mongod.exe" --dbpath "C:\data\db"
```

### Chạy Spring Boot

```powershell
# Dùng Maven bundled trong IntelliJ:
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" spring-boot:run -f pom.xml

# Hoặc chạy JAR trực tiếp:
java -jar target\cinema-backend-1.0.0.jar
```

Backend sẽ khởi động tại: **http://localhost:8080**

### Seed data tự động

Khi chạy lần đầu (collection `movies` trống), `DataSeeder` tự động chèn:
- **10 phim** (8 NOW_SHOWING + 2 COMING_SOON)
- **344 suất chiếu** (14/09/2026 → 11/10/2026)

Những lần chạy tiếp theo sẽ **bỏ qua** seed.

## API Endpoints

### Movie — `/api/movies`

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/movies` | Tất cả phim |
| GET | `/api/movies?status=NOW_SHOWING` | Lọc theo status |
| GET | `/api/movies?q=deadpool` | Tìm kiếm theo tên |
| GET | `/api/movies/{id}` | Chi tiết phim |
| POST | `/api/movies` | Tạo phim mới |
| PUT | `/api/movies/{id}` | Cập nhật phim |
| DELETE | `/api/movies/{id}` | Xóa phim |

### Showtime — `/api/showtimes`

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/showtimes` | Tất cả suất chiếu |
| GET | `/api/showtimes/{id}` | Chi tiết suất chiếu |
| GET | `/api/showtimes/movie/{movieId}` | Suất chiếu theo phim |
| GET | `/api/showtimes/date/2026-09-20` | Suất chiếu theo ngày |
| GET | `/api/showtimes/range?startDate=2026-09-14&endDate=2026-09-21` | Khoảng ngày |
| POST | `/api/showtimes` | Tạo suất chiếu |
| PUT | `/api/showtimes/{id}` | Cập nhật suất chiếu |
| DELETE | `/api/showtimes/{id}` | Xóa suất chiếu |

## Seed Data Chi Tiết

### Movies

| # | Tên phim | Thể loại | Thời lượng | Status |
|---|---------|---------|-----------|--------|
| 1 | Deadpool & Wolverine | Hành động, Hài | 127 phút | NOW_SHOWING |
| 2 | Inside Out 2 | Hoạt hình, Gia đình | 100 phút | NOW_SHOWING |
| 3 | Gladiator II | Hành động, Lịch sử | 148 phút | NOW_SHOWING |
| 4 | Alien: Romulus | Kinh dị, Sci-Fi | 119 phút | NOW_SHOWING |
| 5 | Transformers One | Hành động, Hoạt hình | 104 phút | NOW_SHOWING |
| 6 | Venom: The Last Dance | Hành động, Sci-Fi | 111 phút | NOW_SHOWING |
| 7 | Joker: Folie à Deux | Chính kịch, Tội phạm | 138 phút | NOW_SHOWING |
| 8 | The Wild Robot | Hoạt hình, Gia đình | 102 phút | NOW_SHOWING |
| 9 | Moana 2 | Hoạt hình, Phiêu lưu | 100 phút | COMING_SOON |
| 10 | Kraven the Hunter | Hành động, Sci-Fi | 127 phút | COMING_SOON |

### Showtime Distribution

| Phim | Demand | Phòng | Giờ chiếu | Occupancy |
|------|--------|-------|-----------|-----------|
| Deadpool | HIGH | ROOM-01, ROOM-03 | 14:00, 19:00, 21:30 | 74%–100% |
| Inside Out 2 | HIGH | ROOM-02 | 09:00, 14:00, 19:00 | 62%–100% |
| Gladiator II | MEDIUM-HIGH | ROOM-03 | 09:00, 19:00 | 43%–94% |
| Alien: Romulus | MEDIUM-HIGH | ROOM-04 | 16:30, 21:30 | 58%–88% |
| Transformers | MEDIUM | ROOM-04 | 11:30 (cuối tuần) | 32%–62% |
| Venom | MEDIUM-LOW | ROOM-04 | 09:00 | 28%–56% |
| Joker | LOW | ROOM-04 | 14:00 (cuối tuần) | 20%–49% |
| Wild Robot | LOW | ROOM-04 | 11:30 (ngày thường) | 14%–42% |

### Analytics Coverage

Dữ liệu đủ để phân tích:
- ✅ Occupancy theo phim
- ✅ Occupancy theo ngày trong tuần (weekday vs weekend)
- ✅ Occupancy theo khung giờ (sáng/chiều/tối/khuya)
- ✅ Occupancy theo format (IMAX/3D/2D)
- ✅ Top phim theo số suất chiếu
- ✅ High-demand vs Low-demand showtimes
- ✅ Trend 4 tuần (Sep 14 – Oct 11, 2026)
