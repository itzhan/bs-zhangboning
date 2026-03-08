<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { get, put } from '@/utils/request'
import {
  NCard, NDataTable, NTag, NButton, NSpace, NSpin, NPagination, NEmpty, NPopconfirm
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import dayjs from 'dayjs'

const reservations = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap: Record<string | number, { label: string; type: 'info' | 'success' | 'default' | 'error' | 'warning' }> = {
  PENDING: { label: '待生效', type: 'info' },
  ACTIVE: { label: '已生效', type: 'success' },
  EFFECTIVE: { label: '已生效', type: 'success' },
  COMPLETED: { label: '已完成', type: 'default' },
  CANCELLED: { label: '已取消', type: 'error' },
  EXPIRED: { label: '已过期', type: 'default' },
  0: { label: '待使用', type: 'info' },
  1: { label: '使用中', type: 'success' },
  2: { label: '已完成', type: 'default' },
  3: { label: '已取消', type: 'error' },
  4: { label: '已过期', type: 'default' },
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'
}

function getStatus(status: string | number) {
  return statusMap[status] || { label: status != null ? String(status) : '未知', type: 'info' as const }
}

const columns: DataTableColumns<any> = [
  { title: '停车场', key: 'lotName', ellipsis: { tooltip: true },
    render: (row) => h('span', {}, row.lotName || row.lot_name || row.parkingLotName || '-')
  },
  { title: '车牌号', key: 'plateNumber',
    render: (row) => h('span', {}, row.plateNumber || row.plate_number || '-')
  },
  { title: '预约开始', key: 'startTime', width: 160,
    render: (row) => h('span', {}, formatTime(row.startTime || row.start_time))
  },
  { title: '预约结束', key: 'endTime', width: 160,
    render: (row) => h('span', {}, formatTime(row.endTime || row.end_time))
  },
  {
    title: '状态', key: 'status', width: 100,
    render: (row) => {
      const s = getStatus(row.status)
      return h(NTag, { type: s.type, size: 'small', round: true }, { default: () => s.label })
    }
  },
  {
    title: '操作', key: 'action', width: 120,
    render: (row) => {
      if (row.status === 'PENDING' || row.status === 'ACTIVE' || row.status === 'EFFECTIVE' || row.status === 0 || row.status === 1) {
        return h(
          NPopconfirm,
          { onPositiveClick: () => handleCancel(row) },
          {
            trigger: () => h(NButton, { type: 'error', size: 'small', ghost: true }, { default: () => '取消预约' }),
            default: () => '确定取消该预约吗？'
          }
        )
      }
      return h('span', { style: { color: '#ccc' } }, '-')
    }
  }
]

async function handleCancel(row: any) {
  try {
    await put(`/api/reservations/${row.id}/cancel`)
    window.$message?.success('已取消预约')
    fetchReservations()
  } catch {
    // handled by interceptor
  }
}

async function fetchReservations() {
  loading.value = true
  try {
    const res: any = await get('/api/reservations', { page: page.value, pageSize: pageSize.value, size: pageSize.value })
    const data = res.data || res
    if (data.list) {
      reservations.value = data.list
      total.value = data.total || data.list.length
    } else if (data.records) {
      reservations.value = data.records
      total.value = data.total || data.records.length
    } else if (Array.isArray(data)) {
      reservations.value = data
      total.value = data.length
    } else {
      reservations.value = []
      total.value = 0
    }
  } catch {
    reservations.value = []
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchReservations()
}

onMounted(fetchReservations)
</script>

<template>
  <div class="reservations-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">我的预约</h1>
        <p class="page-desc">管理您的车位预约记录</p>
      </div>

      <n-card :bordered="false" style="border-radius: 12px;">
        <n-spin :show="loading">
          <n-data-table
            :columns="columns"
            :data="reservations"
            :bordered="false"
            :single-line="false"
            size="small"
            style="min-height: 200px;"
          />
          <n-empty v-if="!loading && reservations.length === 0" description="暂无预约记录" style="padding: 40px 0;" />
        </n-spin>
        <div style="display: flex; justify-content: flex-end; margin-top: 16px;" v-if="total > pageSize">
          <n-pagination
            v-model:page="page"
            :page-size="pageSize"
            :item-count="total"
            @update:page="handlePageChange"
          />
        </div>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.reservations-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.page-header {
  margin-bottom: 24px;
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
</style>
