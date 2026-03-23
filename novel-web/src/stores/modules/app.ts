import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type Theme = 'light' | 'dark'

export const useAppStore = defineStore('app', () => {
  // 状态
  const theme = ref<Theme>((localStorage.getItem('theme') as Theme) || 'light')
  const sidebarCollapsed = ref(false)
  const device = ref<'desktop' | 'mobile'>('desktop')
  const loading = ref(false)

  // 计算属性
  const isDark = computed(() => theme.value === 'dark')

  // 切换主题
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
    localStorage.setItem('theme', theme.value)
  }

  // 设置主题
  function setTheme(newTheme: Theme) {
    theme.value = newTheme
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
    localStorage.setItem('theme', newTheme)
  }

  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置设备类型
  function setDevice(newDevice: 'desktop' | 'mobile') {
    device.value = newDevice
    if (newDevice === 'mobile') {
      sidebarCollapsed.value = true
    }
  }

  // 设置加载状态
  function setLoading(value: boolean) {
    loading.value = value
  }

  // 初始化
  function init() {
    const savedTheme = localStorage.getItem('theme') as Theme
    if (savedTheme) {
      theme.value = savedTheme
      document.documentElement.classList.toggle('dark', savedTheme === 'dark')
    }
    
    // 检测设备类型
    const isMobile = window.innerWidth < 768
    device.value = isMobile ? 'mobile' : 'desktop'
    if (isMobile) {
      sidebarCollapsed.value = true
    }
  }

  return {
    theme,
    sidebarCollapsed,
    device,
    loading,
    isDark,
    toggleTheme,
    setTheme,
    toggleSidebar,
    setDevice,
    setLoading,
    init
  }
})