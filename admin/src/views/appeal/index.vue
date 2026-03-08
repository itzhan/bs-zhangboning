<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NTag } from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'AppealManage' });

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });
const filterStatus = ref<number | null>(null);

const showReplyModal = ref(false);
const replyForm = ref({
  id: 0,
  status: 2,
  reply: ''
});

/* ============ maps ============ */
const statusOptions = [
  { label: '全部', value: null },
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已驳回', value: 3 }
];

const statusTagMap: Record<number, { label: string; type: 'default' | 'info' | 'success' | 'error' | 'warning' }> = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '处理中', type: 'info' },
  2: { label: '已通过', type: 'success' },
  3: { label: '已驳回', type: 'error' }
};

const typeMap: Record<number, string> = {
  1: '费用异常',
  2: '入场异常',
  3: '出场异常',
  4: '其他'
};

const replyStatusOptions = [
  { label: '通过', value: 2 },
  { label: '驳回', value: 3 }
];

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '申诉ID', key: 'id', width: 80 },
  { title: '用户', key: 'username', minWidth: 100 },
  { title: '关联订单', key: 'orderId', minWidth: 100 },
  {
    title: '类型',
    key: 'type',
    minWidth: 100,
    render: row => typeMap[row.type] || '未知'
  },
  {
    title: '申诉内容',
    key: 'content',
    minWidth: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '状态',
    key: 'status',
    minWidth: 90,
    render: row => {
      const s = statusTagMap[row.status] || { label: '未知', type: 'default' as const };
      return h(NTag, { size: 'small', type: s.type }, { default: () => s.label });
    }
  },
  { title: '创建时间', key: 'createTime', minWidth: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: row => {
      if (row.status === 0 || row.status === 1) {
        return h(
          NButton,
          { size: 'small', type: 'primary', onClick: () => openReply(row) },
          { default: () => '处理' }
        );
      }
      return h(NTag, { size: 'small', type: 'default' }, { default: () => '已处理' });
    }
  }
];

/* ============ methods ============ */
async function fetchData() {
  loading.value = true;
  const params: Record<string, any> = { page: pagination.page, size: pagination.pageSize };
  if (filterStatus.value !== null) {
    params.status = filterStatus.value;
  }
  const { data, error } = await request({ url: '/api/appeals/admin', params });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? [];
  pagination.itemCount = data?.total ?? 0;
}

function handleFilterChange() {
  pagination.page = 1;
  fetchData();
}

function openReply(row: any) {
  replyForm.value = { id: row.id, status: 2, reply: '' };
  showReplyModal.value = true;
}

async function handleReply() {
  if (!replyForm.value.reply.trim()) {
    window.$message?.warning('请输入回复内容');
    return;
  }
  const { error } = await request({
    url: `/api/appeals/${replyForm.value.id}/reply`,
    method: 'put',
    data: { status: replyForm.value.status, reply: replyForm.value.reply }
  });
  if (error) {
    window.$message?.error('操作失败');
    return;
  }
  window.$message?.success('处理成功');
  showReplyModal.value = false;
  fetchData();
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
    <NCard title="申诉管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NSpace>
          <NSelect
            v-model:value="filterStatus"
            :options="statusOptions"
            placeholder="筛选状态"
            clearable
            style="width: 150px"
            @update:value="handleFilterChange"
          />
        </NSpace>
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
        :scroll-x="1000"
        remote
        flex-height
        class="sm:h-full"
      />
    </NCard>

    <!-- Reply Modal -->
    <NModal v-model:show="showReplyModal" preset="card" title="处理申诉" style="width: 520px">
      <NForm :model="replyForm" label-placement="left" label-width="80px">
        <NFormItem label="处理结果" path="status">
          <NSelect v-model:value="replyForm.status" :options="replyStatusOptions" />
        </NFormItem>
        <NFormItem label="回复内容" path="reply">
          <NInput v-model:value="replyForm.reply" type="textarea" placeholder="请输入回复内容" :rows="4" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showReplyModal = false">取消</NButton>
          <NButton type="primary" @click="handleReply">提交</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
