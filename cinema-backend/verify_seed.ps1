##############################################################
# verify_seed.ps1
# Script kiểm tra seed data sau khi Spring Boot đã chạy.
# Chạy: .\verify_seed.ps1
##############################################################

$BASE = "http://localhost:8080"

function Invoke-API {
    param([string]$url, [string]$label)
    try {
        $resp = Invoke-RestMethod -Uri $url -Method GET -TimeoutSec 5
        return $resp
    } catch {
        Write-Host "[FAIL] $label - $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " CINEMA BACKEND - SEED DATA VERIFICATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Kiểm tra movies
Write-Host "[1] Kiểm tra collection MOVIES..." -ForegroundColor Yellow
$movies = Invoke-API "$BASE/api/movies" "GET /api/movies"
if ($movies) {
    $count = $movies.data.Count
    Write-Host "    ✅ Số phim: $count" -ForegroundColor Green
    if ($count -eq 10) {
        Write-Host "    ✅ Đúng 10 phim như mong đợi" -ForegroundColor Green
    } else {
        Write-Host "    ⚠️  Mong đợi 10, thực tế: $count" -ForegroundColor Yellow
    }
    
    $nowShowing = ($movies.data | Where-Object { $_.status -eq "NOW_SHOWING" }).Count
    $comingSoon = ($movies.data | Where-Object { $_.status -eq "COMING_SOON" }).Count
    Write-Host "    ✅ NOW_SHOWING: $nowShowing" -ForegroundColor Green
    Write-Host "    ✅ COMING_SOON: $comingSoon" -ForegroundColor Green
}

Write-Host ""

# 2. Kiểm tra showtimes
Write-Host "[2] Kiểm tra collection SHOWTIMES..." -ForegroundColor Yellow
$showtimes = Invoke-API "$BASE/api/showtimes" "GET /api/showtimes"
if ($showtimes) {
    $count = $showtimes.data.Count
    Write-Host "    ✅ Số suất chiếu: $count" -ForegroundColor Green
    if ($count -ge 300) {
        Write-Host "    ✅ Đủ dữ liệu cho analytics (>= 300)" -ForegroundColor Green
    }
    
    # Kiểm tra bookedSeats <= totalSeats
    $invalid = $showtimes.data | Where-Object { $_.bookedSeats -gt $_.totalSeats }
    if ($invalid.Count -eq 0) {
        Write-Host "    ✅ Tất cả: bookedSeats <= totalSeats" -ForegroundColor Green
    } else {
        Write-Host "    ❌ Vi phạm: bookedSeats > totalSeats trong $($invalid.Count) bản ghi" -ForegroundColor Red
    }
    
    # Kiểm tra khoảng ngày
    $dates = $showtimes.data | Select-Object -ExpandProperty showDate | Sort-Object
    Write-Host "    ✅ Ngày sớm nhất: $($dates[0])" -ForegroundColor Green
    Write-Host "    ✅ Ngày muộn nhất: $($dates[-1])" -ForegroundColor Green
    
    # Kiểm tra roomIds
    $rooms = $showtimes.data | Select-Object -ExpandProperty roomId -Unique | Sort-Object
    Write-Host "    ✅ Room IDs: $($rooms -join ', ')" -ForegroundColor Green
    
    # Occupancy stats
    $rates = $showtimes.data | ForEach-Object { $_.occupancyRate * 100 }
    $high   = ($rates | Where-Object { $_ -ge 80 }).Count
    $medium = ($rates | Where-Object { $_ -ge 40 -and $_ -lt 80 }).Count
    $low    = ($rates | Where-Object { $_ -lt 40 }).Count
    Write-Host ""
    Write-Host "    Occupancy distribution:" -ForegroundColor Cyan
    Write-Host "    ✅ HIGH   (>=80%): $high suất" -ForegroundColor Green
    Write-Host "    ✅ MEDIUM (40-79%): $medium suất" -ForegroundColor Green
    Write-Host "    ✅ LOW    (<40%):  $low suất" -ForegroundColor Green
    
    $avgOcc = [Math]::Round(($rates | Measure-Object -Average).Average, 1)
    $minOcc = [Math]::Round(($rates | Measure-Object -Minimum).Minimum, 1)
    $maxOcc = [Math]::Round(($rates | Measure-Object -Maximum).Maximum, 1)
    Write-Host "    ✅ Occupancy: min=$minOcc%, avg=$avgOcc%, max=$maxOcc%" -ForegroundColor Green
}

Write-Host ""

# 3. Kiểm tra API showtime by movie
Write-Host "[3] Kiểm tra API: showtimes by movie..." -ForegroundColor Yellow
if ($movies -and $movies.data.Count -gt 0) {
    $firstMovieId = $movies.data[0].id
    $byMovie = Invoke-API "$BASE/api/showtimes/movie/$firstMovieId" "GET /api/showtimes/movie/{id}"
    if ($byMovie) {
        Write-Host "    ✅ '$($movies.data[0].title)': $($byMovie.data.Count) suất chiếu" -ForegroundColor Green
    }
}

Write-Host ""

# 4. Kiểm tra API showtime by date
Write-Host "[4] Kiểm tra API: showtimes by date..." -ForegroundColor Yellow
$byDate = Invoke-API "$BASE/api/showtimes/date/2026-09-20" "GET /api/showtimes/date/2026-09-20"
if ($byDate) {
    Write-Host "    ✅ Ngày 2026-09-20: $($byDate.data.Count) suất chiếu" -ForegroundColor Green
}

Write-Host ""

# 5. Kiểm tra movieId integrity
Write-Host "[5] Kiểm tra movieId integrity..." -ForegroundColor Yellow
if ($movies -and $showtimes) {
    $validMovieIds = $movies.data | Select-Object -ExpandProperty id
    $invalidShowtimes = $showtimes.data | Where-Object { $_.movieId -notin $validMovieIds }
    if ($invalidShowtimes.Count -eq 0) {
        Write-Host "    ✅ Tất cả showtimes.movieId đều tham chiếu đến Movie hợp lệ" -ForegroundColor Green
    } else {
        Write-Host "    ❌ $($invalidShowtimes.Count) showtime có movieId không hợp lệ!" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " VERIFICATION COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
