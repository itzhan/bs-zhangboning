<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/utils/request'
import {
  NCard, NForm, NFormItem, NInput, NButton, NIcon
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { PersonOutline, LockClosedOutline, CallOutline, MailOutline } from '@vicons/ionicons5'

const router = useRouter()
const formRef = ref<FormInst | null>(null)

const formData = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: ''
})

const loading = ref(false)

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== formData.value.password) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  nickname: { required: true, message: '请输入昵称', trigger: 'blur' },
  phone: {
    pattern: /^1[3-9]\d{9}$/,
    message: '请输入正确的手机号',
    trigger: 'blur'
  },
  email: {
    type: 'email',
    message: '请输入正确的邮箱地址',
    trigger: 'blur'
  }
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await post('/api/auth/register', {
      username: formData.value.username,
      password: formData.value.password,
      nickname: formData.value.nickname,
      phone: formData.value.phone,
      email: formData.value.email
    })
    window.$message?.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // error handled in request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <n-card class="register-card" :bordered="false">
        <div class="register-header">
          <div class="register-logo">🅿</div>
          <h2 class="register-title">创建新账号</h2>
          <p class="register-desc">注册后享受便捷停车服务</p>
        </div>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" size="large">
          <n-form-item path="username">
            <n-input v-model:value="formData.username" placeholder="用户名">
              <template #prefix><n-icon :component="PersonOutline" /></template>
            </n-input>
          </n-form-item>
          <n-form-item path="password">
            <n-input v-model:value="formData.password" type="password" show-password-on="click" placeholder="密码">
              <template #prefix><n-icon :component="LockClosedOutline" /></template>
            </n-input>
          </n-form-item>
          <n-form-item path="confirmPassword">
            <n-input v-model:value="formData.confirmPassword" type="password" show-password-on="click" placeholder="确认密码">
              <template #prefix><n-icon :component="LockClosedOutline" /></template>
            </n-input>
          </n-form-item>
          <n-form-item path="nickname">
            <n-input v-model:value="formData.nickname" placeholder="昵称">
              <template #prefix><n-icon :component="PersonOutline" /></template>
            </n-input>
          </n-form-item>
          <n-form-item path="phone">
            <n-input v-model:value="formData.phone" placeholder="手机号（选填）">
              <template #prefix><n-icon :component="CallOutline" /></template>
            </n-input>
          </n-form-item>
          <n-form-item path="email">
            <n-input v-model:value="formData.email" placeholder="邮箱（选填）">
              <template #prefix><n-icon :component="MailOutline" /></template>
            </n-input>
          </n-form-item>
          <n-button
            type="primary"
            block
            size="large"
            :loading="loading"
            style="margin-top: 8px; border-radius: 8px;"
            @click="handleRegister"
          >
            注 册
          </n-button>
        </n-form>
        <div class="register-footer">
          已有账号？
          <n-button text type="primary" @click="router.push('/login')">返回登录</n-button>
        </div>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0faf4 0%, #e8f4fd 100%);
  position: relative;
  overflow: hidden;
}
.register-page::before {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(32, 128, 240, 0.06);
  top: -120px;
  left: -120px;
}
.register-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(24, 160, 88, 0.06);
  bottom: -100px;
  right: -100px;
}
.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: 20px;
}
.register-card {
  border-radius: 16px;
  padding: 20px 8px;
  box-shadow: 0 8px 32px rgba(0,0,0,.06);
}
.register-header {
  text-align: center;
  margin-bottom: 24px;
}
.register-logo {
  font-size: 48px;
  margin-bottom: 8px;
}
.register-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}
.register-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #888;
}
</style>
