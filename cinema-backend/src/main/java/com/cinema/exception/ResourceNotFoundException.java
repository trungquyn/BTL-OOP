package com.cinema.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi tìm không thấy tài nguyên (Movie, Showtime...).
 * Spring tự động trả về HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String id) {
        super("Không tìm thấy " + resourceName + " với id: " + id);
    }
}
