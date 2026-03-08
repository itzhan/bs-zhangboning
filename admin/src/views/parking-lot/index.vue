<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import {
  NCard,
  NButton,
  NDataTable,
  NInput,
  NSelect,
  NModal,
  NForm,
  NFormItem,
  NInputNumber,
  NTag,
  NSpace,
  NTimePicker
} from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'ParkingLotManage' });

// ─── 状态定义 ───
const loading = ref(false);
const tableData = ref<any[]>([]);
const showModal = ref(false);
const isEdit = ref(false);
const formRef = ref<any>(null);

// ─── 搜索条件 ───
const searchParams = reactive({
  keyword: '',
  status: null as number | null
});

// ─── 分页 ───
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  prefix: ({ itemCount }: { itemCount: number }) => `共 ${itemCount} 条`,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page;
    fetchData();
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    fetchData();
  }
});

// ─── 表单模型 ───
const defaultForm = () => ({
  id: null as number | null,
  name: '',
  address: '',
  longitude: null as number | null,
  latitude: null as number | null,
  totalSpaces: null as number | null,
  openTime: '08:00' as string | null,
  closeTime: '22:00' as string | null,
  contactPhone: '',
  description: '',
  status: 0
});
const formModel = reactive(defaultForm());

// ─── 状态选项 ───
const statusOptions = [
  { label: '运营中', value: 1 },
  { label: '关闭', value: 0 },
  { label: '维护中', value: 2 }
];

const statusMap: Record<number, { label: string; type: 'success' | 'error' | 'warning' }> = {
  1: { label: '运营中', type: 'success' },
  0: { label: '关闭', type: 'error' },
  2: { label: '维护中', type: 'warning' }
};

// ─── 表单校验规则 ───
const formRules = {
  name: { required: true, message: '请输入停车场名称', trigger: 'blur' },
  address: { required: true, message: '请输入地址', trigger: 'blur' },
  totalSpaces: { required: true, type: 'number', message: '请输入总车位数', trigger: 'blur' },
  contactPhone: { required: true, message: '请输入联系电话', trigger: 'blur' }
};

// ─── 表格列定义 ───
const columns: DataTableColumns = [
  { title: '名称', key: 'name', width: 150, ellipsis: { tooltip: true } },
  { title: '地址', key: 'address', width: 200, ellipsis: { tooltip: true } },
  { title: '总车位', key: 'totalSpaces', width: 80, align: 'center' },
  { title: '可用车位', key: 'availableSpaces', width: 90, align: 'center' },
  {
    title: '开放时间',
    key: 'openTime',
    width: 160,
    align: 'center',
    render(row: any) {
      return `${row.openTime || '--'} ~ ${row.closeTime || '--'}`;
    }
  },
  { title: '联系电话', key: 'contactPhone', width: 130 },
  { title: '计费规则', key: 'billingRuleName', width: 120, ellipsis: { tooltip: true },
    render(row: any) {
      return row.billingRuleName || '无';
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    align: 'center',
    render(row: any) {
      const info = statusMap[row.status as number] || { label: '未知', type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small', round: true }, { default: () => info.label });
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    align: 'center',
    render(row: any) {
      return h(NSpace, { justify: 'center' }, () => [
        h(
          NButton,
          { size: 'small', type: 'primary', quaternary: true, onClick: () => handleEdit(row) },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          { size: 'small', type: 'error', quaternary: true, onClick: () => handleDelete(row) },
          { default: () => '删除' }
        )
      ]);
    }
  }
];

// ─── 数据获取 ───
async function fetchData() {
  loading.value = true;
  const { data, error } = await request({
    url: '/api/parking-lots',
    params: {
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status ?? undefined
    }
  });
  loading.value = false;
  if (!error && data) {
    tableData.value = data.list || data.records || data.content || [];
    pagination.itemCount = data.total || data.totalElements || 0;
  }
}

// ─── 搜索 & 重置 ───
function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  searchParams.keyword = '';
  searchParams.status = null;
  handleSearch();
}

// ─── 新增 ───
function handleAdd() {
  isEdit.value = false;
  Object.assign(formModel, defaultForm());
  showModal.value = true;
}

// ─── 编辑 ───
function handleEdit(row: any) {
  isEdit.value = true;
  Object.assign(formModel, {
    id: row.id,
    name: row.name,
    address: row.address,
    longitude: row.longitude,
    latitude: row.latitude,
    totalSpaces: row.totalSpaces,
    openTime: row.openTime || '08:00',
    closeTime: row.closeTime || '22:00',
    contactPhone: row.contactPhone,
    description: row.description,
    status: row.status
  });
  showModal.value = true;
}

// ─── 删除 ───
function handleDelete(row: any) {
  window.$dialog?.warning({
    title: '确认',
    content: `确认删除停车场「${row.name}」？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/parking-lots/${row.id}`, method: 'delete' });
      if (!error) {
        window.$message?.success('删除成功');
        fetchData();
      }
    }
  });
}

// ─── 提交表单 ───
async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  const body = { ...formModel };
  delete (body as any).id;

  if (isEdit.value) {
    const { error } = await request({ url: `/api/parking-lots/${formModel.id}`, method: 'put', data: body });
    if (!error) {
      window.$message?.success('更新成功');
      showModal.value = false;
      fetchData();
    }
  } else {
    const { error } = await request({ url: '/api/parking-lots', method: 'post', data: body });
    if (!error) {
      window.$message?.success('新增成功');
      showModal.value = false;
      fetchData();
    }
  }
}

// ─── 时间选择辅助 ───
function timeStringToMs(time: string | null): number | null {
  if (!time) return null;
  const [hours, minutes] = time.split(':').map(Number);
  return (hours * 3600 + minutes * 60) * 1000;
}

function msToTimeString(ms: number | null): string | null {
  if (ms === null || ms === undefined) return null;
  const totalSeconds = ms / 1000;
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
}

// ─── 初始化 ───
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="停车场管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NButton type="primary" @click="handleAdd">
          <template #icon>
            <icon-mdi-plus />
          </template>
          新增
        </NButton>
      </template>

      <!-- 搜索栏 -->
      <div class="mb-16px flex flex-wrap items-center gap-12px">
        <NInput
          v-model:value="searchParams.keyword"
          placeholder="搜索名称/地址"
          clearable
          class="w-200px"
          @keyup.enter="handleSearch"
        />
        <NSelect
          v-model:value="searchParams.status"
          :options="statusOptions"
          placeholder="状态"
          clearable
          class="w-140px"
        />
        <NButton type="primary" @click="handleSearch">查询</NButton>
        <NButton @click="handleReset">重置</NButton>
      </div>

      <!-- 数据表格 -->
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :scroll-x="1050"
        remote
        striped
      />
    </NCard>

    <!-- 新增/编辑弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="isEdit ? '编辑停车场' : '新增停车场'"
      preset="card"
      class="w-640px"
      :mask-closable="false"
    >
      <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="90">
        <NFormItem label="名称" path="name">
          <NInput v-model:value="formModel.name" placeholder="请输入停车场名称" />
        </NFormItem>
        <NFormItem label="地址" path="address">
          <NInput v-model:value="formModel.address" placeholder="请输入地址" />
        </NFormItem>
        <div class="flex gap-12px">
          <NFormItem label="经度" path="longitude" class="flex-1">
            <NInputNumber v-model:value="formModel.longitude" placeholder="经度" :precision="6" class="w-full" />
          </NFormItem>
          <NFormItem label="纬度" path="latitude" class="flex-1">
            <NInputNumber v-model:value="formModel.latitude" placeholder="纬度" :precision="6" class="w-full" />
          </NFormItem>
        </div>
        <NFormItem label="总车位" path="totalSpaces">
          <NInputNumber v-model:value="formModel.totalSpaces" placeholder="请输入总车位数" :min="0" class="w-full" />
        </NFormItem>
        <div class="flex gap-12px">
          <NFormItem label="开放时间" path="openTime" class="flex-1">
            <NTimePicker
              :value="timeStringToMs(formModel.openTime)"
              format="HH:mm"
              class="w-full"
              @update:value="(v: number | null) => (formModel.openTime = msToTimeString(v))"
            />
          </NFormItem>
          <NFormItem label="关闭时间" path="closeTime" class="flex-1">
            <NTimePicker
              :value="timeStringToMs(formModel.closeTime)"
              format="HH:mm"
              class="w-full"
              @update:value="(v: number | null) => (formModel.closeTime = msToTimeString(v))"
            />
          </NFormItem>
        </div>
        <NFormItem label="联系电话" path="contactPhone">
          <NInput v-model:value="formModel.contactPhone" placeholder="请输入联系电话" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formModel.description" type="textarea" placeholder="请输入描述" :rows="3" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSelect v-model:value="formModel.status" :options="statusOptions" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
