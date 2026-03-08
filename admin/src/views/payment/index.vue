<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NCard, NDataTable, NTag } from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'PaymentManage' });

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });

/* ============ maps ============ */
const payMethodMap: Record<number, string> = {
  1: '微信',
  2: '支付宝',
  3: '现金'
};

const payMethodTagType: Record<number, 'success' | 'info' | 'warning'> = {
  1: 'success',
  2: 'info',
  3: 'warning'
};

const payStatusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'default' }> = {
  0: { label: '未支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已退款', type: 'error' },
  3: { label: '支付失败', type: 'default' }
};

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '支付流水号', key: 'transactionNo', minWidth: 180,
    render: row => row.transactionNo || row.orderNo || '-'
  },
  { title: '订单号', key: 'orderNo', minWidth: 160,
    render: row => row.orderNo || `#${row.orderId}` || '-'
  },
  {
    title: '支付金额',
    key: 'amount',
    minWidth: 110,
    render: row => `¥${(row.amount ?? 0).toFixed(2)}`
  },
  {
    title: '支付方式',
    key: 'paymentMethod',
    minWidth: 100,
    render: row =>
      h(
        NTag,
        { size: 'small', type: payMethodTagType[row.paymentMethod] || 'default' },
        { default: () => payMethodMap[row.paymentMethod] || '未知' }
      )
  },
  {
    title: '支付状态',
    key: 'status',
    minWidth: 100,
    render: row => {
      const s = payStatusMap[row.status] || { label: '未知', type: 'default' as const };
      return h(NTag, { size: 'small', type: s.type }, { default: () => s.label });
    }
  },
  { title: '支付时间', key: 'paidAt', minWidth: 170,
    render: row => row.paidAt || row.createdAt || '-'
  }
];

/* ============ methods ============ */
async function fetchData() {
  loading.value = true;
  const { data, error } = await request({
    url: '/api/payments/admin',
    params: { page: pagination.page, size: pagination.pageSize }
  });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? data?.records ?? [];
  pagination.itemCount = data?.total ?? 0;
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

/* ============ init ============ */
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="支付流水" :bordered="false" class="sm:flex-1-hidden card-wrapper">
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
        :scroll-x="860"
        remote
        flex-height
        class="sm:h-full"
      />
    </NCard>
  </div>
</template>
