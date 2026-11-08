import axios from 'axios';

/**
 * Axios instance dùng chung cho toàn bộ Frontend của hệ thống.
 * Base URL đọc từ biến môi trường VITE_API_BASE_URL (mặc định: http://localhost:8080/api).
 * Thiết kế dùng chung để Người 2 và Người 3 có thể tái sử dụng trực tiếp.
 */
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Response interceptor: chuẩn hóa dữ liệu trả về và bắt lỗi
axiosClient.interceptors.response.use(
  (response) => {
    // Trả về dữ liệu payload từ backend ApiResponse (thường là response.data)
    return response.data;
  },
  (error) => {
    // Không nuốt lỗi silently — chuyển tiếp lỗi để component hoặc hook xử lý
    const errorMessage =
      error.response?.data?.message ||
      error.message ||
      'Đã xảy ra lỗi khi kết nối tới máy chủ backend';

    console.error('[API Error]:', errorMessage, error.response?.data);
    return Promise.reject(error);
  }
);

export default axiosClient;
