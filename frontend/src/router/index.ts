import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
  { path: '/parking-lots', name: 'ParkingLotList', component: () => import('@/views/ParkingLotList.vue'), meta: { title: '停车场列表' } },
  { path: '/parking-lots/:id', name: 'ParkingLotDetail', component: () => import('@/views/ParkingLotDetail.vue'), meta: { title: '停车场详情' } },
  { path: '/guidance', name: 'Guidance', component: () => import('@/views/Guidance.vue'), meta: { title: '智能引导' } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  { path: '/my-orders', name: 'MyOrders', component: () => import('@/views/MyOrders.vue'), meta: { title: '我的订单', requiresAuth: true } },
  { path: '/my-reservations', name: 'MyReservations', component: () => import('@/views/MyReservations.vue'), meta: { title: '我的预约', requiresAuth: true } },
  { path: '/reservation', name: 'Reservation', component: () => import('@/views/Reservation.vue'), meta: { title: '预约车位', requiresAuth: true } },
  { path: '/my-vehicles', name: 'MyVehicles', component: () => import('@/views/MyVehicles.vue'), meta: { title: '我的车辆', requiresAuth: true } },
  { path: '/my-coupons', name: 'MyCoupons', component: () => import('@/views/MyCoupons.vue'), meta: { title: '我的优惠券', requiresAuth: true } },
  { path: '/appeal', name: 'Appeal', component: () => import('@/views/Appeal.vue'), meta: { title: '异常申诉', requiresAuth: true } },
  { path: '/announcements', name: 'Announcements', component: () => import('@/views/Announcements.vue'), meta: { title: '公告列表' } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || ''} - 景区智能停车引导系统`
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.token) {
      window.$message?.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
