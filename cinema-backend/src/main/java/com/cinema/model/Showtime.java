package com.cinema.model;

import com.cinema.model.enums.ShowtimeFormat;
import com.cinema.model.enums.ShowtimeLanguage;
import com.cinema.model.enums.ShowtimeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Showtime Entity — maps to MongoDB collection "showtimes".
 *
 * Quan hệ:
 *   - movieId → movies._id   (Movie 1 → N Showtime)
 *   - roomId  → rooms._id    (để trống cho đến khi module Room được tích hợp)
 *
 * Index tổng hợp (movieId + showDate) tối ưu cho analytics.
 *
 * bookedSeats: mặc định = 0, sẽ được cập nhật khi Ticket được tạo (module Người 2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "showtimes")
@CompoundIndexes({
        @CompoundIndex(name = "movie_date_idx", def = "{'movieId': 1, 'showDate': 1}"),
        @CompoundIndex(name = "date_status_idx", def = "{'showDate': 1, 'status': 1}")
})
public class Showtime {

    @Id
    private String id;

    /** Tham chiếu đến movies._id */
    @Indexed
    private String movieId;

    /**
     * Tham chiếu đến rooms._id.
     * Có thể null trong giai đoạn đầu.
     * Module Room (Người 3) sẽ bổ sung sau.
     */
    private String roomId;

    private LocalDate showDate;

    /** Giờ bắt đầu, định dạng "HH:mm" (ví dụ: "09:00") */
    private String startTime;

    /** Giờ kết thúc, định dạng "HH:mm" (ví dụ: "11:30") */
    private String endTime;

    private ShowtimeFormat format;

    private ShowtimeLanguage language;

    /** Giá vé cơ bản (VND) */
    private double basePrice;

    /** Tổng số ghế trong phòng */
    private int totalSeats;

    /**
     * Số ghế đã đặt.
     * Được cập nhật khi Ticket mới được tạo (module Ticket/Người 2).
     * Mặc định = 0 khi tạo mới.
     */
    @Builder.Default
    private int bookedSeats = 0;

    private ShowtimeStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
