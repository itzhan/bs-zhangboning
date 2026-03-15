<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NTag
} from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'CouponManage' });

function formatTime(val: any) {
  if (!val) return '--';
  return String(val).substring(0, 16);
}

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });
const showModal = ref(false);
const isEdit = ref(false);

const defaultForm = () => ({
  id: undefined as number | undefined,
  name: '',
  type: 1,
  value: 0,
  minAmount: 0,
  startTime: null as string | null,
  endTime: null as string | null,
  total: 0,
  status: 1
});

const formModel = ref(defaultForm());

/* ============ options ============ */
const typeOptions = [
  { label: '满减', value: 1 },
  { label: '折扣', value: 2 },
  { label: '免费时长', value: 3 }
];

function typeDisplay(type: number, value: number) {
  if (type === 1) return `减${value}元`;
  if (type === 2) return `${value}折`;
  if (type === 3) return `免费${value}分钟`;
  return String(value);
}

const typeTagMap: Record<number, 'success' | 'info' | 'warning'> = {
  1: 'success',
  2: 'info',
  3: 'warning'
};

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '停用', type: 'error' }
};

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '名称', key: 'name', minWidth: 120 },
  {
    title: '类型',
    key: 'type',
    minWidth: 80,
    render: row =>
      h(
        NTag,
        { size: 'small', type: typeTagMap[row.type] || 'default' },
        { default: () => typeOptions.find(o => o.value === row.type)?.label || '未知' }
      )
  },
  {
    title: '优惠值',
    key: 'value',
    minWidth: 110,
    render: row => typeDisplay(row.type, row.value)
  },
  { title: '最低金额', key: 'minAmount', minWidth: 90, render: row => `¥${row.minAmount ?? 0}` },
  { title: '生效时间', key: 'startTime', minWidth: 150, render: (row: any) => formatTime(row.startTime) },
  { title: '失效时间', key: 'endTime', minWidth: 150, render: (row: any) => formatTime(row.endTime) },
  { title: '总量', key: 'totalCount', minWidth: 70 },
  { title: '已用', key: 'usedCount', minWidth: 70 },
  {
    title: '剩余',
    key: 'remaining',
    minWidth: 70,
    render: row => (row.totalCount ?? 0) - (row.usedCount ?? 0)
  },
  {
    title: '状态',
    key: 'status',
    minWidth: 80,
    render: row => {
      const s = statusMap[row.status] || { label: '未知', type: 'default' as const };
      return h(NTag, { size: 'small', type: s.type }, { default: () => s.label });
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: row =>
      h(NSpace, { size: 8 }, () => [
        h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
      ])
  }
];

/* ============ methods ============ */
async function fetchData() {
  loading.value = true;
  const { data, error } = await request({
    url: '/api/coupons',
    params: { page: pagination.page, size: pagination.pageSize }
  });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? [];
  pagination.itemCount = data?.total ?? 0;
}

function openAdd() {
  isEdit.value = false;
  formModel.value = defaultForm();
  showModal.value = true;
}

function openEdit(row: any) {
  isEdit.value = true;
  formModel.value = { ...row };
  showModal.value = true;
}

async function handleSubmit() {
  const payload = { ...formModel.value };
  if (isEdit.value) {
    const { error } = await request({
      url: `/api/coupons/${payload.id}`,
      method: 'put',
      data: payload
    });
    if (error) {
      window.$message?.error('操作失败');
      return;
    }
    window.$message?.success('修改成功');
  } else {
    const { error } = await request({
      url: '/api/coupons',
      method: 'post',
      data: payload
    });
    if (error) {
      window.$message?.error('操作失败');
      return;
    }
    window.$message?.success('新增成功');
  }
  showModal.value = false;
  fetchData();
}

function handleDelete(id: number) {
  window.$dialog?.warning({
    title: '确认删除',
    content: '确定删除该优惠券吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/coupons/${id}`, method: 'delete' });
      if (error) {
        window.$message?.error('删除失败');
        return;
      }
      window.$message?.success('删除成功');
      fetchData();
    }
  });
}

function handlePageChange(page: number) {
  pagination.page = page;
  fetchData();
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize;
  pagination.page = 1;
  fetchData();
}

/* date-picker helpers */
function handleStartTimeUpdate(ts: number | null) {
  formModel.value.startTime = ts ? new Date(ts).toISOString().replace('T', ' ').substring(0, 19) : null;
}
function handleEndTimeUpdate(ts: number | null) {
  formModel.value.endTime = ts ? new Date(ts).toISOString().replace('T', ' ').substring(0, 19) : null;
}

/* ============ init ============ */
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="优惠券管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NButton type="primary" @click="openAdd">新增优惠券</NButton>
      </template>

      <NDataTable
        :loading="loading"
        :columns="columns"
        :data="tableData"
        :pagination="{
          page: pagination.page,
          pageSize: pagination.pageSize,
          itemCount: pagination.itemCount,
          showSizePicker: true,
          pageSizes: [10, 20, 50],
          onChange: handlePageChange,
          onUpdatePageSize: handlePageSizeChange
        }"
        :scroll-x="1200"
        remote
        flex-height
        class="sm:h-full"
      />
    </NCard>

    <!-- Add / Edit Modal -->
    <NModal v-model:show="showModal" preset="card" :title="isEdit ? '编辑优惠券' : '新增优惠券'" style="width: 600px">
      <NForm :model="formModel" label-placement="left" label-width="100px">
        <NFormItem label="名称" path="name">
          <NInput v-model:value="formModel.name" placeholder="请输入名称" />
        </NFormItem>
        <NFormItem label="类型" path="type">
          <NSelect v-model:value="formModel.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem label="优惠值" path="value">
          <NInputNumber v-model:value="formModel.value" :min="0" :precision="2" style="width: 100%" />
        </NFormItem>
        <NFormItem label="最低金额" path="minAmount">
          <NInputNumber v-model:value="formModel.minAmount" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="生效时间" path="startTime">
          <NDatePicker
            type="datetime"
            :value="formModel.startTime ? new Date(formModel.startTime).getTime() : null"
            style="width: 100%"
            @update:value="handleStartTimeUpdate"
          />
        </NFormItem>
        <NFormItem label="失效时间" path="endTime">
          <NDatePicker
            type="datetime"
            :value="formModel.endTime ? new Date(formModel.endTime).getTime() : null"
            style="width: 100%"
            @update:value="handleEndTimeUpdate"
          />
        </NFormItem>
        <NFormItem label="总量" path="total">
          <NInputNumber v-model:value="formModel.total" :min="0" :precision="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSwitch v-model:value="formModel.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>停用</template>
          </NSwitch>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
