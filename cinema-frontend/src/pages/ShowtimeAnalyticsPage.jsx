import React, { useState, useEffect, useCallback } from 'react';
import { CalendarCheck, Tv } from 'lucide-react';
import analyticsApi from '../api/analyticsApi';
import DateRangeFilter from '../components/common/DateRangeFilter';
import HighLowDemandList from '../components/analytics/HighLowDemandList';

/**
 * ShowtimeAnalyticsPage Component — Phân tích chi tiết suất chiếu.
 * Gọi:
 * - GET /api/analytics/showtimes/high-demand
 * - GET /api/analytics/showtimes/low-demand
 * - GET /api/analytics/showtimes/weekday-weekend
 * - GET /api/analytics/showtimes/by-format
 */
export default function ShowtimeAnalyticsPage() {
  const [dateFilter, setDateFilter] = useState({ startDate: undefined, endDate: undefined });

  const [highDemand, setHighDemand] = useState([]);
  const [lowDemand, setLowDemand] = useState([]);
  const [weekdayWeekend, setWeekdayWeekend] = useState(null);
  const [formatStats, setFormatStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async (params) => {
    setLoading(true);
    setError(null);
    try {
      const [highRes, lowRes, wwRes, fmtRes] = await Promise.all([
        analyticsApi.getHighDemandShowtimes({ ...params, limit: 10 }),
        analyticsApi.getLowDemandShowtimes({ ...params, limit: 10 }),
        analyticsApi.getWeekdayWeekendStats(params),
        analyticsApi.getFormatStats(params)
      ]);

      setHighDemand(highRes?.data || highRes || []);
      setLowDemand(lowRes?.data || lowRes || []);
      setWeekdayWeekend(wwRes?.data || wwRes || null);
      setFormatStats(fmtRes?.data || fmtRes || []);
    } catch (err) {
      setError('Không thể tải dữ liệu phân tích suất chiếu.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData(dateFilter);
  }, [fetchData, dateFilter]);

  const handleApplyFilter = ({ startDate, endDate }) => {
    setDateFilter({ startDate, endDate });
  };

  const handleResetFilter = () => {
    setDateFilter({ startDate: undefined, endDate: undefined });
  };

  const weekdayData = weekdayWeekend?.weekday;
  const weekendData = weekdayWeekend?.weekend;

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Showtime Analytics</h1>
        <p className="dashboard-subtitle">
          Phân tích các suất chiếu cao điểm/thấp điểm, so sánh ngày thường vs cuối tuần và định dạng phòng chiếu
        </p>
      </div>

      <DateRangeFilter
        initialStartDate={dateFilter.startDate || ''}
        initialEndDate={dateFilter.endDate || ''}
        onApply={handleApplyFilter}
        onReset={handleResetFilter}
      />

      {/* ─── WEEKDAY VS WEEKEND & FORMAT DISTRIBUTION ───────────────────── */}
      <div className="chart-grid-2">
        {/* So sánh Ngày thường vs Cuối tuần */}
        <div className="analytics-card">
          <div className="card-title">
            <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CalendarCheck size={18} color="#3b82f6" />
              So sánh Ngày thường (Thứ 2 - Thứ 6) vs Cuối tuần (Thứ 7 - CN)
            </span>
          </div>

          {loading ? (
            <div className="state-box">Đang tải số liệu ngày thường / cuối tuần...</div>
          ) : error ? (
            <div className="state-box"><p style={{ color: '#ef4444' }}>{error}</p></div>
          ) : !weekdayData && !weekendData ? (
            <div className="state-box">Không có dữ liệu trong khoảng thời gian này.</div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '12px' }}>
              <div style={{ background: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>Ngày thường (Thứ 2 - Thứ 6)</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#3b82f6', marginTop: '6px' }}>
                  {((weekdayData?.avgOccupancyRate || 0) * 100).toFixed(1)}%
                </div>
                <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>
                  Số suất: {weekdayData?.totalShowtimes || 0} suất
                </div>
                <div style={{ fontSize: '0.8rem', color: '#64748b' }}>
                  Vé đã đặt: {(weekdayData?.totalBooked || 0).toLocaleString()} / {(weekdayData?.totalCapacity || 0).toLocaleString()}
                </div>
              </div>

              <div style={{ background: '#faf5ff', padding: '16px', borderRadius: '8px', border: '1px solid #e9d5ff' }}>
                <div style={{ fontSize: '0.85rem', color: '#7e22ce', fontWeight: 600 }}>Cuối tuần (Thứ 7 - Chủ nhật)</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#9333ea', marginTop: '6px' }}>
                  {((weekendData?.avgOccupancyRate || 0) * 100).toFixed(1)}%
                </div>
                <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>
                  Số suất: {weekendData?.totalShowtimes || 0} suất
                </div>
                <div style={{ fontSize: '0.8rem', color: '#64748b' }}>
                  Vé đã đặt: {(weekendData?.totalBooked || 0).toLocaleString()} / {(weekendData?.totalCapacity || 0).toLocaleString()}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Phân bố theo định dạng (Format distribution) */}
        <div className="analytics-card">
          <div className="card-title">
            <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Tv size={18} color="#06b6d4" />
              Thống kê theo định dạng phòng chiếu (Format Stats)
            </span>
          </div>

          {loading ? (
            <div className="state-box">Đang tải số liệu định dạng...</div>
          ) : error ? (
            <div className="state-box"><p style={{ color: '#ef4444' }}>{error}</p></div>
          ) : formatStats.length === 0 ? (
            <div className="state-box">Không có dữ liệu định dạng trong khoảng thời gian này.</div>
          ) : (
            <div style={{ overflowX: 'auto', marginTop: '8px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #e2e8f0', color: '#64748b', textAlign: 'left' }}>
                    <th style={{ padding: '8px 10px' }}>Định dạng</th>
                    <th style={{ padding: '8px 10px', textAlign: 'center' }}>Số suất chiếu</th>
                    <th style={{ padding: '8px 10px', textAlign: 'center' }}>Vé đã bán</th>
                    <th style={{ padding: '8px 10px', textAlign: 'right' }}>Occupancy TB</th>
                  </tr>
                </thead>
                <tbody>
                  {formatStats.map((fmt, idx) => (
                    <tr key={fmt.format || `fmt-${idx}`} style={{ borderBottom: '1px solid #f1f5f9' }}>
                      <td style={{ padding: '10px', fontWeight: 600, color: '#1e293b' }}>
                        {fmt.format ? fmt.format.replace('_', '') : 'Tiêu chuẩn'}
                      </td>
                      <td style={{ padding: '10px', textAlign: 'center', color: '#475569' }}>
                        {fmt.totalShowtimes || 0}
                      </td>
                      <td style={{ padding: '10px', textAlign: 'center', color: '#475569' }}>
                        {fmt.totalBooked?.toLocaleString() || 0}
                      </td>
                      <td style={{ padding: '10px', textAlign: 'right', fontWeight: 600, color: '#06b6d4' }}>
                        {((fmt.avgOccupancyRate || 0) * 100).toFixed(1)}%
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* ─── HIGH / LOW DEMAND SHOWTIMES LIST ───────────────────────────── */}
      <HighLowDemandList
        highDemand={highDemand}
        lowDemand={lowDemand}
        loading={loading}
        error={error}
      />
    </div>
  );
}
