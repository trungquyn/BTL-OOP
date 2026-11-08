import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Cell
} from 'recharts';

/**
 * DayOfWeekChart Component — Biểu đồ tỷ lệ lấp đầy theo thứ trong tuần (Thứ 2 - Chủ nhật).
 *
 * Props:
 * - data: Danh sách DayOfWeekOccupancyResponse từ API
 * - loading: boolean
 * - error: string | null
 * - onRetry: callback
 */
export default function DayOfWeekChart({ data = [], loading = false, error = null, onRetry }) {
  if (loading) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Tỷ lệ lấp đầy theo thứ trong tuần</h3>
        <div className="state-box">Đang tải dữ liệu theo thứ...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Tỷ lệ lấp đầy theo thứ trong tuần</h3>
        <div className="state-box">
          <p style={{ color: '#ef4444' }}>Không thể tải dữ liệu theo thứ.</p>
          {onRetry && (
            <button className="btn-retry" onClick={onRetry}>
              Thử lại
            </button>
          )}
        </div>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Tỷ lệ lấp đầy theo thứ trong tuần</h3>
        <div className="state-box">Không có dữ liệu trong khoảng thời gian này.</div>
      </div>
    );
  }

  // Sắp xếp đúng thứ tự từ Thứ 2 (1) đến Chủ nhật (7), không đảo thứ tự
  const sortedData = [...data].sort((a, b) => (a.dayOfWeek || 0) - (b.dayOfWeek || 0));

  const chartData = sortedData.map((item) => ({
    dayOfWeek: item.dayOfWeek,
    dayLabel: item.dayLabel || `Thứ ${item.dayOfWeek}`,
    occupancyPercent: Number(((item.avgOccupancyRate || 0) * 100).toFixed(1)),
    totalShowtimes: item.totalShowtimes || 0,
    totalBooked: item.totalBooked || 0,
    totalCapacity: item.totalCapacity || 0
  }));

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const d = payload[0].payload;
      return (
        <div style={{
          background: '#ffffff',
          border: '1px solid #cbd5e1',
          padding: '10px 14px',
          borderRadius: '8px',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
          fontSize: '0.85rem'
        }}>
          <div style={{ fontWeight: 600, color: '#1e293b', marginBottom: '4px' }}>{d.dayLabel}</div>
          <div style={{ color: '#0ea5e9', fontWeight: 600 }}>Tỷ lệ lấp đầy: {d.occupancyPercent}%</div>
          <div style={{ color: '#64748b' }}>Vé đã bán: {d.totalBooked} / {d.totalCapacity}</div>
          <div style={{ color: '#64748b' }}>Số suất chiếu: {d.totalShowtimes}</div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="analytics-card">
      <h3 className="card-title">
        <span>Tỷ lệ lấp đầy theo ngày trong tuần (Day of Week)</span>
      </h3>
      <div style={{ width: '100%', height: 300 }}>
        <ResponsiveContainer>
          <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
            <XAxis
              dataKey="dayLabel"
              tick={{ fontSize: 12, fill: '#475569' }}
            />
            <YAxis
              tick={{ fontSize: 12, fill: '#64748b' }}
              domain={[0, 100]}
              tickFormatter={(v) => `${v}%`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Bar dataKey="occupancyPercent" name="Tỷ lệ lấp đầy (%)" radius={[6, 6, 0, 0]}>
              {chartData.map((entry) => {
                // Nhấn màu nổi bật hơn cho cuối tuần (Thứ 7, CN)
                const isWeekend = entry.dayOfWeek === 6 || entry.dayOfWeek === 7;
                return (
                  <Cell
                    key={`dow-${entry.dayOfWeek}`}
                    fill={isWeekend ? '#8b5cf6' : '#0ea5e9'}
                  />
                );
              })}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
