<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'
import {
  NCard, NTag, NSpin, NIcon, NEmpty, NCollapse, NCollapseItem, NSpace
} from 'naive-ui'
import { MegaphoneOutline, TimeOutline, ChevronDownOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'

const announcements = ref<any[]>([])
const loading = ref(false)

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'
}

function getTypeColor(type: string | number): 'info' | 'success' | 'warning' | 'error' {
  const map: Record<string | number, any> = {
    '通知': 'info',
    '公告': 'success',
    '活动': 'warning',
    '紧急': 'error',
    NOTICE: 'info',
    ACTIVITY: 'warning',
    URGENT: 'error',
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'error',
  }
  return map[type] || 'info'
}

const typeLabel: Record<number, string> = {
  0: '通知',
  1: '公告',
  2: '活动',
  3: '紧急',
}

async function fetchAnnouncements() {
  loading.value = true
  try {
    const res: any = await get('/api/public/announcements')
    const data = res.data || res
    const list = data.list || data.records || (Array.isArray(data) ? data : [])
    announcements.value = Array.isArray(list) ? list : []
  } catch {
    announcements.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchAnnouncements)
</script>

<template>
  <div class="announcements-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">公告列表</h1>
        <p class="page-desc">查看景区停车场最新公告和通知</p>
      </div>

      <n-spin :show="loading">
        <div class="announcement-list">
          <n-collapse arrow-placement="right">
            <n-collapse-item
              v-for="a in announcements"
              :key="a.id"
              :name="a.id"
            >
              <template #header>
                <div class="announcement-header">
                  <n-icon :component="MegaphoneOutline" :size="18" color="#18a058" />
                  <span class="announcement-title">{{ a.title }}</span>
                  <n-tag v-if="a.type != null" :type="getTypeColor(a.type)" size="tiny" :bordered="false" round>
                    {{ typeLabel[a.type] || a.type }}
                  </n-tag>
                </div>
              </template>
              <template #header-extra>
                <span class="announcement-time">
                  <n-icon :component="TimeOutline" :size="12" />
                  {{ formatTime(a.createdAt || a.publishTime || a.publish_time || a.createTime || a.create_time) }}
                </span>
              </template>
              <div class="announcement-content">
                {{ a.content || '暂无内容' }}
              </div>
            </n-collapse-item>
          </n-collapse>
        </div>
        <n-empty v-if="!loading && announcements.length === 0" description="暂无公告" style="margin-top: 60px;" />
      </n-spin>
    </div>
  </div>
</template>

<style scoped>
.announcements-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}
.page-header {
  margin-bottom: 28px;
}
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}
.page-desc {
  color: #888;
  font-size: 14px;
  margin: 0;
}
.announcement-list {
  background: #fff;
  border-radius: 12px;
  padding: 8px;
}
.announcement-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.announcement-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}
.announcement-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #bbb;
  white-space: nowrap;
}
.announcement-content {
  padding: 12px 0 8px 28px;
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
