<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NTag } from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'AnnouncementManage' });

function formatTime(val: any) {
  if (!val) return '--';
  return String(val).substring(0, 16);
}

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });
const filterType = ref<number | null>(null);
const filterStatus = ref<number | null>(null);
const showModal = ref(false);
const isEdit = ref(false);

const defaultForm = () => ({
  id: undefined as number | undefined,
  title: '',
  content: '',
  type: 1,
  status: 0
});

const formModel = ref(defaultForm());

/* ============ options ============ */
const typeOptions = [
  { label: '系统公告', value: 1 },
  { label: '停车提醒', value: 2 },
  { label: '优惠活动', value: 3 }
];

const typeFilterOptions = [{ label: '全部', value: null }, ...typeOptions];

const typeTagMap: Record<number, 'info' | 'warning' | 'success'> = {
  1: 'info',
  2: 'warning',
  3: 'success'
};

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下架', value: 2 }
];

const statusFilterOptions = [{ label: '全部', value: null }, ...statusOptions];

const statusTagMap: Record<number, { label: string; type: 'default' | 'success' | 'error' }> = {
  0: { label: '草稿', type: 'default' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已下架', type: 'error' }
};

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '标题', key: 'title', minWidth: 180, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    minWidth: 100,
    render: row =>
      h(
        NTag,
        { size: 'small', type: typeTagMap[row.type] || 'default' },
        { default: () => typeOptions.find(o => o.value === row.type)?.label || '未知' }
      )
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
  { title: '发布时间', key: 'publishTime', minWidth: 160, render: (row: any) => formatTime(row.publishTime || row.createdAt) },
  { title: '创建时间', key: 'createdAt', minWidth: 160, render: (row: any) => formatTime(row.createdAt) },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    render: row =>
      h(NSpace, { size: 8 }, () => {
        const btns = [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' })
        ];
        if (row.status === 0) {
          btns.push(
            h(NButton, { size: 'small', type: 'success', onClick: () => handlePublish(row.id) }, { default: () => '发布' })
          );
        }
        btns.push(
          h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        );
        return btns;
      })
  }
];

/* ============ methods ============ */
async function fetchData() {
  loading.value = true;
  const params: Record<string, any> = { page: pagination.page, size: pagination.pageSize };
  if (filterType.value !== null) params.type = filterType.value;
  if (filterStatus.value !== null) params.status = filterStatus.value;
  const { data, error } = await request({ url: '/api/announcements', params });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? [];
  pagination.itemCount = data?.total ?? 0;
}

function handleFilterChange() {
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
      url: `/api/announcements/${formModel.value.id}`,
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
      url: '/api/announcements',
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

async function handlePublish(id: number) {
  const { error } = await request({ url: `/api/announcements/${id}/publish`, method: 'put' });
  if (error) {
    window.$message?.error('发布失败');
    return;
  }
  window.$message?.success('发布成功');
  fetchData();
}

function handleDelete(id: number) {
  window.$dialog?.warning({
    title: '确认删除',
    content: '确定删除该公告吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/announcements/${id}`, method: 'delete' });
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
    <NCard title="公告管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NSpace>
          <NSelect
            v-model:value="filterType"
            :options="typeFilterOptions"
            placeholder="公告类型"
            clearable
            style="width: 140px"
            @update:value="handleFilterChange"
          />
          <NSelect
            v-model:value="filterStatus"
            :options="statusFilterOptions"
            placeholder="状态"
            clearable
            style="width: 120px"
            @update:value="handleFilterChange"
          />
          <NButton type="primary" @click="openAdd">新增公告</NButton>
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
        :scroll-x="900"
        remote
        flex-height
        class="sm:h-full"
      />
    </NCard>

    <!-- Add / Edit Modal -->
    <NModal v-model:show="showModal" preset="card" :title="isEdit ? '编辑公告' : '新增公告'" style="width: 640px">
      <NForm :model="formModel" label-placement="left" label-width="80px">
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formModel.title" placeholder="请输入标题" />
        </NFormItem>
        <NFormItem label="类型" path="type">
          <NSelect v-model:value="formModel.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem label="内容" path="content">
          <NInput v-model:value="formModel.content" type="textarea" placeholder="请输入公告内容" :rows="6" />
        </NFormItem>
        <NFormItem label="状态" path="status">
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
