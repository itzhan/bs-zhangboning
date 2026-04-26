<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import {
  NCard, NGrid, NGi, NButton, NTag, NSpace, NSpin, NIcon, NEmpty,
  NSelect, NSteps, NStep, NProgress, NAlert
} from 'naive-ui'
import {
  NavigateOutline, LocationOutline, CarSportOutline, FlashOutline,
  CompassOutline, CheckmarkCircleOutline
} from '@vicons/ionicons5'

const router = useRouter()

interface ScenicSpot {
  id: number
  name: string
  longitude: number | string
  latitude: number | string
  description?: string
  image?: string
  radiusKm?: number | string
  nearbyLotCount?: number
  nearbyAvailableSpaces?: number
  nearbyTotalSpaces?: number
}

const scenicSpots = ref<ScenicSpot[]>([])
const selectedSpot = ref<number | null>(null)
const loadingSpots = ref(false)

// 下拉选项：显示名称 + 实时附近车位
const spotOptions = computed(() =>
  scenicSpots.value.map((s) => ({
    label: `${s.name}（附近 ${s.nearbyAvailableSpaces ?? 0}/${s.nearbyTotalSpaces ?? 0} 车位）`,
    value: s.id
  }))
)
const lots = ref<any[]>([])
const loading = ref(false)

const currentSpot = computed(() => {
  const spot = scenicSpots.value.find((s) => s.id === selectedSpot.value)
    ?? scenicSpots.value[0]
  if (!spot) return null
  return { ...spot, lng: Number(spot.longitude), lat: Number(spot.latitude) }
})

// 简化版距离估算（球面近似，单位：km）
function haversine(lat1: number, lng1: number, lat2: number, lng2: number) {
  const toRad = (d: number) => (d * Math.PI) / 180
  const R = 6371
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}

// 智能引导评分：可用率 70% + 距离 30%
const recommendations = computed(() => {
  const spot = currentSpot.value
  if (!spot) return []
  return lots.value
    .map((lot) => {
      const total = lot.totalSpaces ?? lot.total_spaces ?? 0
      const avail = lot.availableSpaces ?? lot.available_spaces ?? 0
      const ratio = total > 0 ? avail / total : 0
      const lng = Number(lot.longitude ?? lot.lng ?? spot.lng)
      const lat = Number(lot.latitude ?? lot.lat ?? spot.lat)
      const dist = haversine(spot.lat, spot.lng, lat, lng)
      // 距离归一：默认 5km 内为佳，越近越好
      const distScore = Math.max(0, 1 - Math.min(dist, 5) / 5)
      const score = ratio * 0.7 + distScore * 0.3
      const open = lot.status === 'OPEN' || lot.status === 1
      return { ...lot, _ratio: ratio, _dist: dist, _score: open ? score : 0, _avail: avail, _total: total, _open: open }
    })
    .filter((l) => l._total > 0)
    .sort((a, b) => b._score - a._score)
})

const top = computed(() => recommendations.value[0])

async function fetchLots() {
  loading.value = true
  try {
    const res: any = await get('/api/public/parking-lots')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    lots.value = Array.isArray(list) ? list : []
  } catch {
    lots.value = []
  } finally {
    loading.value = false
  }
}

async function fetchSpots() {
  loadingSpots.value = true
  try {
    const res: any = await get('/api/public/scenic-spots')
    const data = res.data || res
    const list = Array.isArray(data) ? data : (data.list || data.records || [])
    scenicSpots.value = Array.isArray(list) ? list : []
    if (scenicSpots.value.length > 0 && selectedSpot.value == null) {
      selectedSpot.value = scenicSpots.value[0]!.id
    }
  } catch {
    scenicSpots.value = []
  } finally {
    loadingSpots.value = false
  }
}

function navigateTo(lot: any) {
  const lng = lot.longitude || lot.lng
  const lat = lot.latitude || lot.lat
  const spot = currentSpot.value
  if (!spot) {
    window.$message?.warning('请先选择目的景点')
    return
  }
  if (lng && lat) {
    // 路径规划：从停车场到景点
    const url = `https://uri.amap.com/navigation?from=${lng},${lat},${encodeURIComponent(lot.name || '停车场')}&to=${spot.lng},${spot.lat},${encodeURIComponent(spot.name)}&mode=car&policy=1&src=parkguide&coordinate=gaode&callnative=0`
    window.open(url, '_blank')
  } else {
    window.$message?.warning('该停车场暂无坐标信息')
  }
}

function goReserve(lot: any) {
  router.push({ path: '/reservation', query: { lotId: lot.id } })
}

function goDetail(lot: any) {
  router.push(`/parking-lots/${lot.id}`)
}

onMounted(() => {
  fetchSpots()
  fetchLots()
})
</script>

<template>
  <div class="guidance-page">
    <div class="container">
      <!-- 头部说明 -->
      <n-card class="header-card" :bordered="false">
        <div class="header-inner">
          <div>
            <h1 class="title">
              <n-icon :component="CompassOutline" :size="28" color="#18a058" />
              智能停车引导
            </h1>
            <p class="subtitle">选择目的景点，系统将依据"实时空位 + 距离"为您推荐最优停车场，并为您规划至景点的步行/驾车路径。</p>
          </div>
          <div class="spot-selector">
            <span class="selector-label">目的地：</span>
            <n-select
              v-model:value="selectedSpot"
              :options="spotOptions"
              :loading="loadingSpots"
              :disabled="loadingSpots || scenicSpots.length === 0"
              placeholder="请选择景点"
              style="width: 320px;"
            />
          </div>
        </div>
      </n-card>

      <!-- 引导流程 -->
      <n-card :bordered="false" class="steps-card">
        <n-steps :current="3" size="small">
          <n-step title="选择景点" description="选择您的游览目的地" />
          <n-step title="智能推荐" description="按空位与距离综合排序" />
          <n-step title="一键导航" description="跳转地图，引导到达停车场" />
          <n-step title="抵达后续" description="扫码入场 / 在线预约" />
        </n-steps>
      </n-card>

      <n-spin :show="loading">
        <!-- 最佳推荐 -->
        <n-card v-if="top" :bordered="false" class="best-card">
          <div class="best-header">
            <n-tag type="success" size="medium" round>
              <template #icon><n-icon :component="FlashOutline" /></template>
              系统推荐
            </n-tag>
            <span class="best-tip">
              基于实时空位与到 <b>{{ currentSpot?.name }}</b> 的距离综合评估
              <template v-if="currentSpot">
                · 该景点附近共 <b>{{ currentSpot.nearbyLotCount ?? 0 }}</b> 个停车场，
                实时可用 <b style="color: #18a058;">{{ currentSpot.nearbyAvailableSpaces ?? 0 }}</b> /
                {{ currentSpot.nearbyTotalSpaces ?? 0 }} 个车位
              </template>
            </span>
          </div>
          <div class="best-body">
            <div class="best-info">
              <h2 class="best-name">{{ top.name }}</h2>
              <p class="best-addr">
                <n-icon :component="LocationOutline" :size="14" />
                {{ top.address || '暂无地址' }}
              </p>
              <n-space :size="20" style="margin-top: 12px;">
                <div>
                  <div class="metric-label">可用车位</div>
                  <div class="metric-value" style="color: #18a058;">
                    {{ top._avail }} <span class="metric-sub">/ {{ top._total }}</span>
                  </div>
                </div>
                <div>
                  <div class="metric-label">距景点</div>
                  <div class="metric-value" style="color: #2080f0;">
                    {{ top._dist.toFixed(2) }} <span class="metric-sub">km</span>
                  </div>
                </div>
                <div>
                  <div class="metric-label">综合评分</div>
                  <div class="metric-value" style="color: #f0a020;">
                    {{ (top._score * 100).toFixed(0) }} <span class="metric-sub">分</span>
                  </div>
                </div>
              </n-space>
            </div>
            <div class="best-actions">
              <n-progress
                type="circle"
                :percentage="Math.round(top._ratio * 100)"
                :stroke-width="10"
                style="width: 110px;"
                color="#18a058"
              >
                <div style="text-align: center;">
                  <div style="font-size: 22px; font-weight: 700; color: #18a058;">
                    {{ Math.round(top._ratio * 100) }}%
                  </div>
                  <div style="font-size: 11px; color: #999;">空位率</div>
                </div>
              </n-progress>
              <n-space style="margin-top: 16px;" justify="center">
                <n-button type="primary" round @click="navigateTo(top)">
                  <template #icon><n-icon :component="NavigateOutline" /></template>
                  立即导航
                </n-button>
                <n-button round @click="goReserve(top)">
                  预约车位
                </n-button>
              </n-space>
            </div>
          </div>
        </n-card>

        <!-- 候选列表 -->
        <div class="section">
          <h2 class="section-title">备选引导方案</h2>
          <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
            <n-gi v-for="(lot, idx) in recommendations.slice(1, 7)" :key="lot.id" span="3 m:3 l:1">
              <n-card hoverable class="alt-card">
                <div class="alt-rank">No.{{ idx + 2 }}</div>
                <h3 class="alt-name" @click="goDetail(lot)">{{ lot.name }}</h3>
                <p class="alt-addr">
                  <n-icon :component="LocationOutline" :size="13" />
                  {{ lot.address || '暂无地址' }}
                </p>
                <div class="alt-meta">
                  <n-tag size="small" type="success" :bordered="false">
                    <n-icon :component="CarSportOutline" :size="12" /> {{ lot._avail }}/{{ lot._total }}
                  </n-tag>
                  <n-tag size="small" type="info" :bordered="false">{{ lot._dist.toFixed(2) }} km</n-tag>
                  <n-tag size="small" type="warning" :bordered="false">评分 {{ (lot._score * 100).toFixed(0) }}</n-tag>
                </div>
                <n-space style="margin-top: 12px;">
                  <n-button size="small" type="primary" ghost @click="navigateTo(lot)">
                    <template #icon><n-icon :component="NavigateOutline" /></template>
                    导航
                  </n-button>
                  <n-button size="small" @click="goReserve(lot)">预约</n-button>
                </n-space>
              </n-card>
            </n-gi>
          </n-grid>
          <n-empty v-if="!loading && recommendations.length === 0" description="暂无可引导的停车场" style="padding: 40px;" />
        </div>

        <n-alert type="info" style="margin-top: 24px;" :show-icon="true">
          <template #icon><n-icon :component="CheckmarkCircleOutline" /></template>
          引导路径由高德地图开放服务承载；评分公式：空位率 × 0.7 + 距离评分 × 0.3。
        </n-alert>
      </n-spin>
    </div>
  </div>
</template>

<style scoped>
.guidance-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.header-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #f6fff9 0%, #f0f7ff 100%);
}
.header-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  margin: 0 0 8px;
  color: #222;
}
.subtitle {
  margin: 0;
  color: #666;
  font-size: 13px;
  max-width: 640px;
}
.spot-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}
.selector-label {
  font-size: 14px;
  color: #555;
}
.steps-card {
  margin-top: 16px;
  border-radius: 12px;
}
.best-card {
  margin-top: 16px;
  border-radius: 12px;
  border-left: 4px solid #18a058;
}
.best-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.best-tip {
  font-size: 12px;
  color: #999;
}
.best-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}
.best-info {
  flex: 1;
  min-width: 280px;
}
.best-name {
  font-size: 22px;
  font-weight: 700;
  color: #222;
  margin: 0 0 4px;
}
.best-addr {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #888;
  font-size: 13px;
  margin: 0;
}
.best-actions {
  text-align: center;
}
.metric-label {
  font-size: 12px;
  color: #999;
}
.metric-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 2px;
}
.metric-sub {
  font-size: 12px;
  color: #aaa;
  font-weight: 500;
}
.section {
  margin-top: 28px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 4px solid #2080f0;
}
.alt-card {
  border-radius: 12px;
  position: relative;
}
.alt-rank {
  position: absolute;
  top: 12px;
  right: 14px;
  font-size: 12px;
  color: #bbb;
  font-weight: 600;
}
.alt-name {
  font-size: 16px;
  margin: 0 0 4px;
  color: #333;
  cursor: pointer;
}
.alt-name:hover {
  color: #18a058;
}
.alt-addr {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #888;
  font-size: 12px;
  margin: 0 0 10px;
}
.alt-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
