<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NMenu,
  NButton,
  NDropdown,
  NAvatar,
  NSpace,
  NIcon,
  NFlex
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  HomeOutline,
  CarOutline,
  CompassOutline,
  MegaphoneOutline,
  PersonCircleOutline,
  LogOutOutline,
  ReceiptOutline,
  CalendarOutline,
  CarSportOutline,
  PricetagsOutline,
  AlertCircleOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// Active menu key
const activeKey = computed(() => {
  const path = route.path
  if (path.startsWith('/parking-lots')) return '/parking-lots'
  if (path.startsWith('/guidance')) return '/guidance'
  if (path === '/home' || path === '/') return '/home'
  return path
})

// Navigation menu
const menuOptions: MenuOption[] = [
  {
    label: () => h(RouterLink, { to: '/home' }, { default: () => '首页' }),
    key: '/home',
    icon: () => h(NIcon, null, { default: () => h(HomeOutline) })
  },
  {
    label: () => h(RouterLink, { to: '/parking-lots' }, { default: () => '停车场' }),
    key: '/parking-lots',
    icon: () => h(NIcon, null, { default: () => h(CarOutline) })
  },
  {
    label: () => h(RouterLink, { to: '/guidance' }, { default: () => '智能引导' }),
    key: '/guidance',
    icon: () => h(NIcon, null, { default: () => h(CompassOutline) })
  },
  {
    label: () => h(RouterLink, { to: '/announcements' }, { default: () => '公告' }),
    key: '/announcements',
    icon: () => h(NIcon, null, { default: () => h(MegaphoneOutline) })
  }
]

// User dropdown options
const userDropdownOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonCircleOutline) })
  },
  {
    label: '我的订单',
    key: 'my-orders',
    icon: () => h(NIcon, null, { default: () => h(ReceiptOutline) })
  },
  {
    label: '我的预约',
    key: 'my-reservations',
    icon: () => h(NIcon, null, { default: () => h(CalendarOutline) })
  },
  {
    label: '我的车辆',
    key: 'my-vehicles',
    icon: () => h(NIcon, null, { default: () => h(CarSportOutline) })
  },
  {
    label: '我的优惠券',
    key: 'my-coupons',
    icon: () => h(NIcon, null, { default: () => h(PricetagsOutline) })
  },
  {
    label: '申诉',
    key: 'appeal',
    icon: () => h(NIcon, null, { default: () => h(AlertCircleOutline) })
  },
  { type: 'divider' as const, key: 'd1' },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
  }
]

function handleUserAction(key: string) {
  if (key === 'logout') {
    userStore.logout()
  } else {
    router.push(`/${key}`)
  }
}
</script>

<template>
  <NLayout class="layout-container">
    <!-- Header -->
    <NLayoutHeader bordered class="layout-header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="router.push('/home')">
          <span class="logo-icon"><svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="M9 17V7h4a3 3 0 0 1 0 6H9"/></svg></span>
          <span class="logo-text">景区停车引导系统</span>
        </div>

        <!-- Navigation -->
        <div class="nav-menu">
          <NMenu
            mode="horizontal"
            :value="activeKey"
            :options="menuOptions"
            responsive
          />
        </div>

        <!-- Right side -->
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <NDropdown
              :options="userDropdownOptions"
              trigger="hover"
              @select="handleUserAction"
            >
              <NFlex align="center" :size="8" style="cursor: pointer">
                <NAvatar
                  round
                  :size="32"
                  :src="userStore.userInfo.avatar || undefined"
                >
                  {{ userStore.displayName.charAt(0) }}
                </NAvatar>
                <span class="username">{{ userStore.displayName }}</span>
              </NFlex>
            </NDropdown>
          </template>
          <template v-else>
            <NSpace>
              <NButton text type="primary" @click="router.push('/login')">
                登录
              </NButton>
              <NButton type="primary" round size="small" @click="router.push('/register')">
                注册
              </NButton>
            </NSpace>
          </template>
        </div>
      </div>
    </NLayoutHeader>

    <!-- Content -->
    <NLayoutContent class="layout-content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </NLayoutContent>

    <!-- Footer -->
    <NLayoutFooter class="layout-footer">
      <div class="footer-content">
        <p>© 2026 景区停车引导系统 All Rights Reserved</p>
        <p class="footer-sub">智慧停车 · 畅游无忧</p>
      </div>
    </NLayoutFooter>
  </NLayout>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #18a058, #2080f0);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 32px;
  overflow: hidden;
}

.nav-menu :deep(.n-menu) {
  border-bottom: none !important;
}

.header-right {
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  color: #333;
}

.layout-content {
  min-height: calc(100vh - 60px - 120px);
  background: #f5f7fa;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.layout-footer {
  background: linear-gradient(135deg, #18a058 0%, #2080f0 100%);
  padding: 32px 24px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
}

.footer-content p {
  margin: 0;
  font-size: 14px;
}

.footer-sub {
  margin-top: 8px !important;
  font-size: 12px !important;
  color: rgba(255, 255, 255, 0.65);
}
</style>
