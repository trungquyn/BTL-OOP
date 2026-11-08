import React from 'react';

const DAYS = [
  { dow: 1, label: 'Thứ 2' },
  { dow: 2, label: 'Thứ 3' },
  { dow: 3, label: 'Thứ 4' },
  { dow: 4, label: 'Thứ 5' },
  { dow: 5, label: 'Thứ 6' },
  { dow: 6, label: 'Thứ 7' },
  { dow: 7, label: 'Chủ nhật' }
];

/**
 * Trả về màu nền và màu chữ cho ô heatmap dựa trên occupancy rate (0.0 - 1.0).
 */
function getHeatmapColor(rate) {
  if (rate === null || rate === undefined) {
    return { bg: '#f8fafc', color: '#94a3b8', border: '#e2e8f0' }; // Ô trống
  }
  if (rate >= 0.8) {
    // High demand (>=80%): Đỏ / Đỏ cam đậm
    return { bg: '#fee2e2', color: '#b91c1c', border: '#fca5a5' };
  }
  if (rate >= 0.6) {
    // Medium-high: Cam
    return { bg: '#ffedd5', color: '#c2410c', border: '#fdba74' };
  }
  if (rate >= 0.4) {
    // Medium demand (40-59%): Vàng
    return { bg: '#fef3c7', color: '#b45309', border: '#fde68a' };
  }
  if (rate >= 0.2) {
    // Medium-low: Xanh lục nhạt
    return { bg: '#dcfce7', color: '#15803d', border: '#bbf7d0' };
  }
  // Low demand (<20%): Xám xanh rất nhạt
  return { bg: '#f0fdf4', color: '#166534', border: '#dcfce7' };
}

/**
 * OccupancyHeatmap Component — Ma trận nhiệt 2D (Thứ trong tuần × Khung giờ bắt đầu).
 *
 * Props:
 * - data: Danh sách HeatmapCellResponse từ API
 * - loading: boolean
 * - error: string | null
 * - onRetry: callback
 */
export default function OccupancyHeatmap({ data = [], loading = false, error = null, onRetry }) {
  if (loading) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Ma trận nhiệt tỷ lệ lấp đầy (Occupancy Heatmap)</h3>
        <div className="state-box">Đang tải ma trận nhiệt...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-card">
        <h3 className="card-title">Ma trận nhiệt tỷ lệ lấp đầy (Occupancy Heatmap)</h3>
        <div className="state-box">
          <p style={{ color: '#ef4444' }}>Không thể tải dữ liệu Heatmap.</p>
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
        <h3 className="card-title">Ma trận nhiệt tỷ lệ lấp đầy (Occupancy Heatmap)</h3>
        <div className="state-box">Không có dữ liệu trong khoảng thời gian này.</div>
      </div>
    );
  }

  // 1. Trích xuất danh sách giờ thực tế xuất hiện trong dữ liệu (không hard-code)
  const uniqueHours = Array.from(new Set(data.map((c) => c.hour))).sort((a, b) => a - b);

  // 2. Tạo lookup map: `dayOfWeek_hour` -> cell data
  const cellMap = new Map();
  data.forEach((c) => {
    cellMap.set(`${c.dayOfWeek}_${c.hour}`, c);
  });

  return (
    <div className="analytics-card">
      <div className="card-title">
        <span>Ma trận nhiệt lấp đầy: Thứ trong tuần × Giờ chiếu (Heatmap Grid)</span>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center', fontSize: '0.75rem', color: '#64748b' }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 12, height: 12, borderRadius: 2, background: '#f0fdf4', border: '1px solid #dcfce7' }}></span>
            &lt;40%
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 12, height: 12, borderRadius: 2, background: '#fef3c7', border: '1px solid #fde68a' }}></span>
            40-79%
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 12, height: 12, borderRadius: 2, background: '#fee2e2', border: '1px solid #fca5a5' }}></span>
            ≥80%
          </span>
        </div>
      </div>

      <div style={{ overflowX: 'auto', paddingBottom: '8px' }}>
        <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: '4px', minWidth: '600px' }}>
          <thead>
            <tr>
              <th style={{ padding: '8px', textAlign: 'left', fontSize: '0.8rem', color: '#64748b', fontWeight: 600 }}>
                Thứ \ Giờ
              </th>
              {uniqueHours.map((hour) => (
                <th
                  key={`th-${hour}`}
                  style={{
                    padding: '8px 4px',
                    textAlign: 'center',
                    fontSize: '0.8rem',
                    color: '#475569',
                    fontWeight: 600
                  }}
                >
                  {String(hour).padStart(2, '0')}:00
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {DAYS.map((day) => (
              <tr key={`row-${day.dow}`}>
                <td style={{ padding: '6px 10px', fontSize: '0.85rem', fontWeight: 600, color: '#334155', whiteSpace: 'nowrap' }}>
                  {day.label}
                </td>
                {uniqueHours.map((hour) => {
                  const cell = cellMap.get(`${day.dow}_${hour}`);
                  const hasData = cell !== undefined && cell !== null;
                  const rate = hasData ? cell.avgOccupancyRate : null;
                  const colors = getHeatmapColor(rate);
                  const displayText = hasData ? `${(rate * 100).toFixed(0)}%` : '-';
                  const titleText = hasData
                    ? `${day.label} lúc ${String(hour).padStart(2, '0')}:00: Tỷ lệ lấp đầy ${(rate * 100).toFixed(1)}% (${cell.count || 0} suất)`
                    : `${day.label} lúc ${String(hour).padStart(2, '0')}:00: Chưa có suất chiếu`;

                  return (
                    <td
                      key={`td-${day.dow}-${hour}`}
                      title={titleText}
                      style={{
                        padding: '10px 4px',
                        textAlign: 'center',
                        fontSize: '0.8rem',
                        fontWeight: hasData ? 600 : 400,
                        backgroundColor: colors.bg,
                        color: colors.color,
                        border: `1px solid ${colors.border}`,
                        borderRadius: '6px',
                        cursor: hasData ? 'pointer' : 'default',
                        transition: 'transform 0.1s'
                      }}
                    >
                      {displayText}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
