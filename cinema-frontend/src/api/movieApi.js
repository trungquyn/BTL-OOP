import axiosClient from './axiosClient';

/**
 * Service API cho phân hệ Quản lý Phim (Movie).
 * Kết nối tới MovieController (/api/movies/**) trên Backend.
 */
const movieApi = {
  /**
   * Lấy danh sách phim (hỗ trợ lọc theo ?status=... hoặc ?q=...).
   * @param {Object} params - { status, q }
   */
  getMovies(params = {}) {
    return axiosClient.get('/movies', { params });
  },

  /**
   * Lấy chi tiết một bộ phim theo ID.
   * @param {string} id
   */
  getMovieById(id) {
    return axiosClient.get(`/movies/${id}`);
  },

  /**
   * Tạo mới một bộ phim.
   * @param {Object} data - MovieRequest body
   */
  createMovie(data) {
    return axiosClient.post('/movies', data);
  },

  /**
   * Cập nhật thông tin một bộ phim theo ID.
   * @param {string} id
   * @param {Object} data - MovieRequest body
   */
  updateMovie(id, data) {
    return axiosClient.put(`/movies/${id}`, data);
  },

  /**
   * Xóa một bộ phim theo ID.
   * @param {string} id
   */
  deleteMovie(id) {
    return axiosClient.delete(`/movies/${id}`);
  }
};

export default movieApi;
