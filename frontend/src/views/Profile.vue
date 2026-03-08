<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { put } from '@/utils/request'
import {
  NCard, NForm, NFormItem, NInput, NButton, NSpace, NAvatar, NDivider, NGrid, NGi, NIcon
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { PersonOutline, CallOutline, MailOutline, LockClosedOutline } from '@vicons/ionicons5'

const userStore = useUserStore()
const profileFormRef = ref<FormInst | null>(null)
const passwordFormRef = ref<FormInst | null>(null)
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileForm = ref({
  nickname: '',
  phone: '',
  email: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules: FormRules = {
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

const passwordRules: FormRules = {
  oldPassword: { required: true, message: '请输入旧密码', trigger: 'blur' },
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== passwordForm.value.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

async function handleSaveProfile() {
  try {
    await profileFormRef.value?.validate()
  } catch { return }

  savingProfile.value = true
  try {
    const userId = userStore.userInfo?.id
    await put(`/api/users/${userId}`, {
      nickname: profileForm.value.nickname,
      phone: profileForm.value.phone,
      email: profileForm.value.email
    })
    window.$message?.success('保存成功')
    await userStore.fetchUserInfo()
  } catch {
    // handled by interceptor
  } finally {
    savingProfile.value = false
  }
}

async function handleChangePassword() {
  try {
    await passwordFormRef.value?.validate()
  } catch { return }

  savingPassword.value = true
  try {
    const userId = userStore.userInfo?.id
    await put(`/api/users/${userId}/password`, {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    window.$message?.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    // handled by interceptor
  } finally {
    savingPassword.value = false
  }
}

function initProfileForm() {
  const info = userStore.userInfo || {}
  profileForm.value = {
    nickname: info.nickname || '',
    phone: info.phone || '',
    email: info.email || ''
  }
}

onMounted(initProfileForm)
</script>

<template>
  <div class="profile-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-desc">管理您的个人信息和账号安全</p>
      </div>

      <n-grid :cols="3" :x-gap="24" :y-gap="24" responsive="screen" item-responsive>
        <!-- Profile Card -->
        <n-gi span="3 m:3 l:1">
          <n-card :bordered="false" style="border-radius: 12px; text-align: center;">
            <n-avatar
              round
              :size="80"
              style="background: linear-gradient(135deg, #18a058, #2080f0); color: #fff; font-size: 32px; margin-bottom: 16px;"
            >
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U')[0] }}
            </n-avatar>
            <h3 style="margin: 0 0 4px; font-size: 18px; color: #333;">
              {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}
            </h3>
            <p style="color: #999; font-size: 13px; margin: 0;">
              {{ userStore.userInfo?.username || '' }}
            </p>
            <n-divider />
            <div class="info-row">
              <n-icon :component="CallOutline" :size="16" color="#888" />
              <span>{{ userStore.userInfo?.phone || '未设置手机号' }}</span>
            </div>
            <div class="info-row">
              <n-icon :component="MailOutline" :size="16" color="#888" />
              <span>{{ userStore.userInfo?.email || '未设置邮箱' }}</span>
            </div>
          </n-card>
        </n-gi>

        <!-- Edit Profile -->
        <n-gi span="3 m:3 l:2">
          <n-card :bordered="false" title="编辑资料" style="border-radius: 12px;">
            <n-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-placement="left" label-width="80">
              <n-form-item label="昵称" path="nickname">
                <n-input v-model:value="profileForm.nickname" placeholder="请输入昵称">
                  <template #prefix><n-icon :component="PersonOutline" /></template>
                </n-input>
              </n-form-item>
              <n-form-item label="手机号" path="phone">
                <n-input v-model:value="profileForm.phone" placeholder="请输入手机号">
                  <template #prefix><n-icon :component="CallOutline" /></template>
                </n-input>
              </n-form-item>
              <n-form-item label="邮箱" path="email">
                <n-input v-model:value="profileForm.email" placeholder="请输入邮箱">
                  <template #prefix><n-icon :component="MailOutline" /></template>
                </n-input>
              </n-form-item>
              <n-form-item>
                <n-button type="primary" :loading="savingProfile" @click="handleSaveProfile">
                  保存修改
                </n-button>
              </n-form-item>
            </n-form>
          </n-card>

          <!-- Change Password -->
          <n-card :bordered="false" title="修改密码" style="border-radius: 12px; margin-top: 24px;">
            <n-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-placement="left" label-width="80">
              <n-form-item label="旧密码" path="oldPassword">
                <n-input v-model:value="passwordForm.oldPassword" type="password" show-password-on="click" placeholder="请输入旧密码">
                  <template #prefix><n-icon :component="LockClosedOutline" /></template>
                </n-input>
              </n-form-item>
              <n-form-item label="新密码" path="newPassword">
                <n-input v-model:value="passwordForm.newPassword" type="password" show-password-on="click" placeholder="请输入新密码">
                  <template #prefix><n-icon :component="LockClosedOutline" /></template>
                </n-input>
              </n-form-item>
              <n-form-item label="确认密码" path="confirmPassword">
                <n-input v-model:value="passwordForm.confirmPassword" type="password" show-password-on="click" placeholder="请确认新密码">
                  <template #prefix><n-icon :component="LockClosedOutline" /></template>
                </n-input>
              </n-form-item>
              <n-form-item>
                <n-button type="warning" :loading="savingPassword" @click="handleChangePassword">
                  修改密码
                </n-button>
              </n-form-item>
            </n-form>
          </n-card>
        </n-gi>
      </n-grid>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 32px 0 60px;
}
.container {
  max-width: 1000px;
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
.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 14px;
  color: #666;
}
</style>
