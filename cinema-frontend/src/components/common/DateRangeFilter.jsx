import React, { useState } from 'react';
import { Calendar, Filter, RotateCcw } from 'lucide-react';

/**
 * DateRangeFilter Component — Bộ lọc khoảng ngày (YYYY-MM-DD).
 *
 * Props:
 * - initialStartDate: giá trị khởi tạo startDate (optional)
 * - initialEndDate: giá trị khởi tạo endDate (optional)
 * - onApply: callback khi bấm nút Áp dụng ({ startDate, endDate })
 * - onReset: callback khi bấm nút Đặt lại ()
 */
export default function DateRangeFilter({
  initialStartDate = '',
  initialEndDate = '',
  onApply,
  onReset
}) {
  const [startDate, setStartDate] = useState(initialStartDate);
  const [endDate, setEndDate] = useState(initialEndDate);

  const handleApply = (e) => {
    e.preventDefault();
    if (onApply) {
      onApply({
        startDate: startDate ? startDate : undefined,
        endDate: endDate ? endDate : undefined
      });
    }
  };

  const handleReset = () => {
    setStartDate('');
    setEndDate('');
    if (onReset) {
      onReset();
    }
  };

  return (
    <div style={{
      background: '#ffffff',
      border: '1px solid #e2e8f0',
      borderRadius: '10px',
      padding: '14px 18px',
      marginBottom: '24px',
      display: 'flex',
      flexWrap: 'wrap',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: '12px',
      boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#475569', fontWeight: 600, fontSize: '0.9rem' }}>
        <Calendar size={18} color="#3b82f6" />
        <span>Khoảng thời gian:</span>
      </div>

      <form onSubmit={handleApply} style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <label style={{ fontSize: '0.85rem', color: '#64748b' }}>Từ:</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            style={{
              padding: '6px 10px',
              borderRadius: '6px',
              border: '1px solid #cbd5e1',
              fontSize: '0.85rem',
              color: '#1e293b'
            }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <label style={{ fontSize: '0.85rem', color: '#64748b' }}>Đến:</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            style={{
              padding: '6px 10px',
              borderRadius: '6px',
              border: '1px solid #cbd5e1',
              fontSize: '0.85rem',
              color: '#1e293b'
            }}
          />
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            type="submit"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '6px 14px',
              background: '#3b82f6',
              color: '#ffffff',
              border: 'none',
              borderRadius: '6px',
              fontWeight: 500,
              fontSize: '0.85rem',
              cursor: 'pointer'
            }}
          >
            <Filter size={14} />
            Áp dụng
          </button>

          <button
            type="button"
            onClick={handleReset}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '6px 14px',
              background: '#f1f5f9',
              color: '#475569',
              border: '1px solid #cbd5e1',
              borderRadius: '6px',
              fontWeight: 500,
              fontSize: '0.85rem',
              cursor: 'pointer'
            }}
          >
            <RotateCcw size={14} />
            Đặt lại
          </button>
        </div>
      </form>
    </div>
  );
}
