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
  NTag,
  NSpace,
  NDescriptions,
  NDescriptionsItem
} from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'OrderManage' });

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
  lotId: null as number | null,
  keyword: ''
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

// ─── 支付状态 ───
const paymentStatusOptions = [
  { label: '未支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已退款', value: 2 }
];

const paymentStatusMap: Record<number, { label: string; type: 'warning' | 'success' | 'info' }> = {
  0: { label: '未支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已退款', type: 'info' }
};

// ─── 订单状态 ───
const orderStatusOptions = [
  { label: '进行中', value: 0 },
  { label: '已完成', value: 1 },
  { label: '已取消', value: 2 },
  { label: '异常', value: 3 }
];

const orderStatusMap: Record<number, { label: string; type: 'info' | 'success' | 'warning' | 'error' }> = {
  0: { label: '进行中', type: 'info' },
  1: { label: '已完成', type: 'success' },
  2: { label: '已取消', type: 'warning' },
  3: { label: '异常', type: 'error' }
};

// ─── 表格列定义 ───
const columns: DataTableColumns = [
  { title: '订单号', key: 'orderNo', width: 160, ellipsis: { tooltip: true } },
  {
    title: '用户',
    key: 'userName',
    width: 100,
    ellipsis: { tooltip: true },
    render: (row: any) => row.userName || row.username || '--'
  },
  { title: '车牌号', key: 'plateNumber', width: 110 },
  {
    title: '停车场',
    key: 'lotName',
    width: 140,
    ellipsis: { tooltip: true },
    render: (row: any) => row.lotName || row.parkingLotName || '--'
  },
  {
    title: '入场时间',
    key: 'entryTime',
    width: 160,
    render: (row: any) => row.entryTime || row.enterTime || '--'
  },
  {
    title: '出场时间',
    key: 'exitTime',
    width: 160,
    render: (row: any) => row.exitTime || '--'
  },
  {
    title: '时长(分钟)',
    key: 'duration',
    width: 100,
    align: 'center',
    render: (row: any) => row.duration ?? row.parkingDuration ?? '--'
  },
  {
    title: '应付金额',
    key: 'totalAmount',
    width: 100,
    align: 'right',
    render: (row: any) => {
      const amount = row.totalAmount ?? row.amount;
      return amount != null ? `¥${Number(amount).toFixed(2)}` : '--';
    }
  },
  {
    title: '实付金额',
    key: 'actualAmount',
    width: 100,
    align: 'right',
    render: (row: any) => {
      const amount = row.actualAmount ?? row.paidAmount;
      return amount != null ? `¥${Number(amount).toFixed(2)}` : '--';
    }
  },
  {
    title: '支付状态',
    key: 'paymentStatus',
    width: 90,
    align: 'center',
    render(row: any) {
      const info = paymentStatusMap[row.paymentStatus as number] || { label: '未知', type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small', round: true }, { default: () => info.label });
    }
  },
  {
    title: '订单状态',
    key: 'status',
    width: 90,
    align: 'center',
    render(row: any) {
      const info = orderStatusMap[row.status as number] || { label: '未知', type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small', round: true }, { default: () => info.label });
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    align: 'center',
    fixed: 'right',
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
    url: '/api/orders/admin',
    params: {
      page: pagination.page,
      size: pagination.pageSize,
      status: searchParams.status ?? undefined,
      lotId: searchParams.lotId ?? undefined,
      keyword: searchParams.keyword || undefined
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
  searchParams.keyword = '';
  handleSearch();
}

// ─── 查看详情 ───
async function handleViewDetail(row: any) {
  detailData.value = row;
  showDetailModal.value = true;

  detailLoading.value = true;
  const { data, error } = await request({ url: `/api/orders/${row.id}` });
  detailLoading.value = false;
  if (!error && data) {
    detailData.value = data;
  }
}

// ─── 状态标签辅助 ───
function getOrderStatusLabel(status: number): string {
  return orderStatusMap[status]?.label || '未知';
}

function getOrderStatusType(status: number): 'info' | 'success' | 'warning' | 'error' {
  return orderStatusMap[status]?.type || 'info';
}

function getPaymentStatusLabel(status: number): string {
  return paymentStatusMap[status]?.label || '未知';
}

function getPaymentStatusType(status: number): 'warning' | 'success' | 'info' {
  return paymentStatusMap[status]?.type || 'warning';
}

// ─── 初始化 ───
onMounted(() => {
  fetchLotOptions();
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="订单管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <!-- 搜索栏 -->
      <div class="mb-16px flex flex-wrap items-center gap-12px">
        <NInput
          v-model:value="searchParams.keyword"
          placeholder="搜索车牌号/订单号"
          clearable
          class="w-200px"
          @keyup.enter="handleSearch"
        />
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
          :options="orderStatusOptions"
          placeholder="订单状态"
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
        :scroll-x="1480"
        remote
        striped
      />
    </NCard>

    <!-- 订单详情弹窗 -->
    <NModal
      v-model:show="showDetailModal"
      title="订单详情"
      preset="card"
      class="w-680px"
    >
      <template v-if="detailData">
        <NDescriptions label-placement="left" bordered :column="2">
          <NDescriptionsItem label="订单号" :span="2">
            {{ detailData.orderNo || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="订单状态">
            <NTag :type="getOrderStatusType(detailData.status)" size="small" round>
              {{ getOrderStatusLabel(detailData.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="支付状态">
            <NTag :type="getPaymentStatusType(detailData.paymentStatus)" size="small" round>
              {{ getPaymentStatusLabel(detailData.paymentStatus) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="用户">
            {{ detailData.userName || detailData.username || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="车牌号">
            {{ detailData.plateNumber || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="停车场">
            {{ detailData.lotName || detailData.parkingLotName || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="车位">
            {{ detailData.spaceNo || detailData.parkingSpaceNo || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="入场时间">
            {{ detailData.entryTime || detailData.enterTime || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="出场时间">
            {{ detailData.exitTime || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem label="停车时长">
            {{ detailData.duration ?? detailData.parkingDuration ?? '--' }} 分钟
          </NDescriptionsItem>
          <NDescriptionsItem label="应付金额">
            <span class="text-16px font-bold">
              ¥{{ detailData.totalAmount != null ? Number(detailData.totalAmount).toFixed(2) : '--' }}
            </span>
          </NDescriptionsItem>
          <NDescriptionsItem label="实付金额">
            <span class="text-16px font-bold text-primary">
              ¥{{ detailData.actualAmount != null ? Number(detailData.actualAmount).toFixed(2) : (detailData.paidAmount != null ? Number(detailData.paidAmount).toFixed(2) : '--') }}
            </span>
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailData.couponAmount" label="优惠金额">
            -¥{{ Number(detailData.couponAmount).toFixed(2) }}
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailData.paymentMethod != null" label="支付方式">
            {{ ['微信', '支付宝', '现金', '其他'][detailData.paymentMethod] || '--' }}
          </NDescriptionsItem>
          <NDescriptionsItem v-if="detailData.paymentTime" label="支付时间" :span="2">
            {{ detailData.paymentTime }}
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
