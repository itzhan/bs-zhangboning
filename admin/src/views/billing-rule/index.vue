<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import {
  NButton,
  NCard,
  NDataTable,
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

defineOptions({ name: 'BillingRuleManage' });

/* ============ state ============ */
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 });
const showModal = ref(false);
const isEdit = ref(false);
const formRef = ref<any>(null);

const defaultForm = () => ({
  id: undefined as number | undefined,
  name: '',
  lotId: null as number | null,
  ruleType: 1,
  firstHourPrice: 0,
  extraHourPrice: 0,
  dailyMax: 0,
  freeMinutes: 0,
  flatPrice: 0,
  status: 1
});

const formModel = ref(defaultForm());
const lotOptions = ref<{ label: string; value: number }[]>([]);

/* ============ rule-type options ============ */
const ruleTypeOptions = [
  { label: '按时计费', value: 1 },
  { label: '按次计费', value: 2 }
];

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '停用', type: 'error' }
};

/* ============ columns ============ */
const columns: DataTableColumns<any> = [
  { title: '规则名称', key: 'name', minWidth: 120 },
  {
    title: '适用停车场',
    key: 'lotId',
    minWidth: 120,
    render: row => {
      if (!row.lotId) return h(NTag, { size: 'small', type: 'info' }, { default: () => '全局' });
      const lot = lotOptions.value.find(l => l.value === row.lotId);
      return lot ? lot.label : `ID:${row.lotId}`;
    }
  },
  {
    title: '规则类型',
    key: 'ruleType',
    minWidth: 90,
    render: row =>
      h(NTag, { size: 'small', type: row.ruleType === 1 ? 'info' : 'warning' }, { default: () => (row.ruleType === 1 ? '按时' : '按次') })
  },
  { title: '首小时价格', key: 'firstHourPrice', minWidth: 100, render: row => `¥${row.firstHourPrice ?? '-'}` },
  { title: '超时价格', key: 'extraHourPrice', minWidth: 90, render: row => `¥${row.extraHourPrice ?? '-'}` },
  { title: '每日上限', key: 'dailyMax', minWidth: 90, render: row => (row.dailyMax ? `¥${row.dailyMax}` : '-') },
  { title: '免费时长(分)', key: 'freeMinutes', minWidth: 100 },
  { title: '按次价格', key: 'flatPrice', minWidth: 90, render: row => (row.flatPrice ? `¥${row.flatPrice}` : '-') },
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
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id)
          },
          { default: () => '删除' }
        )
      ])
  }
];

/* ============ methods ============ */
async function fetchData() {
  loading.value = true;
  const { data, error } = await request({
    url: '/api/billing-rules',
    params: { page: pagination.page, size: pagination.pageSize }
  });
  loading.value = false;
  if (error) return;
  tableData.value = data?.list ?? data?.records ?? [];
  pagination.itemCount = data?.total ?? 0;
}

async function fetchLotOptions() {
  const { data, error } = await request({ url: '/api/parking-lots/all' });
  if (error) return;
  lotOptions.value = (data || []).map((item: any) => ({ label: item.name, value: item.id }));
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
      url: `/api/billing-rules/${formModel.value.id}`,
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
      url: '/api/billing-rules',
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
    content: '确定删除该计费规则吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { error } = await request({ url: `/api/billing-rules/${id}`, method: 'delete' });
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
  fetchLotOptions();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="计费规则管理" :bordered="false" class="sm:flex-1-hidden card-wrapper">
      <template #header-extra>
        <NButton type="primary" @click="openAdd">新增规则</NButton>
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

    <!-- Add / Edit Modal -->
    <NModal v-model:show="showModal" preset="card" :title="isEdit ? '编辑计费规则' : '新增计费规则'" style="width: 600px">
      <NForm ref="formRef" :model="formModel" label-placement="left" label-width="110px">
        <NFormItem label="规则名称" path="name">
          <NInput v-model:value="formModel.name" placeholder="请输入规则名称" />
        </NFormItem>
        <NFormItem label="适用停车场" path="lotId">
          <NSelect
            v-model:value="formModel.lotId"
            :options="lotOptions"
            clearable
            placeholder="留空表示全局"
          />
        </NFormItem>
        <NFormItem label="规则类型" path="ruleType">
          <NSelect v-model:value="formModel.ruleType" :options="ruleTypeOptions" />
        </NFormItem>
        <NFormItem v-if="formModel.ruleType === 1" label="首小时价格" path="firstHourPrice">
          <NInputNumber v-model:value="formModel.firstHourPrice" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem v-if="formModel.ruleType === 1" label="超时价格" path="extraHourPrice">
          <NInputNumber v-model:value="formModel.extraHourPrice" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="每日上限" path="dailyMax">
          <NInputNumber v-model:value="formModel.dailyMax" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="免费时长(分)" path="freeMinutes">
          <NInputNumber v-model:value="formModel.freeMinutes" :min="0" :precision="0" style="width: 100%" />
        </NFormItem>
        <NFormItem v-if="formModel.ruleType === 2" label="按次价格" path="flatPrice">
          <NInputNumber v-model:value="formModel.flatPrice" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </NInputNumber>
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
