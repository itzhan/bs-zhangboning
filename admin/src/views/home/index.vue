<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { NGrid, NGi, NCard, NStatistic, NNumberAnimation, NSpin, NSpace, NTag } from 'naive-ui';
import * as echarts from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { request } from '@/service/request';
import SvgIcon from '@/components/custom/svg-icon.vue';
import { useAppStore } from '@/store/modules/app';
import { useAuthStore } from '@/store/modules/auth';

echarts.use([BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer]);

defineOptions({
  name: 'Home'
});

const router = useRouter();
const appStore = useAppStore();
const authStore = useAuthStore();
const gap = ref(appStore.isMobile ? 0 : 16);

// Current time greeting
const greetingText = computed(() => {
  const hour = new Date().getHours();
  const name = (authStore as any).userInfo?.userName || (authStore as any).userInfo?.username || '管理员';
  if (hour < 6) return `夜深了，${name}，注意休息！`;
  if (hour < 12) return `早上好，${name}，新的一天开始了！`;
  if (hour < 14) return `中午好，${name}，记得午休哦！`;
  if (hour < 18) return `下午好，${name}，继续加油！`;
  return `晚上好，${name}，辛苦了！`;
});

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

// Recent orders
const recentOrders = ref<any[]>([]);

// Quick action items
const quickActions = [
  { title: '停车场管理', desc: '管理停车场信息', icon: 'mdi:parking', color: '#2080f0', route: '/parking-lot' },
  { title: '车位管理', desc: '查看及调配车位', icon: 'mdi:car', color: '#67c23a', route: '/parking-space' },
  { title: '订单管理', desc: '查看停车订单', icon: 'mdi:receipt-text-outline', color: '#e6a23c', route: '/order' },
  { title: '用户管理', desc: '管理系统用户', icon: 'mdi:account-group', color: '#f56c6c', route: '/user' },
  { title: '优惠券管理', desc: '发放优惠券', icon: 'mdi:ticket-percent', color: '#9b59b6', route: '/coupon' },
  { title: '公告管理', desc: '发布系统公告', icon: 'mdi:bullhorn', color: '#00bcd4', route: '/announcement' },
];

/**
 * Get order status label and type
 */
function getOrderStatusInfo(status: number | string) {
  const map: Record<number, { label: string; type: 'default' | 'info' | 'success' | 'warning' | 'error' }> = {
    0: { label: '进行中', type: 'info' },
    1: { label: '待支付', type: 'warning' },
    2: { label: '已完成', type: 'success' },
    3: { label: '已取消', type: 'error' },
  };
  return map[Number(status)] || { label: '未知', type: 'default' as const };
}

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
      textStyle: { fontSize: 16, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: (params: any) => {
        const param = Array.isArray(params) ? params[0] : params;
        return `${param.name}<br/>营收: ¥${param.value}`;
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: revenueData.value.map(item => item.date)
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (value: number) => `¥${value}` }
    },
    series: [{
      name: '营收',
      type: 'line',
      smooth: true,
      data: revenueData.value.map(item => item.revenue),
      itemStyle: { color: '#2080f0' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(32, 128, 240, 0.3)' },
            { offset: 1, color: 'rgba(32, 128, 240, 0.05)' }
          ]
        }
      },
      emphasis: { focus: 'series' }
    }]
  };
  revenueChartInstance.setOption(option);
}

function updateRevenueChart() {
  if (!revenueChartInstance) return;
  revenueChartInstance.setOption({
    xAxis: { data: revenueData.value.map(item => item.date) },
    series: [{ data: revenueData.value.map(item => item.revenue) }]
  });
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
      textStyle: { fontSize: 16, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const param = Array.isArray(params) ? params[0] : params;
        return `${param.name}<br/>占用率: ${param.value}%`;
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: occupancyData.value.map(item => item.name),
      axisLabel: { rotate: 45, interval: 0 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: (value: number) => `${value}%` }
    },
    series: [{
      name: '占用率',
      type: 'bar',
      data: occupancyData.value.map(item => item.rate),
      itemStyle: {
        color: (params: any) => {
          const rate = params.value;
          if (rate >= 80) return '#f56c6c';
          if (rate >= 60) return '#e6a23c';
          return '#67c23a';
        }
      },
      label: {
        show: true,
        position: 'top',
        formatter: (params: any) => `${params.value}%`
      }
    }]
  };
  occupancyChartInstance.setOption(option);
}

function updateOccupancyChart() {
  if (!occupancyChartInstance) return;
  occupancyChartInstance.setOption({
    xAxis: { data: occupancyData.value.map(item => item.name) },
    series: [{ data: occupancyData.value.map(item => item.rate) }]
  });
}

function handleResize() {
  revenueChartInstance?.resize();
  occupancyChartInstance?.resize();
}

async function fetchDashboardData() {
  try {
    loading.value = true;
    
    // Fetch main dashboard stats
    try {
      const dashRes = await request({ url: '/api/dashboard' });
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
      const revenueResData = (revenueRes as any)?.error ? null : ((revenueRes as any)?.data || revenueRes);
      if (revenueResData) {
        const rawList = Array.isArray(revenueResData) ? revenueResData : [];
        revenueData.value = rawList.map((item: any) => ({
          date: item.date || '',
          revenue: Number(item.revenue) || 0
        }));
        await nextTick();
        if (revenueChartInstance) updateRevenueChart();
        else initRevenueChart();
      }
    } catch (e) {
      console.error('Failed to fetch revenue data:', e);
    }

    // Fetch occupancy data
    try {
      const occupancyRes = await request({ url: '/api/dashboard/occupancy' });
      const occupancyResData = (occupancyRes as any)?.error ? null : ((occupancyRes as any)?.data || occupancyRes);
      if (occupancyResData) {
        const rawList = Array.isArray(occupancyResData) ? occupancyResData : [];
        occupancyData.value = rawList.map((item: any) => ({
          name: item.lotName || item.name || '',
          rate: Number(item.occupancyRate) || Number(item.rate) || 0
        }));
        await nextTick();
        if (occupancyChartInstance) updateOccupancyChart();
        else initOccupancyChart();
      }
    } catch (e) {
      console.error('Failed to fetch occupancy data:', e);
    }

    // Fetch recent orders
    try {
      const ordersRes = await request({ url: '/api/orders/admin', params: { page: 1, size: 5 } });
      const ordersResData = (ordersRes as any)?.error ? null : ((ordersRes as any)?.data || ordersRes);
      if (ordersResData) {
        const list = ordersResData.list || ordersResData.records || (Array.isArray(ordersResData) ? ordersResData : []);
        recentOrders.value = Array.isArray(list) ? list : [];
      }
    } catch (e) {
      console.log('Recent orders endpoint not available');
    }

  } catch (error) {
    console.error('Failed to fetch dashboard data:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await nextTick();
  if (revenueChartRef.value) initRevenueChart();
  if (occupancyChartRef.value) initOccupancyChart();
  await fetchDashboardData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  revenueChartInstance?.dispose();
  occupancyChartInstance?.dispose();
});
</script>

<template>
  <NSpace vertical :size="16">
    <NSpin :show="loading">
      <!-- Welcome Banner -->
      <NCard :bordered="false" class="welcome-card">
        <div class="welcome-content">
          <div class="welcome-text">
            <h2 class="welcome-greeting">{{ greetingText }}</h2>
            <p class="welcome-desc">景区停车引导系统管理后台 · 实时掌握停车场运营状况</p>
          </div>
          <div class="welcome-visual">
            <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect width="18" height="18" x="3" y="3" rx="2"/>
              <path d="M9 17V7h4a3 3 0 0 1 0 6H9"/>
            </svg>
          </div>
        </div>
      </NCard>

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
                    <NNumberAnimation :from="0" :to="dashboardData.totalLots" :duration="1000" :precision="0" />
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
                    <NNumberAnimation :from="0" :to="dashboardData.totalSpaces" :duration="1000" :precision="0" />
                    <span class="stat-divider">/</span>
                    <NNumberAnimation :from="0" :to="dashboardData.availableSpaces" :duration="1000" :precision="0" />
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
                    <NNumberAnimation :from="0" :to="dashboardData.todayOrders" :duration="1000" :precision="0" />
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
                    <NNumberAnimation :from="0" :to="dashboardData.todayRevenue" :duration="1000" :precision="2" />
                  </NStatistic>
                </div>
              </div>
            </NCard>
          </NGi>
        </NGrid>
      </NCard>

      <!-- Quick Actions -->
      <NCard :bordered="false" class="card-wrapper">
        <template #header>
          <div style="font-size: 16px; font-weight: bold; display: flex; align-items: center; gap: 8px;">
            <SvgIcon icon="mdi:lightning-bolt" style="color: #e6a23c;" />
            快捷入口
          </div>
        </template>
        <NGrid cols="s:2 m:3 l:6" responsive="screen" :x-gap="12" :y-gap="12">
          <NGi v-for="action in quickActions" :key="action.title">
            <div class="quick-action-card" @click="router.push(action.route)">
              <div class="quick-action-icon" :style="{ background: action.color + '15', color: action.color }">
                <SvgIcon :icon="action.icon" style="font-size: 24px;" />
              </div>
              <div class="quick-action-title">{{ action.title }}</div>
              <div class="quick-action-desc">{{ action.desc }}</div>
            </div>
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
          <div style="font-size: 16px; font-weight: bold; display: flex; align-items: center; gap: 8px;">
            <SvgIcon icon="mdi:clipboard-text-clock" style="color: #2080f0;" />
            最近订单
          </div>
        </template>
        <div class="recent-orders">
          <div v-for="(order, index) in recentOrders" :key="index" class="order-item">
            <div class="order-info">
              <div class="order-title">
                <span>订单 #{{ order.orderNo || order.id }}</span>
                <NTag :type="getOrderStatusInfo(order.status).type" size="small" round>
                  {{ getOrderStatusInfo(order.status).label }}
                </NTag>
              </div>
              <div class="order-meta">
                <SvgIcon icon="mdi:map-marker" style="font-size: 12px;" />
                {{ order.lotName || '停车场' }} · {{ order.plateNumber || '' }} · {{ order.createdAt || '--' }}
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

/* Welcome Banner */
.welcome-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #2080f0 0%, #18a058 100%) !important;
  border: none !important;
}

.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.welcome-text {
  flex: 1;
}

.welcome-greeting {
  font-size: 22px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px;
}

.welcome-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
  margin: 0;
}

.welcome-visual {
  margin-left: 24px;
  opacity: 0.8;
}

/* Stat Cards */
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

/* Quick Action Cards */
.quick-action-card {
  text-align: center;
  padding: 20px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid transparent;
}

.quick-action-card:hover {
  background: rgba(32, 128, 240, 0.04);
  border-color: rgba(32, 128, 240, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.quick-action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.quick-action-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--n-text-color);
  margin-bottom: 4px;
}

.quick-action-desc {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.dark .quick-action-desc {
  color: rgba(255, 255, 255, 0.45);
}

/* Charts */
.chart-container {
  width: 100%;
  height: 400px;
}

/* Recent Orders */
.recent-orders {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
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
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-meta {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  gap: 4px;
}

.dark .order-meta {
  color: rgba(255, 255, 255, 0.45);
}

.order-amount {
  font-size: 18px;
  font-weight: 600;
  color: #2080f0;
}
</style>