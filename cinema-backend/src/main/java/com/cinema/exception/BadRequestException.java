package com.cinema.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi dữ liệu đầu vào không hợp lệ về mặt business logic.
 * Ví dụ: startTime >= endTime, bookedSeats > totalSeats, movieId không tồn tại.
 * Spring tự động trả về HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
