import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// Element Plus Icons
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 全局样式
import './assets/styles/index.css'

// 路由守卫
import { setupRouterGuards } from './router/guards'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Pinia
const pinia = createPinia()
app.use(pinia)

// Router
app.use(router)
setupRouterGuards(router)

// Element Plus
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')