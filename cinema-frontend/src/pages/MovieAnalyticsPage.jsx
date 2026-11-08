import React, { useState, useEffect, useCallback } from 'react';
import analyticsApi from '../api/analyticsApi';
import DateRangeFilter from '../components/common/DateRangeFilter';
import TopMoviesChart from '../components/analytics/TopMoviesChart';

/**
 * MovieAnalyticsPage Component — Phân tích chi tiết theo từng bộ phim.
 * Gọi GET /api/analytics/movies và GET /api/analytics/movies/top.
 */
export default function MovieAnalyticsPage() {
  const [dateFilter, setDateFilter] = useState({ startDate: undefined, endDate: undefined });
  const [topBy, setTopBy] = useState('occupancy');

  const [moviesList, setMoviesList] = useState([]);
  const [topMovies, setTopMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async (params, by) => {
    setLoading(true);
    setError(null);
    try {
      const [allRes, topRes] = await Promise.all([
        analyticsApi.getMovieAnalytics(params),
        analyticsApi.getTopMovies({ ...params, by, limit: 5 })
      ]);
      setMoviesList(allRes?.data || allRes || []);
      setTopMovies(topRes?.data || topRes || []);
    } catch (err) {
      setError('Không thể tải dữ liệu thống kê phim từ backend.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData(dateFilter, topBy);
  }, [fetchData, dateFilter, topBy]);

  const handleApplyFilter = ({ startDate, endDate }) => {
    setDateFilter({ startDate, endDate });
  };

  const handleResetFilter = () => {
    setDateFilter({ startDate: undefined, endDate: undefined });
  };

  const handleTopByChange = (newBy) => {
    setTopBy(newBy);
    fetchData(dateFilter, newBy);
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Movie Analytics</h1>
        <p className="dashboard-subtitle">
          Thống kê chi tiết số suất chiếu, ghế đã bán và tỷ lệ lấp đầy theo từng bộ phim
        </p>
      </div>

      <DateRangeFilter
        initialStartDate={dateFilter.startDate || ''}
        initialEndDate={dateFilter.endDate || ''}
        onApply={handleApplyFilter}
        onReset={handleResetFilter}
      />

      {/* Top 5 Movies Chart */}
      <TopMoviesChart
        data={topMovies}
        loading={loading}
        error={error}
        currentBy={topBy}
        onByChange={handleTopByChange}
      />

      {/* Movies Table */}
      <div className="analytics-card">
        <h3 className="card-title">Bảng thống kê chi tiết theo phim</h3>

        {loading ? (
          <div className="state-box">Đang tải bảng thống kê phim...</div>
        ) : error ? (
          <div className="state-box">
            <p style={{ color: '#ef4444' }}>{error}</p>
            <button className="btn-retry" onClick={() => fetchData(dateFilter, topBy)}>
              Thử lại
            </button>
          </div>
        ) : moviesList.length === 0 ? (
          <div className="state-box">Không có dữ liệu phim trong khoảng thời gian này.</div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #e2e8f0', color: '#64748b', textAlign: 'left' }}>
                  <th style={{ padding: '10px' }}>Tên phim</th>
                  <th style={{ padding: '10px', textAlign: 'center' }}>Số suất chiếu</th>
                  <th style={{ padding: '10px', textAlign: 'center' }}>Số vé đã đặt</th>
                  <th style={{ padding: '10px', textAlign: 'center' }}>Tổng sức chứa</th>
                  <th style={{ padding: '10px', textAlign: 'center' }}>Tỷ lệ lấp đầy TB</th>
                  <th style={{ padding: '10px', textAlign: 'center' }}>Cao nhất</th>
                  <th style={{ padding: '10px', textAlign: 'center' }}>Thấp nhất</th>
                  <th style={{ padding: '10px', textAlign: 'right' }}>Doanh thu (Người 2)</th>
                </tr>
              </thead>
              <tbody>
                {moviesList.map((m, idx) => {
                  const avgPercent = Number(((m.avgOccupancyRate || 0) * 100).toFixed(1));
                  const maxPercent = Number(((m.maxOccupancyRate || 0) * 100).toFixed(1));
                  const minPercent = Number(((m.minOccupancyRate || 0) * 100).toFixed(1));
                  const revenueDisplay = m.revenue !== null && m.revenue !== undefined
                    ? `${m.revenue.toLocaleString()} đ`
                    : 'N/A';

                  return (
                    <tr
                      key={m.movieId || `m-${idx}`}
                      style={{
                        borderBottom: '1px solid #f1f5f9',
                        backgroundColor: idx % 2 === 0 ? '#ffffff' : '#f8fafc'
                      }}
                    >
                      <td style={{ padding: '12px 10px', fontWeight: 600, color: '#1e293b' }}>
                        {m.movieTitle || 'Chưa rõ'}
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'center', color: '#475569' }}>
                        {m.totalShowtimes || 0}
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'center', color: '#475569' }}>
                        {m.totalBookedSeats?.toLocaleString() || 0}
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'center', color: '#475569' }}>
                        {m.totalCapacity?.toLocaleString() || 0}
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'center', fontWeight: 600, color: '#3b82f6' }}>
                        {avgPercent}%
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'center', color: '#ef4444' }}>
                        {maxPercent}%
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'center', color: '#64748b' }}>
                        {minPercent}%
                      </td>
                      <td style={{ padding: '12px 10px', textAlign: 'right', color: '#94a3b8', fontStyle: 'italic' }}>
                        {revenueDisplay}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
