<template>
  <div class="visitor-layout min-h-screen flex flex-col">
    <!-- Header -->
    <header class="header bg-white dark:bg-gray-800 shadow-sm sticky top-0 z-50">
      <div class="container mx-auto px-4 h-16 flex items-center justify-between">
        <!-- Logo -->
        <router-link to="/" class="flex items-center space-x-2">
          <NMSLogo :size="'lg'" :variant="'dark'" :show-title="true" />
        </router-link>

        <!-- 导航菜单 -->
        <nav class="hidden md:flex items-center space-x-6">
          <router-link to="/home" class="nav-link" :class="{ 'text-primary': $route.path === '/home' }">首页</router-link>
          <router-link to="/category" class="nav-link" :class="{ 'text-primary': $route.path.startsWith('/category') }">分类</router-link>
          <router-link to="/search" class="nav-link" :class="{ 'text-primary': $route.path === '/search' }">搜索</router-link>
          <el-dropdown v-if="userStore.isLoggedIn" trigger="hover" @command="handleNavCommand">
            <span class="nav-link cursor-pointer flex items-center gap-1" :class="{ 'text-primary': $route.path.startsWith('/user') }">
              我的
              <el-icon class="text-xs"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="history">
                  <el-icon><Clock /></el-icon>
                  阅读历史
                </el-dropdown-item>
                <el-dropdown-item command="favorite">
                  <el-icon><Star /></el-icon>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="comments">
                  <el-icon><ChatDotRound /></el-icon>
                  我的评论
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <!-- 右侧操作区 -->
        <div class="flex items-center space-x-4">
          <!-- 搜索框 -->
          <div class="hidden md:block">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索小说、作者"
              prefix-icon="Search"
              clearable
              class="w-48"
              @keyup.enter="handleSearch"
            />
          </div>

          <!-- 主题切换 -->
          <el-tooltip :content="appStore.isDark ? '切换到浅色模式' : '切换到深色模式'">
            <el-button circle @click="appStore.toggleTheme()">
              <el-icon><Sunny v-if="appStore.isDark" /><Moon v-else /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 用户信息 -->
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="flex items-center space-x-2 cursor-pointer">
                <el-avatar :size="32" :src="userStore.userAvatar" />
                <span class="hidden md:inline text-gray-700 dark:text-gray-200">{{ userStore.userName }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="user">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 bg-gray-50 dark:bg-gray-900">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 py-6">
      <div class="container mx-auto px-4 text-center text-gray-500 dark:text-gray-400 text-sm">
        <p>© 2026 NMS All Rights Reserved</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Sunny, Moon, User, SwitchButton, ArrowDown, Clock, Star, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore, useAppStore } from '@/stores'
import NMSLogo from '@/components/common/nms-logo.vue' // 新增NMS Logo组件

const router = useRouter()

const userStore = useUserStore()
const appStore = useAppStore()

const searchKeyword = ref('')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleNavCommand = (command: string) => {
  const routeMap: Record<string, string> = {
    history: '/history',
    favorite: '/favorite',
    comments: '/comments',
  }
  if (routeMap[command]) {
    router.push(routeMap[command])
  }
}

const handleCommand = (command: string) => {
  if (command === 'user') {
    router.push('/user')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/home')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.nav-link {
  @apply text-gray-600 dark:text-gray-300 hover:text-primary transition-colors;
}
</style>