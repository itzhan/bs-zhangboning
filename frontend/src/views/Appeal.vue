<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { get, post } from '@/utils/request'
import {
  NCard, NForm, NFormItem, NInput, NSelect, NButton, NSpace, NSpin,
  NUpload, NDataTable, NTag, NEmpty, NDivider, NIcon
} from 'naive-ui'
import type { FormInst, FormRules, UploadFileInfo, DataTableColumns } from 'naive-ui'
import { CloudUploadOutline, DocumentTextOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'

const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const loading = ref(false)

const orderOptions = ref<any[]>([])
const appeals = ref<any[]>([])

const formData = ref({
  orderId: null as number | null,
  type: null as number | null,
  content: '',
  images: [] as string[]
})

const typeOptions = [
  { label: '费用异常', value: 1 },
  { label: '入场异常', value: 2 },
  { label: '出场异常', value: 3 },
  { label: '其他', value: 4 },
]

const rules: FormRules = {
  orderId: { required: true, type: 'number', message: '请选择订单', trigger: 'change' },
  type: { required: true, type: 'number', message: '请选择申诉类型', trigger: 'change' },
  content: { required: true, message: '请填写申诉内容', trigger: 'blur' },
}

const appealStatusMap: Record<string | number, { label: string; type: 'info' | 'warning' | 'success' | 'error' }> = {
  PENDING: { label: '待处理', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'info' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'error' },
  COMPLETED: { label: '已完成', type: 'success' },
  0: { label: '待处理', type: 'warning' },
  1: { label: '处理中', type: 'info' },
  2: { label: '已通过', type: 'success' },
  3: { label: '已驳回', type: 'error' },
}

const typeMap: Record<number, string> = {
  1: '费用异常',
  2: '入场异常',
  3: '出场异常',
  4: '其他',
}

function getAppealStatus(status: string | number) {
  return appealStatusMap[status] || { label: status != null ? String(status) : '未知', type: 'info' as const }
}

const columns: DataTableColumns<any> = [
  { title: '订单号', key: 'orderNo', ellipsis: { tooltip: true },
    render: (row) => h('span', {}, row.orderNo || row.order_no || '-')
  },
  { title: '类型', key: 'type',
    render: (row) => h(NTag, { size: 'small', bordered: false }, { default: () => typeMap[row.type] || row.type || '-' })
  },
  { title: '内容', key: 'content', ellipsis: { tooltip: true },
    render: (row) => h('span', {}, row.content || '-')
  },
  { title: '状态', key: 'status',
    render: (row) => {
      const s = getAppealStatus(row.status)
      return h(NTag, { type: s.type, size: 'small', round: true }, { default: () => s.label })
    }
  },
  { title: '回复', key: 'reply', ellipsis: { tooltip: true },
    render: (row) => h('span', { style: { color: row.reply ? '#333' : '#ccc' } }, row.reply || '暂无回复')
  },
  { title: '提交时间', key: 'createdAt',
    render: (row) => h('span', {}, row.createdAt || row.create_time ? dayjs(row.createdAt || row.create_time).format('YYYY-MM-DD HH:mm') : '-')
  },
]

const fileList = ref<UploadFileInfo[]>([])

function handleUploadFinish({ file, event }: { file: UploadFileInfo; event?: ProgressEvent }) {
  try {
    const res = JSON.parse((event?.target as XMLHttpRequest)?.response)
    const url = res.data?.url || res.url || res.data
    if (url) {
      formData.value.images.push(url)
    }
  } catch {
    // ignore
  }
  return file
}

async function fetchOrders() {
  try {
    const res: any = await get('/api/orders')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    orderOptions.value = (Array.isArray(list) ? list : []).map((o: any) => ({
      label: `${o.orderNo || o.order_no || '#' + o.id} - ${o.lotName || o.lot_name || o.parkingLotName || ''}`,
      value: o.id
    }))
  } catch {
    orderOptions.value = []
  }
}

async function fetchAppeals() {
  loading.value = true
  try {
    const res: any = await get('/api/appeals')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    appeals.value = Array.isArray(list) ? list : []
  } catch {
    appeals.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch { return }

  submitting.value = true
  try {
    await post('/api/appeals', {
      orderId: formData.value.orderId,
      type: formData.value.type,
      content: formData.value.content,
      images: formData.value.images.join(',')
    })
    window.$message?.success('申诉提交成功')
    formData.value = { orderId: null, type: null, content: '', images: [] }
    fileList.value = []
    fetchAppeals()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOrders()
  fetchAppeals()
})
</script>

<template>
  <div class="appeal-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">异常申诉</h1>
        <p class="page-desc">如遇异常情况，请提交申诉，我们将尽快处理</p>
      </div>

      <!-- Appeal Form -->
      <n-card :bordered="false" style="border-radius: 12px; max-width: 720px;" title="提交申诉">
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80">
          <n-form-item label="关联订单" path="orderId">
            <n-select
              v-model:value="formData.orderId"
              :options="orderOptions"
              placeholder="请选择关联的订单"
              filterable
            />
          </n-form-item>
          <n-form-item label="申诉类型" path="type">
            <n-select
              v-model:value="formData.type"
              :options="typeOptions"
              placeholder="请选择申诉类型"
            />
          </n-form-item>
          <n-form-item label="申诉内容" path="content">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              :rows="4"
              placeholder="请详细描述异常情况"
            />
          </n-form-item>
          <n-form-item label="图片上传">
            <n-upload
              v-model:file-list="fileList"
              action="/api/upload"
              list-type="image-card"
              :max="3"
              accept="image/*"
              @finish="handleUploadFinish"
            >
              <n-icon :component="CloudUploadOutline" :size="24" color="#999" />
            </n-upload>
          </n-form-item>
          <n-form-item>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">
              <template #icon><n-icon :component="DocumentTextOutline" /></template>
              提交申诉
            </n-button>
          </n-form-item>
        </n-form>
      </n-card>

      <!-- My Appeals -->
      <n-divider />
      <h2 style="font-size: 20px; font-weight: 600; color: #333; margin-bottom: 16px;">我的申诉记录</h2>
      <n-card :bordered="false" style="border-radius: 12px;">
        <n-spin :show="loading">
          <n-data-table
            :columns="columns"
            :data="appeals"
            :bordered="false"
            :single-line="false"
            size="small"
          />
          <n-empty v-if="!loading && appeals.length === 0" description="暂无申诉记录" style="padding: 30px 0;" />
        </n-spin>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.appeal-page {
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
