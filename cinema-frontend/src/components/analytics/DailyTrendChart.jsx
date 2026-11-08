import React from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from 'recharts';

/**
 * DailyTrendChart Component — Biểu đồ đường xu hướng tỷ lệ lấp đầy theo ngày.
 *
 * Props:
 * - data: Danh sách DailyOccupancyResponse từ API
 * - loading: boolean
 * - error: string | null
 * - onRetry: callback
 */
export default function DailyTrendChart({ data = [], loading = false, error = null, onRetry }) {
  if (loading) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Xu hướng tỷ lệ lấp đầy theo ngày</h3>
        <div className="state-box">Đang tải dữ liệu biểu đồ...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Xu hướng tỷ lệ lấp đầy theo ngày</h3>
        <div className="state-box">
          <p style={{ color: '#ef4444' }}>Không thể tải dữ liệu xu hướng.</p>
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
        <h3 className="card-title">Xu hướng tỷ lệ lấp đầy theo ngày</h3>
        <div className="state-box">Không có dữ liệu trong khoảng thời gian này.</div>
      </div>
    );
  }

  // Chuẩn hóa dữ liệu hiển thị (chuyển 0.72 -> 72.0%)
  const chartData = data.map((item) => ({
    date: item.date,
    occupancyPercent: Number(((item.avgOccupancyRate || 0) * 100).toFixed(1)),
    totalBooked: item.totalBooked || 0,
    totalCapacity: item.totalCapacity || 0,
    totalShowtimes: item.totalShowtimes || 0
  }));

  const CustomTooltip = ({ active, payload, label }) => {
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
          <div style={{ fontWeight: 600, color: '#1e293b', marginBottom: '4px' }}>Ngày: {label}</div>
          <div style={{ color: '#3b82f6', fontWeight: 600 }}>Tỷ lệ lấp đầy: {d.occupancyPercent}%</div>
          <div style={{ color: '#64748b' }}>Vé đã bán: {d.totalBooked} / {d.totalCapacity} ghế</div>
          <div style={{ color: '#64748b' }}>Số suất chiếu: {d.totalShowtimes}</div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="analytics-card">
      <h3 className="card-title">
        <span>Xu hướng tỷ lệ lấp đầy theo ngày (Daily Occupancy Trend)</span>
      </h3>
      <div style={{ width: '100%', height: 320 }}>
        <ResponsiveContainer>
          <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 25 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 12, fill: '#64748b' }}
              angle={-25}
              textAnchor="end"
              height={50}
            />
            <YAxis
              tick={{ fontSize: 12, fill: '#64748b' }}
              domain={[0, 100]}
              tickFormatter={(v) => `${v}%`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend verticalAlign="top" height={36} />
            <Line
              type="monotone"
              dataKey="occupancyPercent"
              name="Tỷ lệ lấp đầy (%)"
              stroke="#3b82f6"
              strokeWidth={2.5}
              dot={{ r: 3, fill: '#3b82f6' }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
