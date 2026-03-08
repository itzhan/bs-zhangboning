<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { get, post } from '@/utils/request'
import {
  NCard, NDataTable, NTag, NButton, NSpace, NSpin, NPagination,
  NModal, NDescriptions, NDescriptionsItem, NEmpty
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import dayjs from 'dayjs'

const orders = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const showDetail = ref(false)
const currentOrder = ref<any>({})

const statusMap: Record<string | number, { label: string; type: 'info' | 'warning' | 'success' | 'error' }> = {
  ENTERED: { label: '已入场', type: 'info' },
  EXITED: { label: '已出场', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  PAID: { label: '已完成', type: 'success' },
  ABNORMAL: { label: '异常', type: 'error' },
  UNPAID: { label: '待支付', type: 'warning' },
  0: { label: '已入场', type: 'info' },
  1: { label: '已出场', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  3: { label: '异常', type: 'error' },
  4: { label: '待支付', type: 'warning' },
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'
}

function getStatus(status: string | number) {
  return statusMap[status] || { label: status != null ? String(status) : '未知', type: 'info' as const }
}

const columns: DataTableColumns<any> = [
  { title: '订单号', key: 'orderNo', ellipsis: { tooltip: true }, width: 180,
    render: (row) => h('span', {}, row.orderNo || row.order_no || '-')
  },
  { title: '停车场', key: 'lotName', ellipsis: { tooltip: true },
    render: (row) => h('span', {}, row.lotName || row.lot_name || row.parkingLotName || '-')
  },
  { title: '车牌号', key: 'plateNumber',
    render: (row) => h('span', {}, row.plateNumber || row.plate_number || '-')
  },
  { title: '入场时间', key: 'entryTime', width: 150,
    render: (row) => h('span', {}, formatTime(row.entryTime || row.entry_time))
  },
  { title: '出场时间', key: 'exitTime', width: 150,
    render: (row) => h('span', {}, formatTime(row.exitTime || row.exit_time))
  },
  { title: '时长', key: 'duration',
    render: (row) => {
      const d = row.duration || row.parkingDuration
      return h('span', {}, d ? `${d}分钟` : '-')
    }
  },
  { title: '金额', key: 'amount',
    render: (row) => {
      const amt = row.actualAmount ?? row.actual_amount ?? row.amount
      return h('span', { style: { fontWeight: '600', color: '#d03050' } }, amt != null ? `¥${amt}` : '-')
    }
  },
  {
    title: '状态', key: 'status', width: 100,
    render: (row) => {
      const s = getStatus(row.status)
      return h(NTag, { type: s.type, size: 'small', round: true }, { default: () => s.label })
    }
  },
  {
    title: '操作', key: 'action', width: 140,
    render: (row) => {
      const btns = []
      btns.push(
        h(NButton, { text: true, type: 'primary', size: 'small', onClick: () => viewDetail(row) }, { default: () => '详情' })
      )
      if (row.status === 'UNPAID' || row.status === 'EXITED' || row.status === 4 || row.status === 1) {
        btns.push(
          h(NButton, {
            type: 'primary', size: 'small',
            onClick: () => handlePay(row),
            loading: row._paying
          }, { default: () => '去支付' })
        )
      }
      return h(NSpace, { size: 8 }, { default: () => btns })
    }
  }
]

function viewDetail(order: any) {
  currentOrder.value = order
  showDetail.value = true
}

async function handlePay(order: any) {
  order._paying = true
  try {
    await post('/api/payments/pay', { orderId: order.id })
    window.$message?.success('支付成功')
    fetchOrders()
  } catch {
    // handled in interceptor
  } finally {
    order._paying = false
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const res: any = await get('/api/orders', { page: page.value, pageSize: pageSize.value, size: pageSize.value })
    const data = res.data || res
    if (data.list) {
      orders.value = data.list
      total.value = data.total || data.list.length
    } else if (data.records) {
      orders.value = data.records
      total.value = data.total || data.records.length
    } else if (Array.isArray(data)) {
      orders.value = data
      total.value = data.length
    } else {
      orders.value = []
      total.value = 0
    }
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchOrders()
}

onMounted(fetchOrders)
</script>

<template>
  <div class="orders-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">我的订单</h1>
        <p class="page-desc">查看您的停车记录和支付状态</p>
      </div>

      <n-card :bordered="false" style="border-radius: 12px;">
        <n-spin :show="loading">
          <n-data-table
            :columns="columns"
            :data="orders"
            :bordered="false"
            :single-line="false"
            size="small"
            style="min-height: 200px;"
          />
          <n-empty v-if="!loading && orders.length === 0" description="暂无订单记录" style="padding: 40px 0;" />
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

      <!-- Order Detail Modal -->
      <n-modal v-model:show="showDetail" preset="card" title="订单详情" style="max-width: 560px; border-radius: 12px;">
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="订单号">{{ currentOrder.orderNo || currentOrder.order_no || '-' }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="getStatus(currentOrder.status).type" size="small">
              {{ getStatus(currentOrder.status).label }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="停车场">{{ currentOrder.lotName || currentOrder.lot_name || currentOrder.parkingLotName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="车牌号">{{ currentOrder.plateNumber || currentOrder.plate_number || '-' }}</n-descriptions-item>
          <n-descriptions-item label="入场时间">{{ formatTime(currentOrder.entryTime || currentOrder.entry_time) }}</n-descriptions-item>
          <n-descriptions-item label="出场时间">{{ formatTime(currentOrder.exitTime || currentOrder.exit_time) }}</n-descriptions-item>
          <n-descriptions-item label="停车时长">{{ currentOrder.duration || currentOrder.parkingDuration || '-' }} 分钟</n-descriptions-item>
          <n-descriptions-item label="实付金额">
            <span style="color: #d03050; font-weight: 600;">
              ¥{{ currentOrder.actualAmount ?? currentOrder.actual_amount ?? currentOrder.amount ?? '-' }}
            </span>
          </n-descriptions-item>
        </n-descriptions>
      </n-modal>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
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
