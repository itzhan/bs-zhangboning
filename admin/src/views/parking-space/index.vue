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
  NSpace
} from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'ParkingSpaceManage' });

// ─── 状态定义 ───
const loading = ref(false);
const tableData = ref<any[]>([]);
const showModal = ref(false);
const showBatchModal = ref(false);
const isEdit = ref(false);
const formRef = ref<any>(null);
const batchFormRef = ref<any>(null);
const lotOptions = ref<{ label: string; value: number }[]>([]);

// ─── 搜索条件 ───
const searchParams = reactive({
  lotId: null as number | null,
  status: null as number | null,
  type: null as number | null
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

// ─── 类型 & 状态选项 ───
const typeOptions = [
  { label: '普通', value: 0 },
  { label: 'VIP', value: 1 },
  { label: '残疾人', value: 2 },
  { label: '充电桩', value: 3 }
];

const typeMap: Record<number, { label: string; type: 'default' | 'info' | 'warning' | 'success' }> = {
  0: { label: '普通', type: 'default' },
  1: { label: 'VIP', type: 'warning' },
  2: { label: '残疾人', type: 'info' },
  3: { label: '充电桩', type: 'success' }
};

const statusOptions = [
  { label: '空闲', value: 0 },
  { label: '占用', value: 1 },
  { label: '预约', value: 2 },
  { label: '维护', value: 3 }
];

const statusMap: Record<number, { label: string; type: 'success' | 'error' | 'warning' | 'info' }> = {
  0: { label: '空闲', type: 'success' },
  1: { label: '占用', type: 'error' },
  2: { label: '预约', type: 'warning' },
  3: { label: '维护', type: 'info' }
};

// ─── 表单模型 ───
const defaultForm = () => ({
  id: null as number | null,
  lotId: null as number | null,
  spaceNo: '',
  type: 0,
  floor: null as number | null,
  area: '',
  status: 0
});
const formModel = reactive(defaultForm());

// ─── 批量创建表单 ───
const defaultBatchForm = () => ({
  lotId: null as number | null,
  prefix: '',
  count: 10,
  type: 0,
  floor: null as number | null,
  area: ''
});
const batchFormModel = reactive(defaultBatchForm());

// ─── 表单校验 ───
const formRules = {
  lotId: { required: true, type: 'number', message: '请选择所属停车场', trigger: 'change' },
  spaceNo: { required: true, message: '请输入车位编号', trigger: 'blur' },
  type: { required: true, type: 'number', message: '请选择类型', trigger: 'change' }
};

const batchFormRules = {
  lotId: { required: true, type: 'number', message: '请选择所属停车场', trigger: 'change' },
  prefix: { required: true, message: '请输入编号前缀', trigger: 'blur' },
  count: { required: true, type: 'number', message: '请输入创建数量', trigger: 'blur' },
  type: { required: true, type: 'number', message: '请选择类型', trigger: 'change' }
};

// ─── 表格列定义 ───
const columns: DataTableColumns = [
  { title: '车位编号', key: 'spaceNo', width: 120 },
  {
    title: '所属停车场',
    key: 'lotName',
    width: 160,
    ellipsis: { tooltip: true },
    render(row: any) {
      return row.lotName || row.parkingLotName || '--';
    }
  },
  {
    title: '类型',
    key: 'type',
    width: 100,
    align: 'center',
    render(row: any) {
      const info = typeMap[row.type as number] || { label: '未知', type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small', round: true }, { default: () => info.label });
    }
  },
  { title: '楼层', key: 'floor', width: 70, align: 'center', render: (row: any) => row.floor ?? '--' },
  { title: '区域', key: 'area', width: 80, align: 'center', render: (row: any) => row.area || '--' },
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

// ─── 获取停车场列表 ───
async function fetchLotOptions() {
  const { data, error } = await request({ url: '/api/parking-lots/all' });
  if (!error && data) {
    const list = Array.isArray(data) ? data : [];
    lotOptions.value = list.map((item: any) => ({ label: item.name, value: item.id }));
  }
}

// ─── 数据获取 ───
async function fetchData() {
  loading.value = true;
  const { data, error } = await request({
    url: '/api/parking-spaces',
    params: {
      page: pagination.page,
      size: pagination.pageSize,
      lotId: searchParams.lotId ?? undefined,
      status: searchParams.status ?? undefined,
      type: searchParams.type ?? undefined
    }
  });
  loading.value = false;
  if (!error && data) {
    tableData.value = data.records || data.list || data.content || [];
    pagination.itemCount = data.total || data.totalElements || 0;
  }
}

// ─── 搜索 & 重置 ───
function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  searchParams.lotId = null;
  searchParams.status = null;
  searchParams.type = null;
  handleSearch();
}

// ─── 新增 ───
function handleAdd() {
  isEdit.value = false;
  Object.assign(formModel, defaultForm());
  showModal.value = true;
}

// ─── 批量新增 ───
function handleBatchAdd() {
  Object.assign(batchFormModel, defaultBatchForm());
  showBatchModal.value = true;
}

// ─── 编辑 ───
function handleEdit(row: any) {
  isEdit.value = true;
  Object.assign(formModel, {
    id: row.id,
    lotId: row.lotId,
    spaceNo: row.spaceNo,
    type: row.type,
    floor: row.floor,
    area: row.area,
    status: row.status
  });
  showModal.value = true;
}

// ─── 删除 ───
function handleDelete(row: any) {
  window.$dialog?.warning({
    title: '确认',
    content: `确认删除车位「${row.spaceNo}」？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/parking-spaces/${row.id}`, method: 'delete' });
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
    const { error } = await request({ url: `/api/parking-spaces/${formModel.id}`, method: 'put', data: body });
    if (!error) {
      window.$message?.success('更新成功');
      showModal.value = false;
      fetchData();
    }
  } else {
    const { error } = await request({ url: '/api/parking-spaces', method: 'post', data: body });
    if (!error) {
      window.$message?.success('新增成功');
      showModal.value = false;
      fetchData();
    }
  }
}

// ─── 批量创建提交 ───
async function handleBatchSubmit() {
  try {
    await batchFormRef.value?.validate();
  } catch {
    return;
  }

  const { error } = await request({ url: '/api/parking-spaces/batch', method: 'post', data: { ...batchFormModel } });
  if (!error) {
    window.$message?.success('批量创建成功');
    showBatchModal.value = false;
    fetchData();
  }
}

// ─── 初始化 ───
onMounted(() => {
  fetchLotOptions();
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="车位管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NSpace>
          <NButton type="primary" @click="handleBatchAdd">
            <template #icon>
              <icon-mdi-plus-box-multiple />
            </template>
            批量创建
          </NButton>
          <NButton type="primary" @click="handleAdd">
            <template #icon>
              <icon-mdi-plus />
            </template>
            新增
          </NButton>
        </NSpace>
      </template>

      <!-- 搜索栏 -->
      <div class="mb-16px flex flex-wrap items-center gap-12px">
        <NSelect
          v-model:value="searchParams.lotId"
          :options="lotOptions"
          placeholder="所属停车场"
          clearable
          filterable
          class="w-200px"
        />
        <NSelect
          v-model:value="searchParams.type"
          :options="typeOptions"
          placeholder="类型"
          clearable
          class="w-140px"
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
        :scroll-x="780"
        remote
        striped
      />
    </NCard>

    <!-- 新增/编辑弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="isEdit ? '编辑车位' : '新增车位'"
      preset="card"
      class="w-540px"
      :mask-closable="false"
    >
      <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="90">
        <NFormItem label="停车场" path="lotId">
          <NSelect
            v-model:value="formModel.lotId"
            :options="lotOptions"
            placeholder="请选择停车场"
            filterable
          />
        </NFormItem>
        <NFormItem label="车位编号" path="spaceNo">
          <NInput v-model:value="formModel.spaceNo" placeholder="请输入车位编号" />
        </NFormItem>
        <NFormItem label="类型" path="type">
          <NSelect v-model:value="formModel.type" :options="typeOptions" />
        </NFormItem>
        <div class="flex gap-12px">
          <NFormItem label="楼层" path="floor" class="flex-1">
            <NInputNumber v-model:value="formModel.floor" placeholder="楼层" class="w-full" />
          </NFormItem>
          <NFormItem label="区域" path="area" class="flex-1">
            <NInput v-model:value="formModel.area" placeholder="区域" />
          </NFormItem>
        </div>
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

    <!-- 批量创建弹窗 -->
    <NModal
      v-model:show="showBatchModal"
      title="批量创建车位"
      preset="card"
      class="w-540px"
      :mask-closable="false"
    >
      <NForm ref="batchFormRef" :model="batchFormModel" :rules="batchFormRules" label-placement="left" label-width="90">
        <NFormItem label="停车场" path="lotId">
          <NSelect
            v-model:value="batchFormModel.lotId"
            :options="lotOptions"
            placeholder="请选择停车场"
            filterable
          />
        </NFormItem>
        <NFormItem label="编号前缀" path="prefix">
          <NInput v-model:value="batchFormModel.prefix" placeholder="如 A、B-1 等" />
        </NFormItem>
        <NFormItem label="创建数量" path="count">
          <NInputNumber v-model:value="batchFormModel.count" :min="1" :max="500" class="w-full" />
        </NFormItem>
        <NFormItem label="类型" path="type">
          <NSelect v-model:value="batchFormModel.type" :options="typeOptions" />
        </NFormItem>
        <div class="flex gap-12px">
          <NFormItem label="楼层" path="floor" class="flex-1">
            <NInputNumber v-model:value="batchFormModel.floor" placeholder="楼层" class="w-full" />
          </NFormItem>
          <NFormItem label="区域" path="area" class="flex-1">
            <NInput v-model:value="batchFormModel.area" placeholder="区域" />
          </NFormItem>
        </div>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showBatchModal = false">取消</NButton>
          <NButton type="primary" @click="handleBatchSubmit">确认创建</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
