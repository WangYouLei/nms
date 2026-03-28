<template>
  <div class="dashboard-page space-y-6">
    <!-- 欢迎横幅 -->
    <div class="relative overflow-hidden rounded-2xl bg-gradient-primary p-6 md:p-8">
      <!-- 装饰元素 -->
      <div class="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
      <div class="absolute bottom-0 left-0 w-48 h-48 bg-white/10 rounded-full translate-y-1/2 -translate-x-1/2"></div>
      
      <div class="relative">
        <h2 class="text-2xl md:text-3xl font-bold text-white">欢迎回来，{{ userStore.userName }}！</h2>
        <p class="text-white/80 mt-2">开始创作你的下一部精彩作品吧</p>
        
        <div class="flex flex-wrap gap-3 mt-6">
          <el-button 
            type="primary" 
            size="large"
            class="!bg-white/20 !border-white/30 !text-white hover:!bg-white/30"
            @click="router.push('/author/novel/create')"
          >
            <el-icon class="mr-2"><Plus /></el-icon>
            新建小说
          </el-button>
          <el-button 
            size="large"
            class="!bg-white/20 !border-white/30 !text-white hover:!bg-white/30"
            @click="router.push('/author/novels')"
          >
            <el-icon class="mr-2"><Reading /></el-icon>
            管理作品
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 数据概览 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatisticsCard 
        title="作品数量" 
        :value="statistics.novelCount" 
        :icon="Reading"
        icon-color="#409eff"
        icon-bg-class="bg-primary/10"
        :glass-effect="true"
      />
      <StatisticsCard 
        title="总章节" 
        :value="statistics.chapterCount" 
        :icon="Document"
        icon-color="#67c23a"
        icon-bg-class="bg-green-500/10"
        :glass-effect="true"
      />
      <StatisticsCard 
        title="总字数" 
        :value="statistics.wordCount" 
        :icon="EditPen"
        icon-color="#e6a23c"
        icon-bg-class="bg-yellow-500/10"
        unit="字"
        :glass-effect="true"
      />
      <StatisticsCard 
        title="粉丝数" 
        :value="statistics.followerCount" 
        :icon="User"
        icon-color="#f56c6c"
        icon-bg-class="bg-red-500/10"
        :glass-effect="true"
      />
    </div>
    
    <!-- 快捷操作和最近作品 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 快捷操作 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
          <el-icon class="text-primary" :size="18"><Operation /></el-icon>
          快捷操作
        </h3>
        <div class="space-y-3">
          <button 
            class="w-full flex items-center gap-3 p-4 rounded-xl bg-primary/5 hover:bg-primary/10 text-primary transition-colors"
            @click="router.push('/author/novel/create')"
          >
            <el-icon :size="24"><Plus /></el-icon>
            <div class="text-left">
              <p class="font-medium">新建小说</p>
              <p class="text-xs text-gray-500">开始创作新作品</p>
            </div>
          </button>
          <button 
            class="w-full flex items-center gap-3 p-4 rounded-xl bg-green-500/5 hover:bg-green-500/10 text-green-600 transition-colors"
            @click="router.push('/author/novels')"
          >
            <el-icon :size="24"><Edit /></el-icon>
            <div class="text-left">
              <p class="font-medium">继续创作</p>
              <p class="text-xs text-gray-500">编辑已有作品</p>
            </div>
          </button>
          <button 
            class="w-full flex items-center gap-3 p-4 rounded-xl bg-yellow-500/5 hover:bg-yellow-500/10 text-yellow-600 transition-colors"
            @click="router.push('/author/settings')"
          >
            <el-icon :size="24"><Setting /></el-icon>
            <div class="text-left">
              <p class="font-medium">账号设置</p>
              <p class="text-xs text-gray-500">修改个人信息</p>
            </div>
          </button>
        </div>
      </div>
      
      <!-- 最近作品 -->
      <div class="lg:col-span-2 bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
            <el-icon class="text-primary" :size="18"><Reading /></el-icon>
            我的作品
          </h3>
          <router-link to="/author/novels" class="text-sm text-primary hover:text-primary-dark">
            查看全部
          </router-link>
        </div>
        
        <div v-if="loading" class="flex justify-center py-8">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        </div>
        
        <div v-else-if="recentNovels.length === 0" class="text-center py-8">
          <el-icon :size="48" class="text-gray-300 dark:text-gray-600 mb-3"><Document /></el-icon>
          <p class="text-gray-400">暂无作品</p>
          <el-button type="primary" class="mt-4" @click="router.push('/author/novel/create')">
            创建第一部作品
          </el-button>
        </div>
        
        <div v-else class="space-y-3">
          <div 
            v-for="novel in recentNovels" 
            :key="novel.id"
            class="flex items-center gap-4 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
            @click="router.push(`/author/novel/${novel.id}/chapters`)"
          >
            <img 
              :src="getImageUrl(novel.url)" 
              class="w-12 h-16 rounded-lg object-cover"
            />
            <div class="flex-1 min-w-0">
              <p class="font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</p>
              <div class="flex items-center gap-3 mt-1 text-xs text-gray-400">
                <span>{{ novel.chapterCount || 0 }}章</span>
                <span>{{ formatWordCount(novel.allWordCount || 0) }}字</span>
                <span>{{ novel.collectCount || 0 }}人收藏</span>
              </div>
            </div>
            <el-tag 
              size="small" 
              :type="novel.isFinished ? 'success' : 'primary'"
            >
              {{ novel.isFinished ? '完结' : '连载中' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Document, EditPen, User, Plus, Operation, Setting, Edit, Loading } from '@element-plus/icons-vue'
import StatisticsCard from '@/components/business/statistics-card.vue'
import { useUserStore } from '@/stores'
import { getImageUrl } from '@/utils/file-url'
import { searchNovels, getFollowerCount } from '@/api'
import type { NovelListVO } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const statistics = reactive({
  novelCount: 0,
  chapterCount: 0,
  wordCount: 0,
  followerCount: 0
})

const recentNovels = ref<NovelListVO[]>([])

// 格式化字数显示
const formatWordCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    // 获取作者的小说列表
    const res = await searchNovels({
      pageNum: 1,
      pageSize: 100 // 获取所有小说
    })
    
    const novels = res.data?.list || []
    recentNovels.value = novels.slice(0, 5) // 只显示最近5部
    
    // 计算统计数据
    statistics.novelCount = novels.length
    statistics.chapterCount = novels.reduce((sum: number, n: NovelListVO) => sum + (n.chapterCount || 0), 0)
    statistics.wordCount = novels.reduce((sum: number, n: NovelListVO) => sum + (n.allWordCount || 0), 0)
    
    // 获取粉丝数
    if (userStore.userId) {
      const followerRes = await getFollowerCount(userStore.userId)
      statistics.followerCount = followerRes.data || 0
    }
  } catch (error) {
    console.error('Failed to fetch statistics:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>