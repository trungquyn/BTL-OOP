package com.cinema.service;

import com.cinema.dto.request.ShowtimeRequest;
import com.cinema.dto.response.ShowtimeResponse;
import com.cinema.exception.BadRequestException;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.model.Showtime;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ShowtimeService — Business logic layer cho Showtime.
 *
 * Validation business logic:
 * 1. movieId phải tồn tại trong collection "movies"
 * 2. totalSeats > 0
 * 3. bookedSeats >= 0 và <= totalSeats
 * 4. startTime < endTime (so sánh chuỗi "HH:mm" — hoạt động vì format đồng nhất)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository; // Dùng để validate movieId tồn tại

    // ─────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────

    /** Lấy tất cả suất chiếu */
    public List<ShowtimeResponse> getAllShowtimes() {
        log.debug("Lấy tất cả suất chiếu");
        return showtimeRepository.findAll()
                .stream()
                .map(ShowtimeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một suất chiếu.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy
     */
    public ShowtimeResponse getShowtimeById(String id) {
        log.debug("Lấy suất chiếu với id: {}", id);
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", id));
        return ShowtimeResponse.fromEntity(showtime);
    }

    /** Lấy tất cả suất chiếu của một bộ phim */
    public List<ShowtimeResponse> getShowtimesByMovie(String movieId) {
        log.debug("Lấy suất chiếu của phim: movieId={}", movieId);
        // Kiểm tra phim có tồn tại không
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", movieId);
        }
        return showtimeRepository.findByMovieId(movieId)
                .stream()
                .map(ShowtimeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** Lấy suất chiếu theo ngày cụ thể */
    public List<ShowtimeResponse> getShowtimesByDate(LocalDate date) {
        log.debug("Lấy suất chiếu ngày: {}", date);
        return showtimeRepository.findByShowDate(date)
                .stream()
                .map(ShowtimeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy suất chiếu trong khoảng ngày.
     *
     * @throws BadRequestException nếu startDate > endDate
     */
    public List<ShowtimeResponse> getShowtimesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Lấy suất chiếu từ {} đến {}", startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate phải nhỏ hơn hoặc bằng endDate");
        }

        return showtimeRepository.findByShowDateBetween(startDate, endDate)
                .stream()
                .map(ShowtimeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // WRITE
    // ─────────────────────────────────────────────────────────────────

    /**
     * Tạo suất chiếu mới.
     * Thực hiện đầy đủ validation trước khi lưu.
     */
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        log.debug("Tạo suất chiếu mới cho movieId={}", request.getMovieId());

        validateShowtimeRequest(request);

        Showtime showtime = Showtime.builder()
                .movieId(request.getMovieId())
                .roomId(request.getRoomId())
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .format(request.getFormat())
                .language(request.getLanguage())
                .basePrice(request.getBasePrice())
                .totalSeats(request.getTotalSeats())
                .bookedSeats(0)       // Mặc định 0 khi tạo mới
                .status(request.getStatus())
                .build();

        Showtime saved = showtimeRepository.save(showtime);
        log.info("Đã tạo suất chiếu: id={}, movieId={}, showDate={}", saved.getId(), saved.getMovieId(), saved.getShowDate());
        return ShowtimeResponse.fromEntity(saved);
    }

    /**
     * Cập nhật suất chiếu.
     * bookedSeats KHÔNG được cập nhật ở đây — chỉ module Ticket (Người 2) mới được tăng bookedSeats.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy suất chiếu
     */
    public ShowtimeResponse updateShowtime(String id, ShowtimeRequest request) {
        log.debug("Cập nhật suất chiếu: id={}", id);

        Showtime existing = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", id));

        validateShowtimeRequest(request);

        // Kiểm tra totalSeats mới không nhỏ hơn bookedSeats hiện tại
        if (request.getTotalSeats() < existing.getBookedSeats()) {
            throw new BadRequestException(
                    "Tổng số ghế mới (" + request.getTotalSeats() +
                    ") không thể nhỏ hơn số ghế đã đặt (" + existing.getBookedSeats() + ")"
            );
        }

        existing.setMovieId(request.getMovieId());
        existing.setRoomId(request.getRoomId());
        existing.setShowDate(request.getShowDate());
        existing.setStartTime(request.getStartTime());
        existing.setEndTime(request.getEndTime());
        existing.setFormat(request.getFormat());
        existing.setLanguage(request.getLanguage());
        existing.setBasePrice(request.getBasePrice());
        existing.setTotalSeats(request.getTotalSeats());
        existing.setStatus(request.getStatus());
        // bookedSeats KHÔNG thay đổi

        Showtime updated = showtimeRepository.save(existing);
        log.info("Đã cập nhật suất chiếu: id={}", updated.getId());
        return ShowtimeResponse.fromEntity(updated);
    }

    /**
     * Xóa suất chiếu.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy suất chiếu
     */
    public void deleteShowtime(String id) {
        log.debug("Xóa suất chiếu: id={}", id);

        if (!showtimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Showtime", id);
        }

        showtimeRepository.deleteById(id);
        log.info("Đã xóa suất chiếu: id={}", id);
    }

    // ─────────────────────────────────────────────────────────────────
    // PRIVATE VALIDATION
    // ─────────────────────────────────────────────────────────────────

    /**
     * Validation business logic cho ShowtimeRequest.
     * Gọi từ createShowtime và updateShowtime.
     */
    private void validateShowtimeRequest(ShowtimeRequest request) {
        // 1. movieId phải tồn tại
        if (!movieRepository.existsById(request.getMovieId())) {
            throw new BadRequestException("Không tìm thấy phim với movieId: " + request.getMovieId());
        }

        // 2. totalSeats > 0 (đã có @Min(1) trong DTO, nhưng double-check ở service)
        if (request.getTotalSeats() <= 0) {
            throw new BadRequestException("Tổng số ghế phải lớn hơn 0");
        }

        // 3. startTime < endTime (so sánh chuỗi "HH:mm" — hoạt động đúng vì format đồng nhất)
        if (request.getStartTime().compareTo(request.getEndTime()) >= 0) {
            throw new BadRequestException(
                    "Giờ bắt đầu (" + request.getStartTime() +
                    ") phải nhỏ hơn giờ kết thúc (" + request.getEndTime() + ")"
            );
        }
    }
}
