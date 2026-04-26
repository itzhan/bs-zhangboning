<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import {
  NCard, NGrid, NGi, NButton, NTag, NSpace, NSpin, NIcon, NEllipsis
} from 'naive-ui'
import {
  SearchOutline,
  CalendarOutline,
  QrCodeOutline,
  LocationOutline,
  TimeOutline,
  MegaphoneOutline,
  CarSportOutline,
  CompassOutline
} from '@vicons/ionicons5'

const router = useRouter()

const parkingLots = ref<any[]>([])
const announcements = ref<any[]>([])
const loadingLots = ref(false)
const loadingAnnouncements = ref(false)

const features = [
  { title: '智能引导推荐', desc: '按景点位置与实时空位推荐最优停车场', icon: CompassOutline, color: '#18a058', to: '/guidance' },
  { title: '实时车位查询', desc: '随时查看景区各停车场实时空位信息', icon: SearchOutline, color: '#2080f0', to: '/parking-lots' },
  { title: '在线预约车位', desc: '提前预约车位，出行无忧', icon: CalendarOutline, color: '#f0a020', to: '/reservation' },
  { title: '扫码快速入场', desc: '扫码即入，告别排队等候', icon: QrCodeOutline, color: '#d03050', to: '/parking-lots' },
]

function getAvailableColor(available: number, total: number) {
  if (total === 0) return '#999'
  const ratio = available / total
  if (ratio > 0.5) return '#18a058'
  if (ratio > 0.2) return '#f0a020'
  return '#d03050'
}

function getStatusType(status: string | number): 'success' | 'warning' | 'error' | 'info' {
  if (status === '营业中' || status === 'OPEN' || status === 1) return 'success'
  if (status === '已满' || status === 'FULL') return 'warning'
  if (status === 0) return 'error'
  return 'info'
}

function getStatusLabel(status: string | number): string {
  if (status === 'OPEN' || status === 1) return '营业中'
  if (status === 'FULL') return '已满'
  if (status === 0) return '关闭'
  if (status === 2) return '维护中'
  return String(status || '未知')
}

const announcementTypeLabel: Record<number, string> = {
  0: '通知',
  1: '公告',
  2: '活动',
  3: '紧急',
}

async function fetchParkingLots() {
  loadingLots.value = true
  try {
    const res: any = await get('/api/public/parking-lots')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    parkingLots.value = (Array.isArray(list) ? list : []).slice(0, 6)
  } catch {
    parkingLots.value = []
  } finally {
    loadingLots.value = false
  }
}

async function fetchAnnouncements() {
  loadingAnnouncements.value = true
  try {
    const res: any = await get('/api/public/announcements')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    announcements.value = (Array.isArray(list) ? list : []).slice(0, 5)
  } catch {
    announcements.value = []
  } finally {
    loadingAnnouncements.value = false
  }
}

onMounted(() => {
  fetchParkingLots()
  fetchAnnouncements()
})
</script>

<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">景区智能停车引导系统</h1>
        <p class="hero-subtitle">实时查询车位 · 在线预约 · 智能导航 · 便捷出行</p>
        <n-space justify="center" :size="16" style="margin-top: 32px;">
          <n-button type="primary" size="large" round @click="router.push('/parking-lots')">
            <template #icon><n-icon :component="SearchOutline" /></template>
            查看停车场
          </n-button>
          <n-button size="large" round ghost color="#fff" text-color="#fff" @click="router.push('/reservation')">
            <template #icon><n-icon :component="CalendarOutline" /></template>
            预约车位
          </n-button>
        </n-space>
      </div>
      <div class="hero-wave">
        <svg viewBox="0 0 1200 120" preserveAspectRatio="none">
          <path d="M0,60 C300,120 900,0 1200,60 L1200,120 L0,120 Z" fill="#f7faf8"/>
        </svg>
      </div>
    </div>

    <div class="container">
      <!-- Features Section -->
      <div class="section">
        <h2 class="section-title">服务特色</h2>
        <n-grid :cols="4" :x-gap="20" :y-gap="20" responsive="screen" item-responsive>
          <n-gi v-for="f in features" :key="f.title" span="4 m:2 l:1">
            <n-card hoverable class="feature-card" @click="f.to && router.push(f.to)">
              <div style="text-align: center;">
                <div class="feature-icon" :style="{ background: f.color + '15', color: f.color }">
                  <n-icon :component="f.icon" :size="32" />
                </div>
                <h3 style="margin: 12px 0 6px; font-size: 16px; color: #333;">{{ f.title }}</h3>
                <p style="color: #888; font-size: 13px; margin: 0;">{{ f.desc }}</p>
              </div>
            </n-card>
          </n-gi>
        </n-grid>
      </div>

      <!-- Hot Parking Lots -->
      <div class="section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <h2 class="section-title" style="margin-bottom: 0;">热门停车场</h2>
          <n-button text type="primary" @click="router.push('/parking-lots')">查看全部 →</n-button>
        </div>
        <n-spin :show="loadingLots">
          <n-grid :cols="3" :x-gap="20" :y-gap="20" responsive="screen" item-responsive>
            <n-gi v-for="lot in parkingLots" :key="lot.id" span="3 m:3 l:1">
              <n-card
                hoverable
                class="lot-card"
                @click="router.push(`/parking-lots/${lot.id}`)"
              >
                <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                  <div style="flex: 1;">
                    <h3 style="margin: 0 0 6px; font-size: 16px; color: #333;">{{ lot.name }}</h3>
                    <p style="color: #888; font-size: 13px; margin: 0; display: flex; align-items: center; gap: 4px;">
                      <n-icon :component="LocationOutline" :size="14" />
                      <n-ellipsis style="max-width: 220px;">{{ lot.address || '暂无地址' }}</n-ellipsis>
                    </p>
                  </div>
                  <n-tag :type="getStatusType(lot.status)" size="small" round>
                    {{ getStatusLabel(lot.status) }}
                  </n-tag>
                </div>
                <div style="margin-top: 16px; display: flex; justify-content: space-between; align-items: center;">
                  <div>
                    <span style="font-size: 24px; font-weight: 700;" :style="{ color: getAvailableColor(lot.availableSpaces ?? lot.available_spaces ?? 0, lot.totalSpaces ?? lot.total_spaces ?? 1) }">
                      {{ lot.availableSpaces ?? lot.available_spaces ?? 0 }}
                    </span>
                    <span style="color: #999; font-size: 13px;"> / {{ lot.totalSpaces ?? lot.total_spaces ?? 0 }} 车位</span>
                  </div>
                  <n-icon :component="CarSportOutline" :size="20" color="#ccc" />
                </div>
              </n-card>
            </n-gi>
          </n-grid>
          <div v-if="!loadingLots && parkingLots.length === 0" style="text-align: center; padding: 40px; color: #999;">
            暂无停车场数据
          </div>
        </n-spin>
      </div>

      <!-- Announcements -->
      <div class="section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <h2 class="section-title" style="margin-bottom: 0;">最新公告</h2>
          <n-button text type="primary" @click="router.push('/announcements')">查看全部 →</n-button>
        </div>
        <n-spin :show="loadingAnnouncements">
          <n-card>
            <div v-for="(a, idx) in announcements" :key="a.id" class="announcement-item" :style="{ borderBottom: idx < announcements.length - 1 ? '1px solid #f0f0f0' : 'none' }">
              <div style="display: flex; align-items: center; gap: 10px;">
                <n-icon :component="MegaphoneOutline" :size="18" color="#18a058" />
                <span class="announcement-title">{{ a.title }}</span>
                <n-tag v-if="a.type != null" size="tiny" :bordered="false" type="info">{{ announcementTypeLabel[a.type] || a.type }}</n-tag>
              </div>
              <span class="announcement-time">
                <n-icon :component="TimeOutline" :size="12" />
                {{ a.createdAt || a.publishTime || a.publish_time || a.createTime || a.create_time || '' }}
              </span>
            </div>
            <div v-if="!loadingAnnouncements && announcements.length === 0" style="text-align: center; padding: 20px; color: #999;">
              暂无公告
            </div>
          </n-card>
        </n-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 40px;
}
.hero-banner {
  background: linear-gradient(135deg, #18a058 0%, #2080f0 100%);
  padding: 80px 20px 100px;
  text-align: center;
  position: relative;
  overflow: hidden;
}
.hero-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 60%);
  animation: float 20s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-5%, 5%); }
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 12px;
  letter-spacing: 2px;
}
.hero-subtitle {
  font-size: 18px;
  color: rgba(255,255,255,0.85);
  margin: 0;
}
.hero-wave {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
}
.hero-wave svg {
  display: block;
  width: 100%;
  height: 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.section {
  margin-top: 48px;
}
.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  position: relative;
  padding-left: 14px;
}
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #18a058, #2080f0);
  border-radius: 2px;
}
.feature-card {
  border-radius: 12px;
  transition: transform 0.2s;
}
.feature-card:hover {
  transform: translateY(-4px);
}
.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.lot-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.lot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(24, 160, 88, 0.12);
}
.announcement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}
.announcement-title {
  font-size: 14px;
  color: #333;
}
.announcement-time {
  font-size: 12px;
  color: #bbb;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 28px;
  }
  .hero-subtitle {
    font-size: 14px;
  }
  .hero-banner {
    padding: 50px 16px 70px;
  }
}
</style>
