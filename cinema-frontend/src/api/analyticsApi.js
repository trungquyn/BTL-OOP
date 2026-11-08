import axiosClient from './axiosClient';

/**
 * Service API cho phân hệ Movie & Showtime Analytics.
 * Kết nối tới 3 Analytics Controller của Backend:
 * - /api/analytics/movies/**
 * - /api/analytics/showtimes/**
 * - /api/analytics/occupancy/**
 *
 * Tất cả các hàm đều hỗ trợ lọc ngày qua params: { startDate, endDate, ... }
 */
const analyticsApi = {
  // ─── 1. MOVIE ANALYTICS ──────────────────────────────────────────────────

  /**
   * Lấy thống kê tổng hợp của tất cả các phim.
   * @param {Object} params - { startDate, endDate }
   */
  getMovieAnalytics(params = {}) {
    return axiosClient.get('/analytics/movies', { params });
  },

  /**
   * Lấy danh sách Top phim (theo occupancy hoặc số suất chiếu).
   * @param {Object} params - { by: 'occupancy'|'showtimes', limit: number, startDate, endDate }
   */
  getTopMovies(params = {}) {
    return axiosClient.get('/analytics/movies/top', { params });
  },

  /**
   * Lấy chi tiết số liệu phân tích của 1 bộ phim theo ID.
   * @param {string} movieId
   * @param {Object} params - { startDate, endDate }
   */
  getMovieAnalyticsById(movieId, params = {}) {
    return axiosClient.get(`/analytics/movies/${movieId}`, { params });
  },

  // ─── 2. SHOWTIME ANALYTICS ───────────────────────────────────────────────

  /**
   * Lấy danh sách thống kê chi tiết các suất chiếu.
   * @param {Object} params - { startDate, endDate, movieId, roomId, demand: 'HIGH'|'MEDIUM'|'LOW' }
   */
  getShowtimeAnalytics(params = {}) {
    return axiosClient.get('/analytics/showtimes', { params });
  },

  /**
   * Lấy danh sách các suất chiếu có nhu cầu cao (occupancy >= 80%).
   * @param {Object} params - { limit: number, startDate, endDate }
   */
  getHighDemandShowtimes(params = {}) {
    return axiosClient.get('/analytics/showtimes/high-demand', { params });
  },

  /**
   * Lấy danh sách các suất chiếu có nhu cầu thấp (occupancy < 40%).
   * @param {Object} params - { limit: number, startDate, endDate }
   */
  getLowDemandShowtimes(params = {}) {
    return axiosClient.get('/analytics/showtimes/low-demand', { params });
  },

  /**
   * So sánh thống kê suất chiếu giữa ngày thường (Weekday) và cuối tuần (Weekend).
   * @param {Object} params - { startDate, endDate }
   */
  getWeekdayWeekendStats(params = {}) {
    return axiosClient.get('/analytics/showtimes/weekday-weekend', { params });
  },

  /**
   * Thống kê theo định dạng phòng chiếu (2D, 3D, IMAX, 4DX).
   * @param {Object} params - { startDate, endDate }
   */
  getFormatStats(params = {}) {
    return axiosClient.get('/analytics/showtimes/by-format', { params });
  },

  // ─── 3. OCCUPANCY ANALYTICS ──────────────────────────────────────────────

  /**
   * Lấy tổng quan số liệu tỷ lệ lấp đầy toàn hệ thống.
   * @param {Object} params - { startDate, endDate }
   */
  getOccupancySummary(params = {}) {
    return axiosClient.get('/analytics/occupancy', { params });
  },

  /**
   * Lấy xu hướng tỷ lệ lấp đầy theo từng ngày (Line Chart).
   * @param {Object} params - { startDate, endDate }
   */
  getDailyOccupancy(params = {}) {
    return axiosClient.get('/analytics/occupancy/daily', { params });
  },

  /**
   * Lấy phân bố tỷ lệ lấp đầy theo thứ trong tuần (Thứ 2 đến Chủ nhật).
   * @param {Object} params - { startDate, endDate }
   */
  getDayOfWeekOccupancy(params = {}) {
    return axiosClient.get('/analytics/occupancy/day-of-week', { params });
  },

  /**
   * Lấy phân bố tỷ lệ lấp đầy theo khung giờ (Morning, Afternoon, Evening, Night).
   * @param {Object} params - { startDate, endDate }
   */
  getTimeSlotOccupancy(params = {}) {
    return axiosClient.get('/analytics/occupancy/time-slots', { params });
  },

  /**
   * Lấy dữ liệu ma trận nhiệt 2D (dayOfWeek × hour).
   * @param {Object} params - { startDate, endDate }
   */
  getOccupancyHeatmap(params = {}) {
    return axiosClient.get('/analytics/occupancy/heatmap', { params });
  }
};

export default analyticsApi;
