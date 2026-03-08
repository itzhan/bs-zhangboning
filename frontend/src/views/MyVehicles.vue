<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post, put, del } from '@/utils/request'
import {
  NCard, NGrid, NGi, NButton, NTag, NSpace, NSpin, NIcon, NModal,
  NForm, NFormItem, NInput, NEmpty, NPopconfirm
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { CarSportOutline, AddOutline, CreateOutline, TrashOutline, StarOutline } from '@vicons/ionicons5'

const vehicles = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = ref({
  id: null as number | null,
  plateNumber: '',
  brand: '',
  model: '',
  color: '',
  isDefault: false
})

const rules: FormRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5][A-Z][A-Z0-9]{5,6}$/, message: '请输入正确的车牌号', trigger: 'blur' }
  ],
}

function openAdd() {
  isEdit.value = false
  formData.value = { id: null, plateNumber: '', brand: '', model: '', color: '', isDefault: false }
  showModal.value = true
}

function openEdit(v: any) {
  isEdit.value = true
  formData.value = {
    id: v.id,
    plateNumber: v.plateNumber || v.plate_number || '',
    brand: v.brand || '',
    model: v.model || '',
    color: v.color || '',
    isDefault: v.isDefault ?? v.is_default ?? false
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch { return }

  submitting.value = true
  try {
    const payload = {
      plateNumber: formData.value.plateNumber,
      brand: formData.value.brand || null,
      model: formData.value.model || null,
      color: formData.value.color || null,
      isDefault: formData.value.isDefault ? 1 : 0
    }
    if (isEdit.value && formData.value.id) {
      await put(`/api/vehicles/${formData.value.id}`, payload)
      window.$message?.success('更新成功')
    } else {
      await post('/api/vehicles', payload)
      window.$message?.success('添加成功')
    }
    showModal.value = false
    fetchVehicles()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await del(`/api/vehicles/${id}`)
    window.$message?.success('已删除')
    fetchVehicles()
  } catch {
    // handled by interceptor
  }
}

async function fetchVehicles() {
  loading.value = true
  try {
    const res: any = await get('/api/vehicles')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    vehicles.value = Array.isArray(list) ? list : []
  } catch {
    vehicles.value = []
  } finally {
    loading.value = false
  }
}

const colorPresets = ['白色', '黑色', '灰色', '银色', '红色', '蓝色', '绿色', '黄色', '橙色', '棕色']

onMounted(fetchVehicles)
</script>

<template>
  <div class="vehicles-page">
    <div class="container">
      <div class="page-header">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h1 class="page-title">我的车辆</h1>
            <p class="page-desc">管理您的车辆信息</p>
          </div>
          <n-button type="primary" @click="openAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            添加车辆
          </n-button>
        </div>
      </div>

      <n-spin :show="loading">
        <n-grid :cols="3" :x-gap="20" :y-gap="20" responsive="screen" item-responsive>
          <n-gi v-for="v in vehicles" :key="v.id" span="3 m:3 l:1">
            <n-card class="vehicle-card" :bordered="false" hoverable>
              <div class="vehicle-header">
                <n-icon :component="CarSportOutline" :size="28" color="#18a058" />
                <span class="plate-number">{{ v.plateNumber || v.plate_number }}</span>
                <n-tag v-if="v.isDefault || v.is_default" type="warning" size="tiny" round>
                  <template #icon><n-icon :component="StarOutline" :size="12" /></template>
                  默认
                </n-tag>
              </div>
              <div class="vehicle-info">
                <p v-if="v.brand"><span class="label">品牌：</span>{{ v.brand }}</p>
                <p v-if="v.model"><span class="label">型号：</span>{{ v.model }}</p>
                <p v-if="v.color"><span class="label">颜色：</span>{{ v.color }}</p>
              </div>
              <div class="vehicle-actions">
                <n-button text type="primary" size="small" @click="openEdit(v)">
                  <template #icon><n-icon :component="CreateOutline" /></template>
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(v.id)">
                  <template #trigger>
                    <n-button text type="error" size="small">
                      <template #icon><n-icon :component="TrashOutline" /></template>
                      删除
                    </n-button>
                  </template>
                  确定删除该车辆吗？
                </n-popconfirm>
              </div>
            </n-card>
          </n-gi>
        </n-grid>
        <n-empty v-if="!loading && vehicles.length === 0" description="暂无车辆，请添加" style="margin-top: 60px;">
          <template #extra>
            <n-button type="primary" size="small" @click="openAdd">添加车辆</n-button>
          </template>
        </n-empty>
      </n-spin>

      <!-- Add/Edit Modal -->
      <n-modal
        v-model:show="showModal"
        preset="card"
        :title="isEdit ? '编辑车辆' : '添加车辆'"
        style="max-width: 480px; border-radius: 12px;"
      >
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
          <n-form-item label="车牌号" path="plateNumber">
            <n-input v-model:value="formData.plateNumber" placeholder="如：京A12345" />
          </n-form-item>
          <n-form-item label="品牌" path="brand">
            <n-input v-model:value="formData.brand" placeholder="如：丰田" />
          </n-form-item>
          <n-form-item label="型号" path="model">
            <n-input v-model:value="formData.model" placeholder="如：卡罗拉" />
          </n-form-item>
          <n-form-item label="颜色" path="color">
            <n-input v-model:value="formData.color" placeholder="如：白色" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showModal = false">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存' : '添加' }}
            </n-button>
          </n-space>
        </template>
      </n-modal>
    </div>
  </div>
</template>

<style scoped>
.vehicles-page {
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
.vehicle-card {
  border-radius: 12px;
  transition: transform 0.2s;
}
.vehicle-card:hover {
  transform: translateY(-2px);
}
.vehicle-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.plate-number {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  letter-spacing: 1px;
}
.vehicle-info {
  padding: 8px 0;
}
.vehicle-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #666;
}
.vehicle-info .label {
  color: #999;
}
.vehicle-actions {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f3f3;
}
</style>
