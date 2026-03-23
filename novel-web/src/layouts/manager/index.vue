<template>
  <div class="manager-layout h-screen flex bg-gray-100 dark:bg-gray-900">
    <!-- Sidebar - 深色主题 -->
    <aside 
      class="sidebar bg-gray-900 text-white transition-all duration-300 flex flex-col"
      :class="appStore.sidebarCollapsed ? 'w-16' : 'w-60'"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-gray-800">
        <template v-if="!appStore.sidebarCollapsed">
          <div class="w-8 h-8 rounded-xl bg-gradient-primary flex items-center justify-center">
            <el-icon :size="18" color="#fff"><Setting /></el-icon>
          </div>
          <span class="ml-3 text-lg font-bold text-white">管理后台</span>
        </template>
        <template v-else>
          <div class="w-8 h-8 rounded-xl bg-gradient-primary flex items-center justify-center">
            <el-icon :size="18" color="#fff"><Setting /></el-icon>
          </div>
        </template>
      </div>

      <!-- Menu -->
      <nav class="flex-1 py-4 overflow-y-auto">
        <div class="space-y-1 px-3">
          <router-link 
            :to="'/manager/dashboard'"
            class="flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200"
            :class="route.path === '/manager/dashboard' 
              ? 'bg-primary text-white shadow-lg shadow-primary/30' 
              : 'text-gray-400 hover:bg-gray-800 hover:text-white'"
          >
            <el-icon :size="20"><DataBoard /></el-icon>
            <span v-if="!appStore.sidebarCollapsed" class="font-medium">数据概览</span>
          </router-link>
          
          <!-- 内容管理分组 -->
          <div v-if="!appStore.sidebarCollapsed" class="pt-4 pb-2">
            <p class="px-3 text-xs text-gray-500 uppercase tracking-wider">内容管理</p>
          </div>
          <router-link 
            v-for="item in contentMenuItems" 
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200"
            :class="route.path === item.path 
              ? 'bg-primary text-white shadow-lg shadow-primary/30' 
              : 'text-gray-400 hover:bg-gray-800 hover:text-white'"
          >
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!appStore.sidebarCollapsed" class="font-medium">{{ item.title }}</span>
          </router-link>
          
          <!-- 用户管理分组 -->
          <div v-if="!appStore.sidebarCollapsed" class="pt-4 pb-2">
            <p class="px-3 text-xs text-gray-500 uppercase tracking-wider">用户管理</p>
          </div>
          <router-link 
            v-for="item in userMenuItems" 
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200"
            :class="route.path === item.path 
              ? 'bg-primary text-white shadow-lg shadow-primary/30' 
              : 'text-gray-400 hover:bg-gray-800 hover:text-white'"
          >
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!appStore.sidebarCollapsed" class="font-medium">{{ item.title }}</span>
          </router-link>
        </div>
      </nav>

      <!-- 底部统计 -->
      <div v-if="!appStore.sidebarCollapsed" class="p-4 border-t border-gray-800">
        <div class="bg-gray-800 rounded-xl p-4">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-400">系统状态</span>
            <span class="flex items-center gap-1 text-xs text-green-400">
              <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
              正常
            </span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Container -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Header -->
      <header class="h-16 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-6">
        <div class="flex items-center gap-4">
          <button 
            class="w-10 h-10 rounded-xl flex items-center justify-center transition-colors hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300"
            @click="appStore.toggleSidebar()"
          >
            <el-icon :size="20">
              <Fold v-if="!appStore.sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </button>
          <el-breadcrumb separator="/" class="text-sm">
            <el-breadcrumb-item :to="{ path: '/manager/dashboard' }">
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
              <div class="hidden sm:block">
                <p class="text-sm font-medium text-gray-800 dark:text-gray-200">{{ userStore.userName }}</p>
                <p class="text-xs text-gray-500">管理员</p>
              </div>
              <el-icon class="text-gray-400"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
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
    
    <!-- Footer -->
    <footer class="bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 py-6">
      <div class="container mx-auto px-4 text-center text-gray-500 dark:text-gray-400 text-sm">
        <p>© 2026 NMS All Rights Reserved</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Setting, DataBoard, User, Fold, Expand, 
  Sunny, Moon, ArrowDown, SwitchButton, Document, Avatar, List
} from '@element-plus/icons-vue'
import { useUserStore, useAppStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const contentMenuItems = [
  { path: '/manager/novels', title: '小说管理', icon: Document },
  { path: '/manager/categories', title: '分类管理', icon: List }
]

const userMenuItems = [
  { path: '/manager/visitors', title: '访客管理', icon: User },
  { path: '/manager/authors', title: '作者管理', icon: Avatar },
  { path: '/manager/administrators', title: '管理员', icon: Setting }
]

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/manager/login')
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