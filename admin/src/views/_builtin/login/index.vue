<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';
import CodeLogin from './modules/code-login.vue';
import Register from './modules/register.vue';
import ResetPwd from './modules/reset-pwd.vue';
import BindWechat from './modules/bind-wechat.vue';

interface Props {
  /** The login module */
  module?: UnionKey.LoginModule;
}

const props = defineProps<Props>();

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: App.I18n.I18nKey;
  component: Component;
}

const moduleMap: Record<UnionKey.LoginModule, LoginModule> = {
  'pwd-login': { label: loginModuleRecord['pwd-login'], component: PwdLogin },
  'code-login': { label: loginModuleRecord['code-login'], component: CodeLogin },
  register: { label: loginModuleRecord.register, component: Register },
  'reset-pwd': { label: loginModuleRecord['reset-pwd'], component: ResetPwd },
  'bind-wechat': { label: loginModuleRecord['bind-wechat'], component: BindWechat }
};

const activeModule = computed(() => moduleMap[props.module || 'pwd-login']);
</script>

<template>
  <div class="login-page">
    <!-- Left Panel: Parking Theme Banner -->
    <div class="login-left">
      <div class="left-overlay"></div>
      <div class="left-content">
        <!-- Parking Icon SVG -->
        <div class="left-logo">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect width="18" height="18" x="3" y="3" rx="2"/>
            <path d="M9 17V7h4a3 3 0 0 1 0 6H9"/>
          </svg>
        </div>
        <h1 class="left-title">景区停车引导系统</h1>
        <p class="left-subtitle">Smart Parking Guidance System</p>
        <div class="left-features">
          <div class="feature-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/><circle cx="12" cy="10" r="3"/></svg>
            <span>10大景区停车场智能管理</span>
          </div>
          <div class="feature-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v16a2 2 0 0 0 2 2h16"/><path d="m19 9-5 5-4-4-3 3"/></svg>
            <span>实时车位占用率统计</span>
          </div>
          <div class="feature-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"/><line x1="16" x2="16" y1="2" y2="6"/><line x1="8" x2="8" y1="2" y2="6"/><line x1="3" x2="21" y1="10" y2="10"/></svg>
            <span>在线预约与支付管理</span>
          </div>
          <div class="feature-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/></svg>
            <span>黑名单与异常申诉处理</span>
          </div>
        </div>
        <div class="left-footer">
          <p>© 2026 毕业设计 · 张博宁</p>
        </div>
      </div>
    </div>

    <!-- Right Panel: Login Form -->
    <div class="login-right">
      <NCard :bordered="false" class="login-card">
        <div class="login-form-wrapper">
          <header class="login-header">
            <div class="login-header-top">
              <div class="login-logo-small">
                <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect width="18" height="18" x="3" y="3" rx="2"/>
                  <path d="M9 17V7h4a3 3 0 0 1 0 6H9"/>
                </svg>
              </div>
              <div class="login-header-actions">
                <ThemeSchemaSwitch
                  :theme-schema="themeStore.themeScheme"
                  :show-tooltip="false"
                  class="text-20px"
                  @switch="themeStore.toggleThemeScheme"
                />
                <LangSwitch
                  v-if="themeStore.header.multilingual.visible"
                  :lang="appStore.locale"
                  :lang-options="appStore.localeOptions"
                  :show-tooltip="false"
                  @change-lang="appStore.changeLocale"
                />
              </div>
            </div>
            <h2 class="login-title">{{ $t('system.title') }}</h2>
            <p class="login-desc">管理后台</p>
          </header>
          <main class="login-main">
            <h3 class="login-module-title">{{ $t(activeModule.label) }}</h3>
            <div class="pt-24px">
              <Transition :name="themeStore.page.animateMode" mode="out-in" appear>
                <component :is="activeModule.component" />
              </Transition>
            </div>
          </main>
        </div>
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* Left Panel */
.login-left {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #1a6dd8 0%, #0d4e9e 50%, #18a058 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.left-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(24, 160, 88, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(32, 128, 240, 0.3) 0%, transparent 50%);
}

.left-overlay::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 60%);
  animation: float 25s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(-3%, 3%) rotate(1deg); }
  66% { transform: translate(2%, -2%) rotate(-1deg); }
}

.left-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 40px;
  max-width: 500px;
}

.left-logo {
  margin-bottom: 24px;
  opacity: 0.95;
}

.left-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 3px;
}

.left-subtitle {
  font-size: 16px;
  opacity: 0.7;
  margin: 0 0 48px;
  letter-spacing: 2px;
  font-weight: 300;
}

.left-features {
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 0 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 15px;
  opacity: 0.9;
  padding: 10px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(4px);
  transition: background 0.3s, transform 0.2s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(4px);
}

.left-footer {
  margin-top: 60px;
  opacity: 0.5;
  font-size: 13px;
}

.left-footer p {
  margin: 0;
}

/* Right Panel */
.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--n-color);
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: transparent !important;
}

.login-form-wrapper {
  width: 100%;
}

.login-header {
  margin-bottom: 32px;
}

.login-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.login-header-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.login-logo-small {
  display: flex;
  align-items: center;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--n-text-color);
  margin: 0 0 4px;
}

.login-desc {
  font-size: 14px;
  color: var(--n-text-color-3);
  margin: 0;
}

.login-module-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--primary-color);
  margin: 0 0 24px;
}

/* Responsive */
@media (max-width: 900px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}
</style>
