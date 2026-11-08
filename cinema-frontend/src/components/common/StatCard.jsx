import React from 'react';

/**
 * StatCard Component — Hiển thị chỉ số KPI tổng quan.
 *
 * Props:
 * - title: Tiêu đề chỉ số
 * - value: Giá trị hiển thị
 * - subtext: Ghi chú phụ bên dưới (optional)
 * - icon: Icon component (từ lucide-react)
 * - color: Mã màu nhấn (optional, mặc định xanh primary)
 */
export default function StatCard({ title, value, subtext, icon: Icon, color = '#3b82f6' }) {
  return (
    <div style={{
      background: '#ffffff',
      border: '1px solid #e2e8f0',
      borderRadius: '10px',
      padding: '18px 20px',
      boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
      display: 'flex',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      position: 'relative',
      overflow: 'hidden'
    }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '8px' }}>
          {title}
        </div>
        <div style={{ fontSize: '1.65rem', fontWeight: 700, color: '#0f172a', lineHeight: 1.2 }}>
          {value !== undefined && value !== null ? value : '-'}
        </div>
        {subtext && (
          <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '6px' }}>
            {subtext}
          </div>
        )}
      </div>

      {Icon && (
        <div style={{
          width: '42px',
          height: '42px',
          borderRadius: '8px',
          backgroundColor: `${color}15`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: color
        }}>
          <Icon size={22} />
        </div>
      )}
    </div>
  );
}
