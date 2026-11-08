import React from 'react';
import { Routes, Route, Navigate, Link } from 'react-router-dom';
import AnalyticsDashboard from '../pages/AnalyticsDashboard';
import MovieAnalyticsPage from '../pages/MovieAnalyticsPage';
import ShowtimeAnalyticsPage from '../pages/ShowtimeAnalyticsPage';

/**
 * Định tuyến cho phân hệ Analytics của Người 1.
 * Thiết kế theo dạng module hóa (/analytics/*) để sau này Người 3 dễ dàng gắn vào layout chung.
 */
export default function AppRoutes() {
  return (
    <div>
      {/* Navigation bar tối giản để chuyển đổi giữa các trang Analytics */}
      <nav style={{
        padding: '12px 24px',
        backgroundColor: '#1f2937',
        color: '#f9fafb',
        display: 'flex',
        gap: '20px',
        alignItems: 'center',
        fontFamily: 'system-ui, sans-serif'
      }}>
        <strong style={{ marginRight: '12px' }}>🎬 Cinema Analytics (Module 1)</strong>
        <Link to="/analytics" style={{ color: '#60a5fa', textDecoration: 'none' }}>
          Dashboard
        </Link>
        <Link to="/analytics/movies" style={{ color: '#60a5fa', textDecoration: 'none' }}>
          Movie Analytics
        </Link>
        <Link to="/analytics/showtimes" style={{ color: '#60a5fa', textDecoration: 'none' }}>
          Showtime Analytics
        </Link>
      </nav>

      {/* Cấu hình các route */}
      <Routes>
        <Route path="/" element={<Navigate to="/analytics" replace />} />
        <Route path="/analytics" element={<AnalyticsDashboard />} />
        <Route path="/analytics/movies" element={<MovieAnalyticsPage />} />
        <Route path="/analytics/showtimes" element={<ShowtimeAnalyticsPage />} />
        <Route path="*" element={<div style={{ padding: '24px' }}><h3>404 - Trang không tồn tại</h3></div>} />
      </Routes>
    </div>
  );
}
