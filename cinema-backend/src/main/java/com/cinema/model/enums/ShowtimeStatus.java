package com.cinema.model.enums;

/**
 * Trạng thái suất chiếu.
 * - SCHEDULED  : Đã lên lịch, chưa diễn ra
 * - ONGOING    : Đang chiếu
 * - COMPLETED  : Đã chiếu xong
 * - CANCELLED  : Đã hủy
 */
public enum ShowtimeStatus {
    SCHEDULED,
    ONGOING,
    COMPLETED,
    CANCELLED
}
