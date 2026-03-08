<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import {
  NCard,
  NButton,
  NDataTable,
  NSelect,
  NModal,
  NTag,
  NSpace,
  NDescriptions,
  NDescriptionsItem
} from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'ReservationManage' });

// ─── 状态定义 ───
const loading = ref(false);
const tableData = ref<any[]>([]);
const showDetailModal = ref(false);
const detailData = ref<any>(null);
const detailLoading = ref(false);
const lotOptions = ref<{ label: string; value: number }[]>([]);

// ─── 搜索条件 ───
const searchParams = reactive({
  status: null as number | null,
  lotId: null as number | null
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

// ─── 状态选项 ───
const statusOptions = [
  { label: '待生效', value: 0 },
  { label: '已生效', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已过期', value: 4 }
];

const statusMap: Record<number, { label: string; type: 'info' | 'success' | 'default' | 'warning' | 'error' }> = {
  0: { label: '待生效', type: 'info' },
  1: { label: '已生效', type: 'success' },
  2: { label: '已完成', type: 'default' },
  3: { label: '已取消', type: 'warning' },
  4: { label: '已过期', type: 'error' }
};

// ─── 表格列定义 ───
const columns: DataTableColumns = [
  { title: '预约ID', key: 'id', width: 80, align: 'center' },
  {
    title: '用户',
    key: 'username',
    width: 100,
    ellipsis: { tooltip: true },
    render: (row: any) => row.username || '--'
  },
  { title: '车牌号', key: 'plateNumber', width: 110,
    render: (row: any) => row.plateNumber || '--'
  },
  {
    title: '停车场',
    key: 'lotName',
    width: 150,
    ellipsis: { tooltip: true },
    render: (row: any) => row.lotName || row.parkingLotName || '--'
  },
  {
    title: '车位',
    key: 'spaceName',
    width: 90,
    render: (row: any) => row.spaceName || '--'
  },
  {
    title: '预约时间段',
    key: 'timeRange',
    width: 200,
    render(row: any) {
      const start = row.startTime || row.reserveStartTime || '--';
      const end = row.endTime || row.reserveEndTime || '--';
      return `${start} ~ ${end}`;
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
    width: 100,
    align: 'center',
    render(row: any) {
      return h(NSpace, { justify: 'center' }, () => [
        h(
          NButton,
          { size: 'small', type: 'primary', quaternary: true, onClick: () => handleViewDetail(row) },
          { default: () => '详情' }
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
    url: '/api/reservations/admin',
    params: {
      page: pagination.page,
      size: pagination.pageSize,
      status: searchParams.status ?? undefined,
      lotId: searchParams.lotId ?? undefined
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
  searchParams.status = null;
  searchParams.lotId = null;
  handleSearch();
}

// ─── 查看详情 ───
async function handleViewDetail(row: any) {
  detailData.value = row;
  showDetailModal.value = true;

  // 尝试获取更详细的信息
  detailLoading.value = true;
  const { data, error } = await request({ url: `/api/reservations/${row.id}` });
  detailLoading.value = false;
  if (!error && data) {
    detailData.value = data;
  }
}

// ─── 获取状态标签 ───
function getStatusLabel(status: number): string {
  return statusMap[status]?.label || '未知';
}

function getStatusType(status: number): 'info' | 'success' | 'default' | 'warning' | 'error' {
  return statusMap[status]?.type || 'default';
}

// ─── 初始化 ───
onMounted(() => {
  fetchLotOptions();
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="预约管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <!-- 搜索栏 -->
      <div class="mb-16px flex flex-wrap items-center gap-12px">
        <NSelect
          v-model:value="searchParams.lotId"
          :options="lotOptions"
          placeholder="停车场"
          clearable
          filterable
          class="w-200px"
        />
        <NSelect
          v-model:value="searchParams.status"
          :options="statusOptions"
          placeholder="预约状态"
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
        :scroll-x="920"
        remote
        striped
      />
    </NCard>

    <!-- 预约详情弹窗 -->
    <NModal
      v-model:show="showDetailModal"
      title="预约详情"
      preset="card"
      class="w-600px"
    >
      <template v-if="detailData">
        <NDescriptions label-placement="left" bordered :column="2">
          <NDescriptionsItem label="预约ID">
            {{ detailData.id }}
          </NDescriptionsItem>
          <NDescriptionsItem label="状态">
            <NTag :type="getStatusType(detailData.status)" size="small" round>
              {{ getStatusLabel(detailData.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="用户">
            {{ detailData.username || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="车牌号">
            {{ detailData.plateNumber || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="停车场">
            {{ detailData.lotName || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="车位">
            {{ detailData.spaceName || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="开始时间" :span="2">
            {{ detailData.startTime || detailData.reserveStartTime || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="结束时间" :span="2">
            {{ detailData.endTime || detailData.reserveEndTime || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="创建时间" :span="2">
            {{ detailData.createTime || detailData.createdAt || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailData.remark" label="备注" :span="2">
            {{ detailData.remark }}
          </NDescriptionsItem>
        </NDescriptions>
      </template>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showDetailModal = false">关闭</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
