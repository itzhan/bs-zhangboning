<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NSwitch, NTag } from 'naive-ui';
import { request } from '@/service/request';

defineOptions({ name: 'UserManage' });

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });
const keyword = ref('');
const filterRole = ref<string | null>(null);
const showModal = ref(false);

const editForm = ref({
  id: 0,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  role: 'USER'
});

/* ============ options ============ */
const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '游客', value: 'USER' }
];

const roleFilterOptions = [{ label: '全部', value: null }, ...roleOptions];

const roleTagMap: Record<string, { label: string; type: 'info' | 'default' }> = {
  ADMIN: { label: '管理员', type: 'info' },
  USER: { label: '游客', type: 'default' }
};

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '正常', type: 'success' },
  0: { label: '禁用', type: 'error' }
};

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '用户名', key: 'username', minWidth: 100 },
  { title: '昵称', key: 'nickname', minWidth: 100 },
  { title: '手机号', key: 'phone', minWidth: 120 },
  { title: '邮箱', key: 'email', minWidth: 160 },
  {
    title: '角色',
    key: 'role',
    minWidth: 80,
    render: row => {
      const r = roleTagMap[row.role] || { label: row.role, type: 'default' as const };
      return h(NTag, { size: 'small', type: r.type }, { default: () => r.label });
    }
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
  { title: '创建时间', key: 'createTime', minWidth: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    render: row =>
      h(NSpace, { size: 8 }, () => [
        h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NButton,
          {
            size: 'small',
            type: row.status === 1 ? 'warning' : 'success',
            onClick: () => handleToggleStatus(row)
          },
          { default: () => (row.status === 1 ? '禁用' : '启用') }
        ),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
      ])
  }
];

/* ============ methods ============ */
async function fetchData() {
  loading.value = true;
  const params: Record<string, any> = { page: pagination.page, size: pagination.pageSize };
  if (keyword.value) params.keyword = keyword.value;
  if (filterRole.value) params.role = filterRole.value;
  const { data, error } = await request({ url: '/api/users', params });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? [];
  pagination.itemCount = data?.total ?? 0;
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function openEdit(row: any) {
  editForm.value = {
    id: row.id,
    username: row.username,
    nickname: row.nickname || '',
    phone: row.phone || '',
    email: row.email || '',
    role: row.role || 'USER'
  };
  showModal.value = true;
}

async function handleEditSubmit() {
  const { error } = await request({
    url: `/api/users/${editForm.value.id}`,
    method: 'put',
    data: editForm.value
  });
  if (error) {
    window.$message?.error('操作失败');
    return;
  }
  window.$message?.success('修改成功');
  showModal.value = false;
  fetchData();
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1;
  const { error } = await request({
    url: `/api/users/${row.id}/status`,
    method: 'put',
    params: { status: newStatus }
  });
  if (error) {
    window.$message?.error('操作失败');
    return;
  }
  window.$message?.success('操作成功');
  fetchData();
}

function handleDelete(id: number) {
  window.$dialog?.warning({
    title: '确认删除',
    content: '确定删除该用户吗？此操作不可撤销。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/users/${id}`, method: 'delete' });
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
    <NCard title="用户管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NSpace>
          <NInput v-model:value="keyword" placeholder="搜索用户名/手机号" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
          <NSelect
            v-model:value="filterRole"
            :options="roleFilterOptions"
            placeholder="角色"
            clearable
            style="width: 120px"
            @update:value="handleSearch"
          />
          <NButton type="info" @click="handleSearch">搜索</NButton>
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
        :scroll-x="1100"
        remote
        flex-height
        class="sm:h-full"
      />
    </NCard>

    <!-- Edit Modal -->
    <NModal v-model:show="showModal" preset="card" title="编辑用户" style="width: 520px">
      <NForm :model="editForm" label-placement="left" label-width="80px">
        <NFormItem label="用户名">
          <NInput :value="editForm.username" disabled />
        </NFormItem>
        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="editForm.nickname" placeholder="请输入昵称" />
        </NFormItem>
        <NFormItem label="手机号" path="phone">
          <NInput v-model:value="editForm.phone" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="邮箱" path="email">
          <NInput v-model:value="editForm.email" placeholder="请输入邮箱" />
        </NFormItem>
        <NFormItem label="角色" path="role">
          <NSelect v-model:value="editForm.role" :options="roleOptions" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleEditSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
