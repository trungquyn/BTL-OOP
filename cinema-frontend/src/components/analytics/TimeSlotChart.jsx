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

const SLOT_COLORS = {
  Morning: '#38bdf8',
  Afternoon: '#3b82f6',
  Evening: '#f59e0b',
  Night: '#6366f1'
};

const SLOT_VI_NAMES = {
  Morning: 'Sáng',
  Afternoon: 'Chiều',
  Evening: 'Tối',
  Night: 'Khuya'
};

/**
 * TimeSlotChart Component — Biểu đồ phân tích tỷ lệ lấp đầy theo 4 khung giờ:
 * - Morning: 06:00–11:59
 * - Afternoon: 12:00–16:59
 * - Evening: 17:00–20:59
 * - Night: 21:00–23:59
 *
 * Props:
 * - data: Danh sách TimeSlotOccupancyResponse
 * - loading: boolean
 * - error: string | null
 * - onRetry: callback
 */
export default function TimeSlotChart({ data = [], loading = false, error = null, onRetry }) {
  if (loading) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Tỷ lệ lấp đầy theo khung giờ</h3>
        <div className="state-box">Đang tải dữ liệu khung giờ...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Tỷ lệ lấp đầy theo khung giờ</h3>
        <div className="state-box">
          <p style={{ color: '#ef4444' }}>Không thể tải dữ liệu khung giờ.</p>
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
        <h3 className="card-title">Tỷ lệ lấp đầy theo khung giờ</h3>
        <div className="state-box">Không có dữ liệu trong khoảng thời gian này.</div>
      </div>
    );
  }

  // Thứ tự thời gian cố định: Morning -> Afternoon -> Evening -> Night (không sort theo occupancy)
  const order = ['Morning', 'Afternoon', 'Evening', 'Night'];
  const sortedData = [...data].sort((a, b) => {
    return order.indexOf(a.slotName) - order.indexOf(b.slotName);
  });

  const chartData = sortedData.map((item) => ({
    slotName: item.slotName,
    displayName: `${SLOT_VI_NAMES[item.slotName] || item.slotName} (${item.timeRange})`,
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
          <div style={{ fontWeight: 600, color: '#1e293b', marginBottom: '4px' }}>{d.displayName}</div>
          <div style={{ color: '#f59e0b', fontWeight: 600 }}>Tỷ lệ lấp đầy: {d.occupancyPercent}%</div>
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
        <span>Tỷ lệ lấp đầy theo khung giờ (Time Slots)</span>
      </h3>
      <div style={{ width: '100%', height: 300 }}>
        <ResponsiveContainer>
          <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
            <XAxis
              dataKey="displayName"
              tick={{ fontSize: 12, fill: '#475569' }}
            />
            <YAxis
              tick={{ fontSize: 12, fill: '#64748b' }}
              domain={[0, 100]}
              tickFormatter={(v) => `${v}%`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Bar dataKey="occupancyPercent" name="Tỷ lệ lấp đầy (%)" radius={[6, 6, 0, 0]}>
              {chartData.map((entry) => (
                <Cell
                  key={`slot-${entry.slotName}`}
                  fill={SLOT_COLORS[entry.slotName] || '#3b82f6'}
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
