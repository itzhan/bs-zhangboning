/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface Window {
  $message: import('naive-ui').MessageApiInjection
  $dialog: import('naive-ui').DialogApiInjection
  $notification: import('naive-ui').NotificationApiInjection
}
