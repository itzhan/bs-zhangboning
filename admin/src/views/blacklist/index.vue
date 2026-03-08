<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NTag } from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'BlacklistManage' });

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });
const keyword = ref('');
const showModal = ref(false);
const isEdit = ref(false);

const defaultForm = () => ({
  id: undefined as number | undefined,
  plateNumber: '',
  reason: '',
  status: 1
});

const formModel = ref(defaultForm());

/* ============ status ============ */
const statusMap: Record<number, { label: string; type: 'error' | 'default' }> = {
  1: { label: '生效中', type: 'error' },
  0: { label: '已解除', type: 'default' }
};

const statusOptions = [
  { label: '生效中', value: 1 },
  { label: '已解除', value: 0 }
];

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '车牌号', key: 'plateNumber', minWidth: 120 },
  { title: '拉黑原因', key: 'reason', minWidth: 200 },
  {
    title: '状态',
    key: 'status',
    minWidth: 90,
    render: row => {
      const s = statusMap[row.status] || { label: '未知', type: 'default' as const };
      return h(NTag, { size: 'small', type: s.type }, { default: () => s.label });
    }
  },
  { title: '创建时间', key: 'createTime', minWidth: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
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
    url: '/api/blacklist',
    params: { page: pagination.page, size: pagination.pageSize, keyword: keyword.value || undefined }
  });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? [];
  pagination.itemCount = data?.total ?? 0;
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
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
  if (isEdit.value) {
    const { error } = await request({
      url: `/api/blacklist/${formModel.value.id}`,
      method: 'put',
      data: formModel.value
    });
    if (error) {
      window.$message?.error('操作失败');
      return;
    }
    window.$message?.success('修改成功');
  } else {
    const { error } = await request({
      url: '/api/blacklist',
      method: 'post',
      data: formModel.value
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
    content: '确定删除该黑名单记录吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/blacklist/${id}`, method: 'delete' });
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

/* ============ init ============ */
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="黑名单管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NSpace>
          <NInput v-model:value="keyword" placeholder="搜索车牌号" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
          <NButton type="info" @click="handleSearch">搜索</NButton>
          <NButton type="primary" @click="openAdd">新增黑名单</NButton>
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
        :scroll-x="780"
        remote
        flex-height
        class="sm:h-full"
      />
    </NCard>

    <!-- Add / Edit Modal -->
    <NModal v-model:show="showModal" preset="card" :title="isEdit ? '编辑黑名单' : '新增黑名单'" style="width: 500px">
      <NForm :model="formModel" label-placement="left" label-width="80px">
        <NFormItem label="车牌号" path="plateNumber">
          <NInput v-model:value="formModel.plateNumber" placeholder="请输入车牌号" :disabled="isEdit" />
        </NFormItem>
        <NFormItem label="拉黑原因" path="reason">
          <NInput v-model:value="formModel.reason" type="textarea" placeholder="请输入拉黑原因" :rows="3" />
        </NFormItem>
        <NFormItem v-if="isEdit" label="状态" path="status">
          <NSelect v-model:value="formModel.status" :options="statusOptions" />
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
