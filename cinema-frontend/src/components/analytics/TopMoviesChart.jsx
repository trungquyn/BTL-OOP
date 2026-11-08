import React, { useState } from 'react';
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

const COLORS = ['#3b82f6', '#06b6d4', '#10b981', '#f59e0b', '#8b5cf6'];

/**
 * TopMoviesChart Component — Biểu đồ cột hiển thị Top phim có occupancy hoặc số suất chiếu cao nhất.
 *
 * Props:
 * - data: Danh sách MovieStatsResponse
 * - loading: boolean
 * - error: string | null
 * - onByChange: callback khi người dùng đổi tiêu chí (occupancy | showtimes)
 * - currentBy: 'occupancy' | 'showtimes'
 */
export default function TopMoviesChart({
  data = [],
  loading = false,
  error = null,
  onByChange,
  currentBy = 'occupancy'
}) {
  const [metric, setMetric] = useState(currentBy);

  const handleMetricToggle = (newMetric) => {
    setMetric(newMetric);
    if (onByChange) {
      onByChange(newMetric);
    }
  };

  if (loading) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Top phim nổi bật</h3>
        <div className="state-box">Đang tải dữ liệu phim...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Top phim nổi bật</h3>
        <div className="state-box">
          <p style={{ color: '#ef4444' }}>Không thể tải danh sách phim.</p>
        </div>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Top phim nổi bật</h3>
        <div className="state-box">Không có dữ liệu trong khoảng thời gian này.</div>
      </div>
    );
  }

  const chartData = data.slice(0, 5).map((item) => ({
    name: item.movieTitle || 'Không rõ',
    occupancyPercent: Number(((item.avgOccupancyRate || 0) * 100).toFixed(1)),
    totalShowtimes: item.totalShowtimes || 0,
    totalBookedSeats: item.totalBookedSeats || 0,
    totalCapacity: item.totalCapacity || 0
  }));

  const isOccupancy = metric === 'occupancy';

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
          <div style={{ fontWeight: 600, color: '#1e293b', marginBottom: '4px' }}>{d.name}</div>
          <div style={{ color: '#3b82f6', fontWeight: 600 }}>Tỷ lệ lấp đầy: {d.occupancyPercent}%</div>
          <div style={{ color: '#64748b' }}>Tổng suất chiếu: {d.totalShowtimes}</div>
          <div style={{ color: '#64748b' }}>Vé đã bán: {d.totalBookedSeats} / {d.totalCapacity}</div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="analytics-card">
      <div className="card-title">
        <span>Top 5 phim hàng đầu</span>
        <div style={{ display: 'flex', gap: '6px' }}>
          <button
            type="button"
            onClick={() => handleMetricToggle('occupancy')}
            style={{
              padding: '4px 10px',
              fontSize: '0.75rem',
              borderRadius: '5px',
              border: '1px solid #cbd5e1',
              cursor: 'pointer',
              background: isOccupancy ? '#3b82f6' : '#f8fafc',
              color: isOccupancy ? '#ffffff' : '#475569',
              fontWeight: 500
            }}
          >
            Theo tỷ lệ lấp đầy
          </button>
          <button
            type="button"
            onClick={() => handleMetricToggle('showtimes')}
            style={{
              padding: '4px 10px',
              fontSize: '0.75rem',
              borderRadius: '5px',
              border: '1px solid #cbd5e1',
              cursor: 'pointer',
              background: !isOccupancy ? '#3b82f6' : '#f8fafc',
              color: !isOccupancy ? '#ffffff' : '#475569',
              fontWeight: 500
            }}
          >
            Theo số suất chiếu
          </button>
        </div>
      </div>

      <div style={{ width: '100%', height: 300 }}>
        <ResponsiveContainer>
          <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 40 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
            <XAxis
              dataKey="name"
              tick={{ fontSize: 11, fill: '#475569' }}
              interval={0}
              angle={-20}
              textAnchor="end"
              height={60}
            />
            <YAxis
              tick={{ fontSize: 12, fill: '#64748b' }}
              domain={isOccupancy ? [0, 100] : [0, 'auto']}
              tickFormatter={(v) => (isOccupancy ? `${v}%` : v)}
            />
            <Tooltip content={<CustomTooltip />} />
            <Bar
              dataKey={isOccupancy ? 'occupancyPercent' : 'totalShowtimes'}
              radius={[6, 6, 0, 0]}
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
