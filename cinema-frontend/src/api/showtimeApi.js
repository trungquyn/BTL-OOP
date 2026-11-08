import axiosClient from './axiosClient';

/**
 * Service API cho phân hệ Suất Chiếu (Showtime).
 * Kết nối chính xác tới ShowtimeController (/api/showtimes/**) trên Backend.
 */
const showtimeApi = {
  /**
   * Lấy tất cả các suất chiếu.
   */
  getShowtimes() {
    return axiosClient.get('/showtimes');
  },

  /**
   * Lấy chi tiết một suất chiếu theo ID.
   * @param {string} id
   */
  getShowtimeById(id) {
    return axiosClient.get(`/showtimes/${id}`);
  },

  /**
   * Lấy danh sách suất chiếu của một phim cụ thể.
   * @param {string} movieId
   */
  getShowtimesByMovie(movieId) {
    return axiosClient.get(`/showtimes/movie/${movieId}`);
  },

  /**
   * Lấy danh sách suất chiếu theo một ngày cụ thể (định dạng: yyyy-MM-dd).
   * @param {string} showDate
   */
  getShowtimesByDate(showDate) {
    return axiosClient.get(`/showtimes/date/${showDate}`);
  },

  /**
   * Lấy danh sách suất chiếu theo khoảng ngày.
   * @param {string} startDate - Định dạng: yyyy-MM-dd
   * @param {string} endDate   - Định dạng: yyyy-MM-dd
   */
  getShowtimesByRange(startDate, endDate) {
    return axiosClient.get('/showtimes/range', {
      params: { startDate, endDate }
    });
  },

  /**
   * Tạo mới một suất chiếu.
   * @param {Object} data - ShowtimeRequest body
   */
  createShowtime(data) {
    return axiosClient.post('/showtimes', data);
  },

  /**
   * Cập nhật thông tin suất chiếu.
   * @param {string} id
   * @param {Object} data - ShowtimeRequest body
   */
  updateShowtime(id, data) {
    return axiosClient.put(`/showtimes/${id}`, data);
  },

  /**
   * Xóa một suất chiếu theo ID.
   * @param {string} id
   */
  deleteShowtime(id) {
    return axiosClient.delete(`/showtimes/${id}`);
  }
};

export default showtimeApi;
