<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  NCard, NForm, NFormItem, NInput, NButton, NSpace, NIcon
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInst | null>(null)

const formData = ref({
  username: '',
  password: ''
})

const loading = ref(false)

const rules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await userStore.login(formData.value.username, formData.value.password)
    window.$message?.success('登录成功')
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch {
    // error handled in request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <n-card class="login-card" :bordered="false">
        <div class="login-header">
          <div class="login-logo"><svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#18a058" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="M9 17V7h4a3 3 0 0 1 0 6H9"/></svg></div>
          <h2 class="login-title">景区智能停车引导系统</h2>
          <p class="login-desc">欢迎回来，请登录您的账号</p>
        </div>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" size="large">
          <n-form-item path="username">
            <n-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon :component="PersonOutline" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item path="password">
            <n-input
              v-model:value="formData.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon :component="LockClosedOutline" />
              </template>
            </n-input>
          </n-form-item>
          <n-button
            type="primary"
            block
            size="large"
            :loading="loading"
            style="margin-top: 8px; border-radius: 8px;"
            @click="handleLogin"
          >
            登 录
          </n-button>
        </n-form>
        <div class="login-footer">
          还没有账号？
          <n-button text type="primary" @click="router.push('/register')">立即注册</n-button>
        </div>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0faf4 0%, #e8f4fd 100%);
  position: relative;
  overflow: hidden;
}
.login-page::before {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(24, 160, 88, 0.06);
  top: -100px;
  right: -100px;
}
.login-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(32, 128, 240, 0.06);
  bottom: -80px;
  left: -80px;
}
.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}
.login-card {
  border-radius: 16px;
  padding: 20px 8px;
  box-shadow: 0 8px 32px rgba(0,0,0,.06);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-logo {
  font-size: 48px;
  margin-bottom: 8px;
}
.login-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}
.login-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #888;
}
</style>
