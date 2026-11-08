import React, { useState, useEffect, useCallback } from 'react';
import { Film, Ticket, Percent, Flame, Snowflake, RefreshCw } from 'lucide-react';
import analyticsApi from '../api/analyticsApi';

import DateRangeFilter from '../components/common/DateRangeFilter';
import StatCard from '../components/common/StatCard';
import DailyTrendChart from '../components/analytics/DailyTrendChart';
import TopMoviesChart from '../components/analytics/TopMoviesChart';
import DayOfWeekChart from '../components/analytics/DayOfWeekChart';
import TimeSlotChart from '../components/analytics/TimeSlotChart';
import OccupancyHeatmap from '../components/analytics/OccupancyHeatmap';
import HighLowDemandList from '../components/analytics/HighLowDemandList';

/**
 * AnalyticsDashboard Page — Trang tổng quan chỉ số và tỷ lệ lấp đầy rạp chiếu.
 * Gọi API thật qua analyticsApi.js, xử lý loading, error và empty state toàn diện.
 */
export default function AnalyticsDashboard() {
  const [dateFilter, setDateFilter] = useState({ startDate: undefined, endDate: undefined });
  const [topBy, setTopBy] = useState('occupancy');

  // Loading states
  const [loadingSummary, setLoadingSummary] = useState(true);
  const [loadingDaily, setLoadingDaily] = useState(true);
  const [loadingTopMovies, setLoadingTopMovies] = useState(true);
  const [loadingDayOfWeek, setLoadingDayOfWeek] = useState(true);
  const [loadingTimeSlots, setLoadingTimeSlots] = useState(true);
  const [loadingHeatmap, setLoadingHeatmap] = useState(true);
  const [loadingDemand, setLoadingDemand] = useState(true);

  // Data states
  const [summaryData, setSummaryData] = useState(null);
  const [dailyData, setDailyData] = useState([]);
  const [topMoviesData, setTopMoviesData] = useState([]);
  const [dayOfWeekData, setDayOfWeekData] = useState([]);
  const [timeSlotsData, setTimeSlotsData] = useState([]);
  const [heatmapData, setHeatmapData] = useState([]);
  const [highDemandData, setHighDemandData] = useState([]);
  const [lowDemandData, setLowDemandData] = useState([]);

  // Error states
  const [errors, setErrors] = useState({});

  // ─── Fetch functions ──────────────────────────────────────────────────────

  const fetchSummary = useCallback(async (params) => {
    setLoadingSummary(true);
    try {
      const res = await analyticsApi.getOccupancySummary(params);
      setSummaryData(res?.data || res);
      setErrors((prev) => ({ ...prev, summary: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, summary: 'Lỗi tải tổng quan' }));
    } finally {
      setLoadingSummary(false);
    }
  }, []);

  const fetchDailyTrend = useCallback(async (params) => {
    setLoadingDaily(true);
    try {
      const res = await analyticsApi.getDailyOccupancy(params);
      setDailyData(res?.data || res || []);
      setErrors((prev) => ({ ...prev, daily: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, daily: 'Lỗi tải xu hướng' }));
    } finally {
      setLoadingDaily(false);
    }
  }, []);

  const fetchTopMovies = useCallback(async (params, by) => {
    setLoadingTopMovies(true);
    try {
      const res = await analyticsApi.getTopMovies({ ...params, by, limit: 5 });
      setTopMoviesData(res?.data || res || []);
      setErrors((prev) => ({ ...prev, topMovies: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, topMovies: 'Lỗi tải top phim' }));
    } finally {
      setLoadingTopMovies(false);
    }
  }, []);

  const fetchDayOfWeek = useCallback(async (params) => {
    setLoadingDayOfWeek(true);
    try {
      const res = await analyticsApi.getDayOfWeekOccupancy(params);
      setDayOfWeekData(res?.data || res || []);
      setErrors((prev) => ({ ...prev, dayOfWeek: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, dayOfWeek: 'Lỗi tải thống kê thứ' }));
    } finally {
      setLoadingDayOfWeek(false);
    }
  }, []);

  const fetchTimeSlots = useCallback(async (params) => {
    setLoadingTimeSlots(true);
    try {
      const res = await analyticsApi.getTimeSlotOccupancy(params);
      setTimeSlotsData(res?.data || res || []);
      setErrors((prev) => ({ ...prev, timeSlots: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, timeSlots: 'Lỗi tải khung giờ' }));
    } finally {
      setLoadingTimeSlots(false);
    }
  }, []);

  const fetchHeatmap = useCallback(async (params) => {
    setLoadingHeatmap(true);
    try {
      const res = await analyticsApi.getOccupancyHeatmap(params);
      setHeatmapData(res?.data || res || []);
      setErrors((prev) => ({ ...prev, heatmap: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, heatmap: 'Lỗi tải heatmap' }));
    } finally {
      setLoadingHeatmap(false);
    }
  }, []);

  const fetchDemand = useCallback(async (params) => {
    setLoadingDemand(true);
    try {
      const [highRes, lowRes] = await Promise.all([
        analyticsApi.getHighDemandShowtimes({ ...params, limit: 10 }),
        analyticsApi.getLowDemandShowtimes({ ...params, limit: 10 })
      ]);
      setHighDemandData(highRes?.data || highRes || []);
      setLowDemandData(lowRes?.data || lowRes || []);
      setErrors((prev) => ({ ...prev, demand: null }));
    } catch (err) {
      setErrors((prev) => ({ ...prev, demand: 'Lỗi tải danh sách nhu cầu' }));
    } finally {
      setLoadingDemand(false);
    }
  }, []);

  // Gọi tất cả khi dateFilter thay đổi
  const fetchAllData = useCallback(
    (params, by) => {
      fetchSummary(params);
      fetchDailyTrend(params);
      fetchTopMovies(params, by);
      fetchDayOfWeek(params);
      fetchTimeSlots(params);
      fetchHeatmap(params);
      fetchDemand(params);
    },
    [fetchSummary, fetchDailyTrend, fetchTopMovies, fetchDayOfWeek, fetchTimeSlots, fetchHeatmap, fetchDemand]
  );

  useEffect(() => {
    fetchAllData(dateFilter, topBy);
  }, [fetchAllData, dateFilter, topBy]);

  const handleApplyFilter = ({ startDate, endDate }) => {
    setDateFilter({ startDate, endDate });
  };

  const handleResetFilter = () => {
    setDateFilter({ startDate: undefined, endDate: undefined });
  };

  const handleTopByChange = (newBy) => {
    setTopBy(newBy);
    fetchTopMovies(dateFilter, newBy);
  };

  // Tính toán hiển thị cho StatCards
  const totalShowtimes = summaryData?.totalShowtimes ?? 0;
  const totalBooked = summaryData?.totalBooked ?? 0;
  const totalCapacity = summaryData?.totalCapacity ?? 0;
  const overallOccupancyPercent = summaryData?.overallOccupancyRate !== undefined && summaryData?.overallOccupancyRate !== null
    ? `${((summaryData.overallOccupancyRate || 0) * 100).toFixed(1)}%`
    : '-';
  const highDemandCount = summaryData?.highDemandCount ?? 0;
  const lowDemandCount = summaryData?.lowDemandCount ?? 0;

  return (
    <div className="dashboard-container">
      {/* ─── HEADER ──────────────────────────────────────────────────────── */}
      <div className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h1 className="dashboard-title">Movie &amp; Showtime Analytics Dashboard</h1>
          <p className="dashboard-subtitle">
            Phân tích tỷ lệ lấp đầy, số suất chiếu và hiệu suất phòng chiếu toàn hệ thống
          </p>
        </div>
        <button
          onClick={() => fetchAllData(dateFilter, topBy)}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '8px 14px',
            background: '#ffffff',
            border: '1px solid #cbd5e1',
            borderRadius: '6px',
            color: '#475569',
            fontSize: '0.85rem',
            fontWeight: 500,
            cursor: 'pointer'
          }}
        >
          <RefreshCw size={14} />
          Làm mới
        </button>
      </div>

      {/* ─── DATE RANGE FILTER ───────────────────────────────────────────── */}
      <DateRangeFilter
        initialStartDate={dateFilter.startDate || ''}
        initialEndDate={dateFilter.endDate || ''}
        onApply={handleApplyFilter}
        onReset={handleResetFilter}
      />

      {/* ─── STAT CARDS (5 KPI) ──────────────────────────────────────────── */}
      <div className="stat-grid">
        <StatCard
          title="Tổng số suất chiếu"
          value={loadingSummary ? '...' : totalShowtimes}
          subtext="Số suất chiếu trong kỳ lọc"
          icon={Film}
          color="#3b82f6"
        />
        <StatCard
          title="Tổng số vé đã bán"
          value={loadingSummary ? '...' : `${totalBooked.toLocaleString()} / ${totalCapacity.toLocaleString()}`}
          subtext="Số ghế đã đặt / Tổng sức chứa"
          icon={Ticket}
          color="#06b6d4"
        />
        <StatCard
          title="Tỷ lệ lấp đầy trung bình"
          value={loadingSummary ? '...' : overallOccupancyPercent}
          subtext="Overall Occupancy Rate"
          icon={Percent}
          color="#10b981"
        />
        <StatCard
          title="Suất High Demand"
          value={loadingSummary ? '...' : highDemandCount}
          subtext="Tỷ lệ lấp đầy ≥ 80%"
          icon={Flame}
          color="#ef4444"
        />
        <StatCard
          title="Suất Low Demand"
          value={loadingSummary ? '...' : lowDemandCount}
          subtext="Tỷ lệ lấp đầy < 40%"
          icon={Snowflake}
          color="#64748b"
        />
      </div>

      {/* ─── DAILY TREND CHART ──────────────────────────────────────────── */}
      <DailyTrendChart
        data={dailyData}
        loading={loadingDaily}
        error={errors.daily}
        onRetry={() => fetchDailyTrend(dateFilter)}
      />

      {/* ─── 2 COLUMNS: TOP MOVIES & DAY OF WEEK ────────────────────────── */}
      <div className="chart-grid-2">
        <TopMoviesChart
          data={topMoviesData}
          loading={loadingTopMovies}
          error={errors.topMovies}
          currentBy={topBy}
          onByChange={handleTopByChange}
        />
        <DayOfWeekChart
          data={dayOfWeekData}
          loading={loadingDayOfWeek}
          error={errors.dayOfWeek}
          onRetry={() => fetchDayOfWeek(dateFilter)}
        />
      </div>

      {/* ─── TIME SLOT CHART ────────────────────────────────────────────── */}
      <TimeSlotChart
        data={timeSlotsData}
        loading={loadingTimeSlots}
        error={errors.timeSlots}
        onRetry={() => fetchTimeSlots(dateFilter)}
      />

      {/* ─── OCCUPANCY HEATMAP 2D ───────────────────────────────────────── */}
      <OccupancyHeatmap
        data={heatmapData}
        loading={loadingHeatmap}
        error={errors.heatmap}
        onRetry={() => fetchHeatmap(dateFilter)}
      />

      {/* ─── HIGH / LOW DEMAND SHOWTIMES LIST ───────────────────────────── */}
      <HighLowDemandList
        highDemand={highDemandData}
        lowDemand={lowDemandData}
        loading={loadingDemand}
        error={errors.demand}
      />
    </div>
  );
}
