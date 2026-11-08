import React from 'react';
import { Flame, Snowflake } from 'lucide-react';

/**
 * HighLowDemandList Component — Danh sách các suất chiếu có nhu cầu Cao (>=80%) và Thấp (<40%).
 *
 * Props:
 * - highDemand: Danh sách ShowtimeAnalyticsResponse (High demand)
 * - lowDemand: Danh sách ShowtimeAnalyticsResponse (Low demand)
 * - loading: boolean
 * - error: string | null
 */
export default function HighLowDemandList({
  highDemand = [],
  lowDemand = [],
  loading = false,
  error = null
}) {
  if (loading) {
    return (
      <div className="chart-grid-2">
        <div className="analytics-card">
          <h3 className="card-title">Suất chiếu cao điểm (High Demand)</h3>
          <div className="state-box">Đang tải suất chiếu cao điểm...</div>
        </div>
        <div className="analytics-card">
          <h3 className="card-title">Suất chiếu thấp điểm (Low Demand)</h3>
          <div className="state-box">Đang tải suất chiếu thấp điểm...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Phân tích suất chiếu theo nhu cầu</h3>
        <div className="state-box">
          <p style={{ color: '#ef4444' }}>Không thể tải danh sách suất chiếu.</p>
        </div>
      </div>
    );
  }

  const renderTable = (items, emptyText, isHigh) => {
    if (!items || items.length === 0) {
      return <div className="state-box" style={{ padding: '24px 0' }}>{emptyText}</div>;
    }

    return (
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #e2e8f0', color: '#64748b', textAlign: 'left' }}>
              <th style={{ padding: '8px 10px' }}>Phim</th>
              <th style={{ padding: '8px 10px' }}>Ngày / Giờ</th>
              <th style={{ padding: '8px 10px' }}>Phòng</th>
              <th style={{ padding: '8px 10px' }}>Số ghế</th>
              <th style={{ padding: '8px 10px', textAlign: 'right' }}>Occupancy</th>
            </tr>
          </thead>
          <tbody>
            {items.map((s, idx) => {
              const occPercent = Number(((s.occupancyRate || 0) * 100).toFixed(1));
              return (
                <tr
                  key={s.showtimeId || `st-${idx}`}
                  style={{
                    borderBottom: '1px solid #f1f5f9',
                    backgroundColor: idx % 2 === 0 ? '#ffffff' : '#f8fafc'
                  }}
                >
                  <td style={{ padding: '10px', fontWeight: 600, color: '#1e293b' }}>
                    {s.movieTitle || 'Chưa rõ'}
                  </td>
                  <td style={{ padding: '10px', color: '#475569' }}>
                    <div>{s.showDate}</div>
                    <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{s.startTime} - {s.endTime}</div>
                  </td>
                  <td style={{ padding: '10px', color: '#475569' }}>
                    <span style={{ background: '#f1f5f9', padding: '2px 6px', borderRadius: 4 }}>
                      {s.roomId || 'N/A'}
                    </span>
                  </td>
                  <td style={{ padding: '10px', color: '#475569' }}>
                    {s.bookedSeats} / {s.totalSeats}
                  </td>
                  <td style={{ padding: '10px', textAlign: 'right' }}>
                    <span className={`badge-demand ${isHigh ? 'badge-high' : 'badge-low'}`}>
                      {occPercent}%
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div className="chart-grid-2">
      {/* HIGH DEMAND CARD */}
      <div className="analytics-card">
        <div className="card-title">
          <span style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#dc2626' }}>
            <Flame size={18} />
            Suất chiếu cao điểm (High Demand ≥ 80%)
          </span>
          <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Top 10 suất</span>
        </div>
        {renderTable(highDemand, 'Không có suất chiếu cao điểm trong giai đoạn này.', true)}
      </div>

      {/* LOW DEMAND CARD */}
      <div className="analytics-card">
        <div className="card-title">
          <span style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#059669' }}>
            <Snowflake size={18} />
            Suất chiếu vắng khách (Low Demand &lt; 40%)
          </span>
          <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Top 10 suất</span>
        </div>
        {renderTable(lowDemand, 'Không có suất chiếu vắng khách trong giai đoạn này.', false)}
      </div>
    </div>
  );
}
