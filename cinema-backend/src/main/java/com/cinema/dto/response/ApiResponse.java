package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper response chung cho tất cả API.
 * Giúp frontend dễ handle response theo chuẩn nhất quán.
 *
 * Ví dụ response thành công:
 * {
 *   "success": true,
 *   "message": "Lấy danh sách phim thành công",
 *   "data": [...]
 * }
 *
 * Ví dụ response lỗi:
 * {
 *   "success": false,
 *   "message": "Không tìm thấy phim với id: abc123",
 *   "data": null
 * }
 *
 * @param <T> kiểu dữ liệu của data (MovieResponse, List<ShowtimeResponse>, ...)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /** Factory method cho response thành công */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Factory method cho response lỗi */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
