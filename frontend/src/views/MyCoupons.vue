<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post } from '@/utils/request'
import {
  NCard, NGrid, NGi, NButton, NTag, NSpace, NSpin, NIcon, NTabs, NTabPane, NEmpty
} from 'naive-ui'
import { GiftOutline, TicketOutline, TimeOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'

const availableCoupons = ref<any[]>([])
const myCoupons = ref<any[]>([])
const loadingAvailable = ref(false)
const loadingMine = ref(false)
const claimingIds = ref<Set<number>>(new Set())

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD') : '-'
}

function getCouponStatusType(status: string): 'success' | 'info' | 'default' | 'warning' {
  if (status === 'UNUSED' || status === '未使用') return 'success'
  if (status === 'USED' || status === '已使用') return 'info'
  return 'default'
}

function getCouponStatusLabel(status: string) {
  const map: Record<string, string> = {
    UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期'
  }
  return map[status] || status || '未知'
}

async function fetchAvailableCoupons() {
  loadingAvailable.value = true
  try {
    const res: any = await get('/api/public/coupons')
    const list = res.data?.records || res.data || res.records || res || []
    availableCoupons.value = Array.isArray(list) ? list : []
  } catch {
    availableCoupons.value = []
  } finally {
    loadingAvailable.value = false
  }
}

async function fetchMyCoupons() {
  loadingMine.value = true
  try {
    const res: any = await get('/api/user-coupons')
    const list = res.data?.records || res.data || res.records || res || []
    myCoupons.value = Array.isArray(list) ? list : []
  } catch {
    myCoupons.value = []
  } finally {
    loadingMine.value = false
  }
}

async function handleClaim(couponId: number) {
  claimingIds.value.add(couponId)
  try {
    await post(`/api/user-coupons/claim/${couponId}`)
    window.$message?.success('领取成功')
    fetchMyCoupons()
    fetchAvailableCoupons()
  } catch {
    // handled by interceptor
  } finally {
    claimingIds.value.delete(couponId)
  }
}

onMounted(() => {
  fetchAvailableCoupons()
  fetchMyCoupons()
})
</script>

<template>
  <div class="coupons-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">优惠券</h1>
        <p class="page-desc">领取优惠券，享受停车折扣</p>
      </div>

      <n-tabs type="line" animated>
        <!-- Available Coupons -->
        <n-tab-pane name="available" tab="可领取">
          <n-spin :show="loadingAvailable" style="min-height: 200px;">
            <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
              <n-gi v-for="c in availableCoupons" :key="c.id" span="3 m:3 l:1">
                <n-card class="coupon-card available" :bordered="false">
                  <div class="coupon-left">
                    <div class="coupon-amount">
                      <span class="unit">¥</span>
                      <span class="value">{{ c.discountAmount ?? c.discount_amount ?? c.amount ?? '?' }}</span>
                    </div>
                    <div class="coupon-condition">
                      满{{ c.minAmount ?? c.min_amount ?? 0 }}可用
                    </div>
                  </div>
                  <div class="coupon-right">
                    <h4 class="coupon-name">{{ c.name || c.couponName || '优惠券' }}</h4>
                    <p class="coupon-expire">
                      <n-icon :component="TimeOutline" :size="12" />
                      {{ formatTime(c.startTime || c.start_time) }} ~ {{ formatTime(c.endTime || c.end_time || c.expireTime || c.expire_time) }}
                    </p>
                    <n-button
                      type="primary"
                      size="small"
                      round
                      :loading="claimingIds.has(c.id)"
                      @click="handleClaim(c.id)"
                    >
                      <template #icon><n-icon :component="GiftOutline" /></template>
                      领取
                    </n-button>
                  </div>
                </n-card>
              </n-gi>
            </n-grid>
            <n-empty v-if="!loadingAvailable && availableCoupons.length === 0" description="暂无可领取的优惠券" style="padding: 40px 0;" />
          </n-spin>
        </n-tab-pane>

        <!-- My Coupons -->
        <n-tab-pane name="mine" tab="我的优惠券">
          <n-spin :show="loadingMine" style="min-height: 200px;">
            <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
              <n-gi v-for="c in myCoupons" :key="c.id" span="3 m:3 l:1">
                <n-card
                  class="coupon-card"
                  :class="{ used: c.status === 'USED' || c.status === '已使用', expired: c.status === 'EXPIRED' || c.status === '已过期' }"
                  :bordered="false"
                >
                  <div class="coupon-left">
                    <div class="coupon-amount">
                      <span class="unit">¥</span>
                      <span class="value">{{ c.discountAmount ?? c.discount_amount ?? c.amount ?? '?' }}</span>
                    </div>
                    <div class="coupon-condition">
                      满{{ c.minAmount ?? c.min_amount ?? 0 }}可用
                    </div>
                  </div>
                  <div class="coupon-right">
                    <div style="display: flex; align-items: center; gap: 8px;">
                      <h4 class="coupon-name">{{ c.name || c.couponName || '优惠券' }}</h4>
                      <n-tag :type="getCouponStatusType(c.status)" size="tiny" round>
                        {{ getCouponStatusLabel(c.status) }}
                      </n-tag>
                    </div>
                    <p class="coupon-expire">
                      <n-icon :component="TimeOutline" :size="12" />
                      {{ formatTime(c.startTime || c.start_time) }} ~ {{ formatTime(c.endTime || c.end_time || c.expireTime || c.expire_time) }}
                    </p>
                  </div>
                </n-card>
              </n-gi>
            </n-grid>
            <n-empty v-if="!loadingMine && myCoupons.length === 0" description="暂无优惠券" style="padding: 40px 0;" />
          </n-spin>
        </n-tab-pane>
      </n-tabs>
    </div>
  </div>
</template>

<style scoped>
.coupons-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.page-header {
  margin-bottom: 24px;
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
.coupon-card {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}
.coupon-card :deep(.n-card__content) {
  display: flex;
  gap: 20px;
  align-items: center;
}
.coupon-card.available {
  background: linear-gradient(135deg, #f0faf4, #e8f4fd);
}
.coupon-card.used {
  opacity: 0.6;
}
.coupon-card.expired {
  opacity: 0.5;
  filter: grayscale(0.5);
}
.coupon-left {
  text-align: center;
  padding-right: 20px;
  border-right: 2px dashed #e8e8e8;
  min-width: 80px;
}
.coupon-amount {
  color: #d03050;
}
.coupon-amount .unit {
  font-size: 14px;
  font-weight: 600;
}
.coupon-amount .value {
  font-size: 32px;
  font-weight: 800;
}
.coupon-condition {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
.coupon-right {
  flex: 1;
}
.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px;
}
.coupon-expire {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  margin: 0 0 8px;
}
</style>
