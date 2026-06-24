<template>
  <div class="user-page container mx-auto px-4 py-6">
    <div class="max-w-4xl mx-auto">
      <!-- 用户信息卡片 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 mb-6">
        <div class="flex items-center gap-6">
          <el-avatar :size="80" :src="userStore.userAvatar" />
          <div>
            <h2 class="text-xl font-bold text-gray-800 dark:text-gray-200">{{ userStore.userName }}</h2>
            <p class="text-gray-500 dark:text-gray-400 mt-1">{{ userStore.userInfo?.account }}</p>
          </div>
          <div class="ml-auto">
            <el-button @click="router.push('/author/login')">切换到作者端</el-button>
          </div>
        </div>
      </div>
      
      <!-- 功能菜单 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
        <div 
          v-for="menu in menus" 
          :key="menu.path"
          class="flex items-center justify-between p-4 border-b border-gray-100 dark:border-gray-700 last:border-b-0 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer"
          @click="handleMenuClick(menu)"
        >
          <div class="flex items-center gap-3">
            <el-icon :size="20"><component :is="menu.icon" /></el-icon>
            <span class="text-gray-700 dark:text-gray-200">{{ menu.title }}</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowRight, Clock, Star, ChatDotRound, Setting, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const menus = [
  { title: '阅读历史', icon: Clock, path: 'history' },
  { title: '我的收藏', icon: Star, path: 'favorite' },
  { title: '我的评论', icon: ChatDotRound, path: '/comments' },
  { title: '账号设置', icon: Setting, path: '/settings' },
  { title: '退出登录', icon: SwitchButton, path: 'logout' }
]

const handleMenuClick = (menu: typeof menus[0]) => {
  if (menu.path === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/home')
    }).catch(() => {})
  } else if (menu.path === 'favorite') {
    router.push('/favorite')
  } else if (menu.path === '/settings') {
    router.push('/settings')
  } else if (menu.path === '/comments') {
    router.push('/comments')
  } else if (menu.path === 'history') {
    router.push('/history')
  } else {
    ElMessage.info('功能开发中')
  }
}
</script>