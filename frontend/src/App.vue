<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  NConfigProvider,
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NMenu,
  NButton,
  NSpace,
  NDropdown,
  NAvatar,
  NMessageProvider,
  NDialogProvider,
  NNotificationProvider,
  useMessage,
  useDialog,
  useNotification,
  zhCN,
  dateZhCN
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { h } from 'vue'
import { RouterLink } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLoggedIn = computed(() => !!userStore.token)

const menuOptions = computed<MenuOption[]>(() => [
  { label: () => h(RouterLink, { to: '/' }, { default: () => '首页' }), key: '/' },
  { label: () => h(RouterLink, { to: '/parking-lots' }, { default: () => '停车场' }), key: '/parking-lots' },
  { label: () => h(RouterLink, { to: '/announcements' }, { default: () => '公告' }), key: '/announcements' },
])

const userMenuOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '我的订单', key: 'my-orders' },
  { label: '我的预约', key: 'my-reservations' },
  { label: '我的车辆', key: 'my-vehicles' },
  { label: '我的优惠券', key: 'my-coupons' },
  { label: '异常申诉', key: 'appeal' },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout' },
]

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/parking-lots')) return '/parking-lots'
  if (path.startsWith('/announcements')) return '/announcements'
  return path
})

function handleUserMenu(key: string) {
  if (key === 'logout') {
    userStore.logout()
    window.$message?.success('已退出登录')
    router.push('/')
  } else {
    router.push(`/${key}`)
  }
}

// 全局 message / dialog / notification 注入
function GlobalProviderSetup() {
  window.$message = useMessage()
  window.$dialog = useDialog()
  window.$notification = useNotification()
  return null
}

const themeOverrides = {
  common: {
    primaryColor: '#18a058',
    primaryColorHover: '#36ad6a',
    primaryColorPressed: '#0c7a43',
    primaryColorSuppl: '#36ad6a',
    borderRadius: '8px'
  }
}
</script>

<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <GlobalProviderSetup />
          <n-layout style="min-height: 100vh">
            <!-- Header -->
            <n-layout-header bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.06);">
              <div style="display: flex; align-items: center; cursor: pointer;" @click="router.push('/')">
                <span style="font-size: 20px; font-weight: 700; color: #18a058; margin-right: 4px;">🅿</span>
                <span style="font-size: 18px; font-weight: 600; color: #18a058;">景区智能停车</span>
              </div>
              <n-menu
                mode="horizontal"
                :value="activeMenu"
                :options="menuOptions"
                style="margin-left: 40px; flex: 1;"
              />
              <n-space align="center">
                <template v-if="isLoggedIn">
                  <n-button text type="primary" @click="router.push('/reservation')">预约车位</n-button>
                  <n-dropdown :options="userMenuOptions" @select="handleUserMenu" trigger="click">
                    <n-button text>
                      <n-space align="center" :size="6">
                        <n-avatar
                          round
                          :size="28"
                          style="background: #18a058; color: #fff; font-size: 14px;"
                        >{{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U')[0] }}</n-avatar>
                        <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
                      </n-space>
                    </n-button>
                  </n-dropdown>
                </template>
                <template v-else>
                  <n-button text type="primary" @click="router.push('/login')">登录</n-button>
                  <n-button type="primary" size="small" @click="router.push('/register')">注册</n-button>
                </template>
              </n-space>
            </n-layout-header>

            <!-- Content -->
            <n-layout-content style="background: #f7faf8;">
              <router-view />
            </n-layout-content>

            <!-- Footer -->
            <n-layout-footer style="text-align: center; padding: 20px; color: #999; font-size: 13px; background: #fff; border-top: 1px solid #eee;">
              © 2026 景区智能停车引导系统 · 毕业设计
            </n-layout-footer>
          </n-layout>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}
#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
