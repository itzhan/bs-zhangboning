<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { get, post } from '@/utils/request'
import {
  NCard, NForm, NFormItem, NSelect, NDatePicker, NButton, NSteps, NStep,
  NSpace, NSpin, NIcon, NEmpty, NTag, NGrid, NGi
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { CarSportOutline, CalendarOutline, CheckmarkCircleOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInst | null>(null)
const currentStep = ref(1)
const loading = ref(false)
const submitting = ref(false)

const lotOptions = ref<any[]>([])
const vehicleOptions = ref<any[]>([])
const availableSpaces = ref<any[]>([])
const loadingSpaces = ref(false)

const formData = ref({
  lotId: null as number | null,
  vehicleId: null as number | null,
  plateNumber: '',
  startTime: null as number | null,
  endTime: null as number | null,
})

const rules: FormRules = {
  lotId: { required: true, type: 'number', message: '请选择停车场', trigger: 'change' },
  vehicleId: { required: true, type: 'number', message: '请选择车辆', trigger: 'change' },
  startTime: { required: true, type: 'number', message: '请选择开始时间', trigger: 'change' },
  endTime: { required: true, type: 'number', message: '请选择结束时间', trigger: 'change' },
}

const selectedLot = computed(() => {
  return lotOptions.value.find(l => l.value === formData.value.lotId)
})

async function fetchLots() {
  try {
    const res: any = await get('/api/public/parking-lots')
    const list = res.data?.records || res.data || res.records || res || []
    lotOptions.value = (Array.isArray(list) ? list : []).map((l: any) => ({
      label: `${l.name}（空位 ${l.availableSpaces ?? l.available_spaces ?? '?'}/${l.totalSpaces ?? l.total_spaces ?? '?'}）`,
      value: l.id,
      ...l
    }))
  } catch {
    lotOptions.value = []
  }
}

async function fetchVehicles() {
  try {
    const res: any = await get('/api/vehicles')
    const list = res.data?.records || res.data || res.records || res || []
    vehicleOptions.value = (Array.isArray(list) ? list : []).map((v: any) => ({
      label: `${v.plateNumber || v.plate_number} ${v.brand ? `(${v.brand} ${v.model || ''})` : ''}`,
      value: v.id,
      plateNumber: v.plateNumber || v.plate_number
    }))
  } catch {
    vehicleOptions.value = []
  }
}

async function fetchAvailableSpaces() {
  if (!formData.value.lotId) return
  loadingSpaces.value = true
  try {
    const res: any = await get(`/api/parking-spaces/available/${formData.value.lotId}`)
    const list = res.data?.records || res.data || res.records || res || []
    availableSpaces.value = Array.isArray(list) ? list : []
  } catch {
    availableSpaces.value = []
  } finally {
    loadingSpaces.value = false
  }
}

watch(() => formData.value.lotId, () => {
  if (formData.value.lotId) {
    fetchAvailableSpaces()
  }
})

watch(() => formData.value.vehicleId, (id) => {
  const v = vehicleOptions.value.find(v => v.value === id)
  if (v) formData.value.plateNumber = v.plateNumber
})

function nextStep() {
  if (currentStep.value === 1 && !formData.value.lotId) {
    window.$message?.warning('请先选择停车场')
    return
  }
  if (currentStep.value === 2 && !formData.value.vehicleId) {
    window.$message?.warning('请先选择车辆')
    return
  }
  if (currentStep.value === 3) {
    if (!formData.value.startTime || !formData.value.endTime) {
      window.$message?.warning('请选择预约时间')
      return
    }
    if (formData.value.endTime <= formData.value.startTime) {
      window.$message?.warning('结束时间必须晚于开始时间')
      return
    }
  }
  if (currentStep.value < 4) currentStep.value++
}

function prevStep() {
  if (currentStep.value > 1) currentStep.value--
}

async function handleSubmit() {
  submitting.value = true
  try {
    const startTime = formData.value.startTime ? dayjs(formData.value.startTime).format('YYYY-MM-DD HH:mm:ss') : ''
    const endTime = formData.value.endTime ? dayjs(formData.value.endTime).format('YYYY-MM-DD HH:mm:ss') : ''
    await post('/api/reservations', {
      lotId: formData.value.lotId,
      vehicleId: formData.value.vehicleId,
      startTime,
      endTime
    })
    window.$message?.success('预约成功！')
    router.push('/my-reservations')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

function disablePreviousDate(ts: number) {
  return ts < Date.now() - 3600000
}

onMounted(() => {
  fetchLots()
  fetchVehicles()
  // Pre-select lot from query
  if (route.query.lotId) {
    formData.value.lotId = Number(route.query.lotId)
  }
})
</script>

<template>
  <div class="reservation-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">预约车位</h1>
        <p class="page-desc">选择停车场和时间，轻松预约</p>
      </div>

      <n-card :bordered="false" style="border-radius: 12px; max-width: 720px; margin: 0 auto;">
        <n-steps :current="currentStep" style="margin-bottom: 36px;">
          <n-step title="选择停车场" />
          <n-step title="选择车辆" />
          <n-step title="选择时间" />
          <n-step title="确认预约" />
        </n-steps>

        <!-- Step 1: Select Lot -->
        <div v-show="currentStep === 1">
          <n-form-item label="选择停车场" label-placement="top">
            <n-select
              v-model:value="formData.lotId"
              :options="lotOptions"
              placeholder="请选择停车场"
              filterable
              size="large"
            />
          </n-form-item>
          <div v-if="formData.lotId" style="margin-top: 16px;">
            <n-spin :show="loadingSpaces">
              <p style="color: #666; font-size: 14px;">
                当前可用车位：
                <n-tag type="success" size="small">{{ availableSpaces.length }} 个</n-tag>
              </p>
            </n-spin>
          </div>
        </div>

        <!-- Step 2: Select Vehicle -->
        <div v-show="currentStep === 2">
          <n-form-item label="选择车辆" label-placement="top">
            <n-select
              v-model:value="formData.vehicleId"
              :options="vehicleOptions"
              placeholder="请选择您的车辆"
              size="large"
            />
          </n-form-item>
          <n-empty
            v-if="vehicleOptions.length === 0"
            description="暂无车辆，请先添加"
          >
            <template #extra>
              <n-button type="primary" size="small" @click="router.push('/my-vehicles')">去添加车辆</n-button>
            </template>
          </n-empty>
        </div>

        <!-- Step 3: Select Time -->
        <div v-show="currentStep === 3">
          <n-grid :cols="2" :x-gap="16">
            <n-gi>
              <n-form-item label="开始时间" label-placement="top">
                <n-date-picker
                  v-model:value="formData.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  :is-date-disabled="disablePreviousDate"
                  style="width: 100%;"
                />
              </n-form-item>
            </n-gi>
            <n-gi>
              <n-form-item label="结束时间" label-placement="top">
                <n-date-picker
                  v-model:value="formData.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  :is-date-disabled="disablePreviousDate"
                  style="width: 100%;"
                />
              </n-form-item>
            </n-gi>
          </n-grid>
        </div>

        <!-- Step 4: Confirm -->
        <div v-show="currentStep === 4">
          <div class="confirm-info">
            <div class="confirm-row">
              <span class="confirm-label">停车场</span>
              <span>{{ selectedLot?.name || '-' }}</span>
            </div>
            <div class="confirm-row">
              <span class="confirm-label">车牌号</span>
              <span>{{ formData.plateNumber || '-' }}</span>
            </div>
            <div class="confirm-row">
              <span class="confirm-label">开始时间</span>
              <span>{{ formData.startTime ? new Date(formData.startTime).toLocaleString() : '-' }}</span>
            </div>
            <div class="confirm-row">
              <span class="confirm-label">结束时间</span>
              <span>{{ formData.endTime ? new Date(formData.endTime).toLocaleString() : '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Navigation Buttons -->
        <n-space justify="center" style="margin-top: 32px;" :size="16">
          <n-button v-if="currentStep > 1" @click="prevStep" size="large">上一步</n-button>
          <n-button v-if="currentStep < 4" type="primary" @click="nextStep" size="large">下一步</n-button>
          <n-button v-if="currentStep === 4" type="primary" size="large" :loading="submitting" @click="handleSubmit">
            <template #icon><n-icon :component="CheckmarkCircleOutline" /></template>
            确认预约
          </n-button>
        </n-space>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.reservation-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.page-header {
  margin-bottom: 28px;
  text-align: center;
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
.confirm-info {
  background: #f7faf8;
  border-radius: 12px;
  padding: 24px;
}
.confirm-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #e8e8e8;
  font-size: 14px;
}
.confirm-row:last-child {
  border-bottom: none;
}
.confirm-label {
  color: #888;
  font-weight: 500;
}
</style>
