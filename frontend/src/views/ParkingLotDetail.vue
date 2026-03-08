<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '@/utils/request'
import {
  NCard, NGrid, NGi, NButton, NTag, NSpace, NSpin, NIcon, NProgress,
  NDescriptions, NDescriptionsItem, NTable, NEmpty, NDivider
} from 'naive-ui'
import {
  LocationOutline, TimeOutline, CallOutline, NavigateOutline,
  CalendarOutline, CarSportOutline, InformationCircleOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const lotId = computed(() => route.params.id as string)

const lot = ref<any>({})
const billingRules = ref<any[]>([])
const availableSpaces = ref<any[]>([])
const loadingLot = ref(false)
const loadingRules = ref(false)
const loadingSpaces = ref(false)

const availableRatio = computed(() => {
  const total = lot.value.totalSpaces ?? lot.value.total_spaces ?? 0
  const available = lot.value.availableSpaces ?? lot.value.available_spaces ?? 0
  return total > 0 ? Math.round((available / total) * 100) : 0
})

const progressColor = computed(() => {
  if (availableRatio.value > 50) return '#18a058'
  if (availableRatio.value > 20) return '#f0a020'
  return '#d03050'
})

function openNavigation() {
  const lng = lot.value.longitude || lot.value.lng
  const lat = lot.value.latitude || lot.value.lat
  if (lng && lat) {
    window.open(`https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(lot.value.name || '停车场')}`, '_blank')
  } else {
    window.$message?.warning('该停车场暂无坐标信息')
  }
}

async function fetchLot() {
  loadingLot.value = true
  try {
    const res: any = await get(`/api/public/parking-lots/${lotId.value}`)
    lot.value = res.data || res
  } catch {
    window.$message?.error('获取停车场信息失败')
  } finally {
    loadingLot.value = false
  }
}

async function fetchBillingRules() {
  loadingRules.value = true
  try {
    const res: any = await get(`/api/billing-rules/lot/${lotId.value}`)
    const data = res.data || res
    // API returns a single BillingRule object, not a list
    if (data && typeof data === 'object' && !Array.isArray(data) && data.id) {
      billingRules.value = [data]
    } else if (Array.isArray(data)) {
      billingRules.value = data
    } else if (data?.list) {
      billingRules.value = data.list
    } else if (data?.records) {
      billingRules.value = data.records
    } else {
      billingRules.value = []
    }
  } catch {
    billingRules.value = []
  } finally {
    loadingRules.value = false
  }
}

async function fetchAvailableSpaces() {
  loadingSpaces.value = true
  try {
    const res: any = await get(`/api/parking-spaces/available/${lotId.value}`)
    const list = res.data?.records || res.data || res.records || res || []
    availableSpaces.value = Array.isArray(list) ? list : []
  } catch {
    availableSpaces.value = []
  } finally {
    loadingSpaces.value = false
  }
}

onMounted(() => {
  fetchLot()
  fetchBillingRules()
  fetchAvailableSpaces()
})
</script>

<template>
  <div class="lot-detail-page">
    <div class="container">
      <n-spin :show="loadingLot">
        <!-- Lot Header -->
        <n-card class="lot-header-card" :bordered="false">
          <div class="lot-header">
            <div>
              <h1 class="lot-name">{{ lot.name || '加载中...' }}</h1>
              <p class="lot-address">
                <n-icon :component="LocationOutline" :size="16" />
                {{ lot.address || '暂无地址' }}
              </p>
            </div>
            <n-space>
              <n-button type="primary" round @click="router.push({ path: '/reservation', query: { lotId: lotId } })">
                <template #icon><n-icon :component="CalendarOutline" /></template>
                预约车位
              </n-button>
              <n-button round @click="openNavigation">
                <template #icon><n-icon :component="NavigateOutline" /></template>
                导航到这里
              </n-button>
            </n-space>
          </div>
        </n-card>

        <n-grid :cols="3" :x-gap="20" :y-gap="20" style="margin-top: 20px;" responsive="screen" item-responsive>
          <!-- Lot Info -->
          <n-gi span="3 m:3 l:2">
            <n-card title="停车场信息" :bordered="false" class="info-card">
              <n-descriptions :column="2" label-placement="left" bordered>
                <n-descriptions-item label="营业时间">
                  <n-icon :component="TimeOutline" :size="14" style="margin-right: 4px;" />
                  {{ lot.openTime || lot.open_time || '00:00' }} - {{ lot.closeTime || lot.close_time || '24:00' }}
                </n-descriptions-item>
                <n-descriptions-item label="联系电话">
                  <n-icon :component="CallOutline" :size="14" style="margin-right: 4px;" />
                  {{ lot.contactPhone || lot.contact_phone || lot.phone || '暂无' }}
                </n-descriptions-item>
                <n-descriptions-item label="状态">
                  <n-tag :type="lot.status === 'OPEN' || lot.status === 1 ? 'success' : 'warning'" size="small">
                    {{ lot.status === 'OPEN' || lot.status === 1 ? '营业中' : lot.status === 'FULL' ? '已满' : lot.status === 0 ? '关闭' : lot.status === 2 ? '维护中' : lot.status || '未知' }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item label="总车位">
                  {{ lot.totalSpaces ?? lot.total_spaces ?? 0 }}
                </n-descriptions-item>
                <n-descriptions-item label="描述" :span="2">
                  {{ lot.description || '暂无描述' }}
                </n-descriptions-item>
              </n-descriptions>
            </n-card>

            <!-- Billing Rules -->
            <n-card title="计费规则" :bordered="false" class="info-card" style="margin-top: 20px;">
              <n-spin :show="loadingRules">
                <n-table :bordered="false" :single-line="false" size="small" v-if="billingRules.length > 0">
                  <thead>
                    <tr>
                      <th>规则名称</th>
                      <th>计费方式</th>
                      <th>单价</th>
                      <th>封顶金额</th>
                      <th>免费时长</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="rule in billingRules" :key="rule.id">
                      <td>{{ rule.name || '-' }}</td>
                      <td>{{ rule.ruleType === 1 ? '按时计费' : rule.ruleType === 2 ? '按次计费' : '-' }}</td>
                      <td>¥{{ rule.firstHourPrice ?? '-' }}/首小时，¥{{ rule.extraHourPrice ?? '-' }}/超时</td>
                      <td>{{ rule.dailyMax ? `¥${rule.dailyMax}` : '无' }}</td>
                      <td>{{ rule.freeMinutes ?? 0 }}分钟</td>
                    </tr>
                  </tbody>
                </n-table>
                <n-empty v-else description="暂无计费规则" />
              </n-spin>
            </n-card>
          </n-gi>

          <!-- Right: Real-time availability -->
          <n-gi span="3 m:3 l:1">
            <n-card title="实时车位" :bordered="false" class="info-card">
              <div class="availability-display">
                <n-progress
                  type="circle"
                  :percentage="availableRatio"
                  :color="progressColor"
                  :rail-color="progressColor + '20'"
                  :stroke-width="10"
                  style="width: 160px;"
                >
                  <div style="text-align: center;">
                    <div style="font-size: 32px; font-weight: 700;" :style="{ color: progressColor }">
                      {{ lot.availableSpaces ?? lot.available_spaces ?? 0 }}
                    </div>
                    <div style="font-size: 12px; color: #999;">可用车位</div>
                  </div>
                </n-progress>
                <p style="text-align: center; color: #888; margin-top: 12px; font-size: 13px;">
                  总计 {{ lot.totalSpaces ?? lot.total_spaces ?? 0 }} 个车位
                </p>
              </div>
            </n-card>

            <!-- Available Spaces -->
            <n-card title="可用车位列表" :bordered="false" class="info-card" style="margin-top: 20px;">
              <n-spin :show="loadingSpaces">
                <div v-if="availableSpaces.length > 0" class="space-grid">
                  <div
                    v-for="space in availableSpaces"
                    :key="space.id"
                    class="space-item"
                  >
                    <n-icon :component="CarSportOutline" :size="16" color="#18a058" />
                    <span>{{ space.spaceNumber || space.space_number || space.code || space.name || '-' }}</span>
                    <n-tag v-if="space.type" size="tiny" :bordered="false">{{ space.type }}</n-tag>
                  </div>
                </div>
                <n-empty v-else description="暂无可用车位" />
              </n-spin>
            </n-card>
          </n-gi>
        </n-grid>
      </n-spin>
    </div>
  </div>
</template>

<style scoped>
.lot-detail-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.lot-header-card {
  border-radius: 12px;
}
.lot-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}
.lot-name {
  font-size: 26px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}
.lot-address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #888;
  margin: 0;
}
.info-card {
  border-radius: 12px;
}
.availability-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}
.space-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.space-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f7faf8;
  border: 1px solid #e8f5e9;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 13px;
  color: #555;
}
</style>
