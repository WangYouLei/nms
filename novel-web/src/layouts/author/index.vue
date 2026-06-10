<template>
  <div class="author-layout h-screen flex bg-gray-50 dark:bg-gray-900">
    <!-- Sidebar -->
    <aside 
      class="sidebar bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 transition-all duration-300 flex flex-col"
      :class="appStore.sidebarCollapsed ? 'w-16' : 'w-60'"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-gray-200 dark:border-gray-700">
        <template v-if="!appStore.sidebarCollapsed">
          <div class="w-8 h-8 rounded-xl bg-gradient-primary flex items-center justify-center">
            <el-icon :size="18" color="#fff"><Edit /></el-icon>
          </div>
          <span class="ml-3 text-lg font-bold text-gray-800 dark:text-gray-200">作者中心</span>
        </template>
        <template v-else>
          <div class="w-8 h-8 rounded-xl bg-gradient-primary flex items-center justify-center">
            <el-icon :size="18" color="#fff"><Edit /></el-icon>
          </div>
        </template>
      </div>

      <!-- Menu -->
      <nav class="flex-1 py-4">
        <div class="space-y-1 px-3">
          <router-link 
            v-for="item in menuItems" 
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200"
            :class="route.path === item.path 
              ? 'bg-primary text-white shadow-lg shadow-primary/30' 
              : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
          >
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!appStore.sidebarCollapsed" class="font-medium">{{ item.title }}</span>
          </router-link>
        </div>
      </nav>

      <!-- Bottom Section -->
      <div v-if="!appStore.sidebarCollapsed" class="p-4 border-t border-gray-200 dark:border-gray-700">
        <div class="bg-gradient-primary rounded-xl p-4 text-white">
          <p class="text-sm opacity-80">创作激励</p>
          <p class="font-bold mt-1">开启你的创作之旅</p>
        </div>
      </div>
    </aside>

    <!-- Main Container -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Header -->
      <header class="h-16 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-6">
        <div class="flex items-center gap-4">
          <button 
            class="w-10 h-10 rounded-xl flex items-center justify-center transition-colors hover:bg-gray-100 dark:hover:bg-gray-700"
            @click="appStore.toggleSidebar()"
          >
            <el-icon :size="20">
              <Fold v-if="!appStore.sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </button>
          <el-breadcrumb separator="/" class="text-sm">
            <el-breadcrumb-item :to="{ path: '/author/dashboard' }">
              <span class="text-gray-500">首页</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">
              <span class="text-gray-800 dark:text-gray-200">{{ route.meta.title }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="flex items-center gap-3">
          <!-- 主题切换 -->
          <button 
            class="w-10 h-10 rounded-xl flex items-center justify-center transition-colors hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300"
            @click="appStore.toggleTheme()"
          >
            <el-icon :size="20">
              <Sunny v-if="appStore.isDark" />
              <Moon v-else />
            </el-icon>
          </button>
          
          <!-- 用户菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="flex items-center gap-3 px-3 py-2 rounded-xl hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer transition-colors">
              <el-avatar :size="36" :src="userStore.userAvatar" class="ring-2 ring-primary/20">
                {{ userStore.userName?.charAt(0) }}
              </el-avatar>
              <div v-if="!appStore.sidebarCollapsed" class="hidden sm:block">
                <p class="text-sm font-medium text-gray-800 dark:text-gray-200">{{ userStore.userName }}</p>
                <p class="text-xs text-gray-500">作者</p>
              </div>
              <el-icon class="text-gray-400"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  账号设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Main Content -->
      <main class="flex-1 overflow-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Edit, DataBoard, Reading, ChatDotRound, Setting, Fold, Expand, 
  Sunny, Moon, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore, useAppStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const menuItems = [
  { path: '/author/dashboard', title: '工作台', icon: DataBoard },
  { path: '/author/novels', title: '我的小说', icon: Reading },
  { path: '/author/comments', title: '评论管理', icon: ChatDotRound },
  { path: '/author/settings', title: '账号设置', icon: Setting }
]

const handleCommand = (command: string) => {
  if (command === 'settings') {
    router.push('/author/settings')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/author/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
/* 路由链接激活样式 */
.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}
</style>