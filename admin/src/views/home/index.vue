<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { NGrid, NGi, NCard, NStatistic, NNumberAnimation, NSpin, NSpace } from 'naive-ui';
import * as echarts from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { request } from '@/service/request';
import SvgIcon from '@/components/custom/svg-icon.vue';
import { useAppStore } from '@/store/modules/app';

echarts.use([BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer]);

defineOptions({
  name: 'Home'
});

const appStore = useAppStore();
const gap = ref(appStore.isMobile ? 0 : 16);

// Loading state
const loading = ref(true);

// Dashboard data
const dashboardData = ref({
  totalLots: 0,
  totalSpaces: 0,
  availableSpaces: 0,
  todayOrders: 0,
  todayRevenue: 0,
  occupancyRate: 0
});

// Chart refs
const revenueChartRef = ref<HTMLDivElement | null>(null);
const occupancyChartRef = ref<HTMLDivElement | null>(null);
let revenueChartInstance: echarts.ECharts | null = null;
let occupancyChartInstance: echarts.ECharts | null = null;

// Revenue chart data
const revenueData = ref<{ date: string; revenue: number }[]>([]);

// Occupancy chart data
const occupancyData = ref<{ name: string; rate: number }[]>([]);

// Recent orders/announcements
const recentOrders = ref<any[]>([]);

/**
 * Initialize revenue chart
 */
function initRevenueChart() {
  if (!revenueChartRef.value) return;

  revenueChartInstance = echarts.init(revenueChartRef.value);
  
  const option: echarts.EChartsOption = {
    title: {
      text: '近7天营收趋势',
      left: 'left',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: (params: any) => {
        const param = Array.isArray(params) ? params[0] : params;
        return `${param.name}<br/>营收: ¥${param.value}`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: revenueData.value.map(item => item.date)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => `¥${value}`
      }
    },
    series: [
      {
        name: '营收',
        type: 'line',
        smooth: true,
        data: revenueData.value.map(item => item.revenue),
        itemStyle: {
          color: '#2080f0'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(32, 128, 240, 0.3)'
              },
              {
                offset: 1,
                color: 'rgba(32, 128, 240, 0.05)'
              }
            ]
          }
        },
        emphasis: {
          focus: 'series'
        }
      }
    ]
  };

  revenueChartInstance.setOption(option);
}

/**
 * Update revenue chart
 */
function updateRevenueChart() {
  if (!revenueChartInstance) return;

  const option: echarts.EChartsOption = {
    xAxis: {
      data: revenueData.value.map(item => item.date)
    },
    series: [
      {
        data: revenueData.value.map(item => item.revenue)
      }
    ]
  };

  revenueChartInstance.setOption(option);
}

/**
 * Initialize occupancy chart
 */
function initOccupancyChart() {
  if (!occupancyChartRef.value) return;

  occupancyChartInstance = echarts.init(occupancyChartRef.value);
  
  const option: echarts.EChartsOption = {
    title: {
      text: '各停车场占用率',
      left: 'left',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const param = Array.isArray(params) ? params[0] : params;
        return `${param.name}<br/>占用率: ${param.value}%`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: occupancyData.value.map(item => item.name),
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: (value: number) => `${value}%`
      }
    },
    series: [
      {
        name: '占用率',
        type: 'bar',
        data: occupancyData.value.map(item => item.rate),
        itemStyle: {
          color: (params: any) => {
            const rate = params.value;
            if (rate >= 80) return '#f56c6c'; // Red for high occupancy
            if (rate >= 60) return '#e6a23c'; // Orange for medium-high
            return '#67c23a'; // Green for low-medium
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => `${params.value}%`
        }
      }
    ]
  };

  occupancyChartInstance.setOption(option);
}

/**
 * Update occupancy chart
 */
function updateOccupancyChart() {
  if (!occupancyChartInstance) return;

  const option: echarts.EChartsOption = {
    xAxis: {
      data: occupancyData.value.map(item => item.name)
    },
    series: [
      {
        data: occupancyData.value.map(item => item.rate)
      }
    ]
  };

  occupancyChartInstance.setOption(option);
}

/**
 * Handle window resize
 */
function handleResize() {
  revenueChartInstance?.resize();
  occupancyChartInstance?.resize();
}

/**
 * Fetch dashboard data
 */
async function fetchDashboardData() {
  try {
    loading.value = true;
    
    // Fetch main dashboard stats
    try {
      const dashRes = await request({ url: '/api/dashboard' });
      // Handle both { data, error } format and direct data format
      const dashData = (dashRes as any)?.error ? null : ((dashRes as any)?.data || dashRes);
      if (dashData) {
        dashboardData.value = {
          totalLots: dashData.totalLots || 0,
          totalSpaces: dashData.totalSpaces || 0,
          availableSpaces: dashData.availableSpaces || 0,
          todayOrders: dashData.todayOrders || 0,
          todayRevenue: dashData.todayRevenue || 0,
          occupancyRate: dashData.occupancyRate || 0
        };
      }
    } catch (e) {
      console.error('Failed to fetch dashboard stats:', e);
    }

    // Fetch revenue data (last 7 days)
    try {
      const endDate = new Date();
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 6);
      
      const startDateStr = startDate.toISOString().split('T')[0];
      const endDateStr = endDate.toISOString().split('T')[0];

      const revenueRes = await request({ 
        url: '/api/dashboard/revenue', 
        params: { startDate: startDateStr, endDate: endDateStr } 
      });
      
      // Handle both { data, error } format and direct data format
      const revenueResData = (revenueRes as any)?.error ? null : ((revenueRes as any)?.data || revenueRes);
      if (revenueResData) {
        const rawList = Array.isArray(revenueResData) ? revenueResData : [];
        // Map RevenueStatsResponse fields: { date, revenue, orderCount }
        revenueData.value = rawList.map((item: any) => ({
          date: item.date || '',
          revenue: Number(item.revenue) || 0
        }));
        await nextTick();
        if (revenueChartInstance) {
          updateRevenueChart();
        } else {
          initRevenueChart();
        }
      }
    } catch (e) {
      console.error('Failed to fetch revenue data:', e);
    }

    // Fetch occupancy data
    try {
      const occupancyRes = await request({ url: '/api/dashboard/occupancy' });
      
      // Handle both { data, error } format and direct data format
      const occupancyResData = (occupancyRes as any)?.error ? null : ((occupancyRes as any)?.data || occupancyRes);
      if (occupancyResData) {
        const rawList = Array.isArray(occupancyResData) ? occupancyResData : [];
        // Map OccupancyStatsResponse fields: { lotName, occupancyRate, totalSpaces, occupiedSpaces }
        occupancyData.value = rawList.map((item: any) => ({
          name: item.lotName || item.name || '',
          rate: Number(item.occupancyRate) || Number(item.rate) || 0
        }));
        await nextTick();
        if (occupancyChartInstance) {
          updateOccupancyChart();
        } else {
          initOccupancyChart();
        }
      }
    } catch (e) {
      console.error('Failed to fetch occupancy data:', e);
    }

    // Fetch recent orders (optional)
    try {
      const ordersRes = await request({ url: '/api/orders/admin', params: { page: 1, size: 5 } });
      const ordersResData = (ordersRes as any)?.error ? null : ((ordersRes as any)?.data || ordersRes);
      if (ordersResData) {
        const list = ordersResData.list || ordersResData.records || (Array.isArray(ordersResData) ? ordersResData : []);
        recentOrders.value = Array.isArray(list) ? list : [];
      }
    } catch (e) {
      // Ignore if endpoint doesn't exist
      console.log('Recent orders endpoint not available');
    }

  } catch (error) {
    console.error('Failed to fetch dashboard data:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  // Wait for DOM to be ready
  await nextTick();
  
  // Initialize charts first (with empty data)
  if (revenueChartRef.value) {
    initRevenueChart();
  }
  if (occupancyChartRef.value) {
    initOccupancyChart();
  }
  
  // Then fetch and update with real data
  await fetchDashboardData();
  
  // Add resize listener
  window.addEventListener('resize', handleResize);
});

// Cleanup
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  revenueChartInstance?.dispose();
  occupancyChartInstance?.dispose();
});
</script>

<template>
  <NSpace vertical :size="16">
    <NSpin :show="loading">
      <!-- Stat Cards -->
      <NCard :bordered="false" class="card-wrapper">
        <NGrid cols="s:1 m:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
          <!-- 停车场总数 -->
          <NGi>
            <NCard :bordered="false" class="stat-card">
              <div class="stat-card-content">
                <div class="stat-card-icon" style="background: linear-gradient(135deg, #2080f0 0%, #1a6dd8 100%);">
                  <SvgIcon icon="mdi:parking" class="icon" />
                </div>
                <div class="stat-card-info">
                  <div class="stat-card-label">停车场总数</div>
                  <NStatistic>
                    <NNumberAnimation
                      :from="0"
                      :to="dashboardData.totalLots"
                      :duration="1000"
                      :precision="0"
                    />
                  </NStatistic>
                </div>
              </div>
            </NCard>
          </NGi>

          <!-- 总车位数/可用车位 -->
          <NGi>
            <NCard :bordered="false" class="stat-card">
              <div class="stat-card-content">
                <div class="stat-card-icon" style="background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);">
                  <SvgIcon icon="mdi:car" class="icon" />
                </div>
                <div class="stat-card-info">
                  <div class="stat-card-label">总车位/可用</div>
                  <NStatistic>
                    <NNumberAnimation
                      :from="0"
                      :to="dashboardData.totalSpaces"
                      :duration="1000"
                      :precision="0"
                    />
                    <span class="stat-divider">/</span>
                    <NNumberAnimation
                      :from="0"
                      :to="dashboardData.availableSpaces"
                      :duration="1000"
                      :precision="0"
                    />
                  </NStatistic>
                  <div class="stat-card-sub">
                    占用率: {{ dashboardData.occupancyRate.toFixed(1) }}%
                  </div>
                </div>
              </div>
            </NCard>
          </NGi>

          <!-- 今日订单 -->
          <NGi>
            <NCard :bordered="false" class="stat-card">
              <div class="stat-card-content">
                <div class="stat-card-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #cf9236 100%);">
                  <SvgIcon icon="mdi:receipt-text-outline" class="icon" />
                </div>
                <div class="stat-card-info">
                  <div class="stat-card-label">今日订单</div>
                  <NStatistic>
                    <NNumberAnimation
                      :from="0"
                      :to="dashboardData.todayOrders"
                      :duration="1000"
                      :precision="0"
                    />
                  </NStatistic>
                </div>
              </div>
            </NCard>
          </NGi>

          <!-- 今日营收 -->
          <NGi>
            <NCard :bordered="false" class="stat-card">
              <div class="stat-card-content">
                <div class="stat-card-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #dd6161 100%);">
                  <SvgIcon icon="mdi:currency-cny" class="icon" />
                </div>
                <div class="stat-card-info">
                  <div class="stat-card-label">今日营收</div>
                  <NStatistic>
                    <span class="currency-symbol">¥</span>
                    <NNumberAnimation
                      :from="0"
                      :to="dashboardData.todayRevenue"
                      :duration="1000"
                      :precision="2"
                    />
                  </NStatistic>
                </div>
              </div>
            </NCard>
          </NGi>
        </NGrid>
      </NCard>

      <!-- Charts Section -->
      <NGrid :x-gap="gap" :y-gap="16" responsive="screen" item-responsive>
        <!-- Revenue Chart -->
        <NGi span="24 s:24 m:12">
          <NCard :bordered="false" class="card-wrapper">
            <div ref="revenueChartRef" class="chart-container"></div>
          </NCard>
        </NGi>

        <!-- Occupancy Chart -->
        <NGi span="24 s:24 m:12">
          <NCard :bordered="false" class="card-wrapper">
            <div ref="occupancyChartRef" class="chart-container"></div>
          </NCard>
        </NGi>
      </NGrid>

      <!-- Bottom Section: Recent Orders -->
      <NCard :bordered="false" class="card-wrapper" v-if="recentOrders.length > 0">
        <template #header>
          <div style="font-size: 16px; font-weight: bold;">最近订单</div>
        </template>
        <div class="recent-orders">
          <div v-for="(order, index) in recentOrders" :key="index" class="order-item">
            <div class="order-info">
              <div class="order-title">订单 #{{ order.orderNo || order.id }}</div>
              <div class="order-meta">
                {{ order.lotName || '停车场' }} · {{ order.createdAt || '--' }}
              </div>
            </div>
            <div class="order-amount">¥{{ order.actualAmount || order.amount || 0 }}</div>
          </div>
        </div>
      </NCard>
    </NSpin>
  </NSpace>
</template>

<style scoped>
.card-wrapper {
  border-radius: 8px;
}

.stat-card {
  height: 100%;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-card-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card-icon .icon {
  font-size: 32px;
  color: white;
}

.stat-card-info {
  flex: 1;
  min-width: 0;
}

.stat-card-label {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.65);
  margin-bottom: 8px;
}

.dark .stat-card-label {
  color: rgba(255, 255, 255, 0.65);
}

.stat-card-sub {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-top: 4px;
}

.dark .stat-card-sub {
  color: rgba(255, 255, 255, 0.45);
}

.stat-divider {
  margin: 0 4px;
  color: rgba(0, 0, 0, 0.45);
}

.dark .stat-divider {
  color: rgba(255, 255, 255, 0.45);
}

.currency-symbol {
  margin-right: 2px;
  font-size: 18px;
  font-weight: 500;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.recent-orders {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.02);
  transition: background 0.2s;
}

.dark .order-item {
  background: rgba(255, 255, 255, 0.05);
}

.order-item:hover {
  background: rgba(0, 0, 0, 0.05);
}

.dark .order-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.order-meta {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.dark .order-meta {
  color: rgba(255, 255, 255, 0.45);
}

.order-amount {
  font-size: 16px;
  font-weight: 600;
  color: #2080f0;
}
</style>