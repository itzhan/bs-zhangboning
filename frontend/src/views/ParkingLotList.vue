<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import {
  NCard, NGrid, NGi, NInput, NSpace, NSpin, NTag, NIcon, NEllipsis, NButton, NEmpty
} from 'naive-ui'
import { SearchOutline, LocationOutline, TimeOutline, CarSportOutline } from '@vicons/ionicons5'

const router = useRouter()
const keyword = ref('')
const parkingLots = ref<any[]>([])
const allLots = ref<any[]>([])
const loading = ref(false)

function getAvailableColor(available: number, total: number) {
  if (total === 0) return '#999'
  const ratio = available / total
  if (ratio > 0.5) return '#18a058'
  if (ratio > 0.2) return '#f0a020'
  return '#d03050'
}

function getStatusLabel(status: string | number) {
  const map: Record<string | number, string> = { OPEN: '营业中', FULL: '已满', CLOSED: '已关闭', 1: '营业中', 0: '已关闭', 2: '维护中' }
  return map[status] || String(status || '未知')
}

function getStatusType(status: string | number): 'success' | 'warning' | 'error' | 'info' {
  if (status === '营业中' || status === 'OPEN' || status === 1) return 'success'
  if (status === '已满' || status === 'FULL') return 'warning'
  if (status === 0) return 'error'
  return 'info'
}

function handleSearch() {
  if (!keyword.value.trim()) {
    parkingLots.value = allLots.value
    return
  }
  const kw = keyword.value.trim().toLowerCase()
  parkingLots.value = allLots.value.filter(
    (lot) =>
      lot.name?.toLowerCase().includes(kw) ||
      lot.address?.toLowerCase().includes(kw)
  )
}

async function fetchParkingLots() {
  loading.value = true
  try {
    const res: any = await get('/api/public/parking-lots', keyword.value ? { keyword: keyword.value } : undefined)
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    allLots.value = Array.isArray(list) ? list : []
    parkingLots.value = allLots.value
  } catch {
    allLots.value = []
    parkingLots.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchParkingLots)
</script>

<template>
  <div class="parking-list-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">停车场列表</h1>
        <p class="page-desc">查找景区周边停车场，实时查看空位情况</p>
      </div>

      <!-- Search Bar -->
      <n-card class="search-bar" :bordered="false" style="margin-bottom: 24px; border-radius: 12px;">
        <n-space align="center">
          <n-input
            v-model:value="keyword"
            placeholder="搜索停车场名称或地址"
            clearable
            size="large"
            style="width: 360px;"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-button type="primary" size="large" @click="handleSearch">搜索</n-button>
        </n-space>
      </n-card>

      <!-- Lot Grid -->
      <n-spin :show="loading">
        <n-grid :cols="3" :x-gap="20" :y-gap="20" responsive="screen" item-responsive>
          <n-gi v-for="lot in parkingLots" :key="lot.id" span="3 m:3 l:1">
            <n-card
              hoverable
              class="lot-card"
              @click="router.push(`/parking-lots/${lot.id}`)"
            >
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-size: 16px; font-weight: 600;">{{ lot.name }}</span>
                  <n-tag :type="getStatusType(lot.status)" size="small" round>
                    {{ getStatusLabel(lot.status) }}
                  </n-tag>
                </div>
              </template>
              <div class="lot-info">
                <p class="lot-address">
                  <n-icon :component="LocationOutline" :size="14" />
                  <n-ellipsis style="max-width: 280px;">{{ lot.address || '暂无地址' }}</n-ellipsis>
                </p>
                <p class="lot-time" v-if="lot.openTime || lot.open_time">
                  <n-icon :component="TimeOutline" :size="14" />
                  {{ lot.openTime || lot.open_time || '00:00' }} - {{ lot.closeTime || lot.close_time || '24:00' }}
                </p>
              </div>
              <div class="lot-spaces">
                <div>
                  <n-icon :component="CarSportOutline" :size="16" style="margin-right: 4px;" />
                  <span>空位</span>
                </div>
                <div>
                  <span
                    class="space-num"
                    :style="{ color: getAvailableColor(lot.availableSpaces ?? lot.available_spaces ?? 0, lot.totalSpaces ?? lot.total_spaces ?? 1) }"
                  >
                    {{ lot.availableSpaces ?? lot.available_spaces ?? 0 }}
                  </span>
                  <span class="space-total"> / {{ lot.totalSpaces ?? lot.total_spaces ?? 0 }}</span>
                </div>
              </div>
            </n-card>
          </n-gi>
        </n-grid>
        <n-empty v-if="!loading && parkingLots.length === 0" description="暂无停车场数据" style="margin-top: 60px;" />
      </n-spin>
    </div>
  </div>
</template>

<style scoped>
.parking-list-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.page-header {
  margin-bottom: 28px;
}
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}
.page-desc {
  color: #888;
  font-size: 14px;
  margin: 0;
}
.search-bar {
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
}
.lot-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.lot-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(24, 160, 88, 0.12);
}
.lot-info {
  margin-top: 4px;
}
.lot-info p {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 6px 0;
  font-size: 13px;
  color: #888;
}
.lot-spaces {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f3f3f3;
  font-size: 14px;
  color: #666;
}
.space-num {
  font-size: 26px;
  font-weight: 700;
}
.space-total {
  color: #aaa;
  font-size: 14px;
}
</style>
