<template>
  <div class="manager-dashboard space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">数据概览</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">平台运营数据一览</p>
      </div>
      <el-button @click="fetchData">
        <el-icon class="mr-2"><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatisticsCard 
        title="小说总数" 
        :value="overview.novelCount" 
        :icon="Reading"
        icon-color="#409eff"
        icon-bg-class="bg-primary/10"
        :show-decor="true"
        :glass-effect="true"
      />
      <StatisticsCard 
        title="作者总数" 
        :value="overview.authorCount" 
        :icon="User"
        icon-color="#67c23a"
        icon-bg-class="bg-green-500/10"
        :show-decor="true"
        :glass-effect="true"
      />
      <StatisticsCard 
        title="用户总数" 
        :value="overview.visitorCount" 
        :icon="Avatar"
        icon-color="#e6a23c"
        icon-bg-class="bg-yellow-500/10"
        :show-decor="true"
        :glass-effect="true"
      />
      <StatisticsCard 
        title="分类总数" 
        :value="overview.categoryCount" 
        :icon="Folder"
        icon-color="#f56c6c"
        icon-bg-class="bg-red-500/10"
        :show-decor="true"
        :glass-effect="true"
      />
    </div>
    
    <!-- 新增数据 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
      <div class="flex items-center justify-between mb-5">
        <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
          <el-icon class="text-primary" :size="18"><Calendar /></el-icon>
          新增数据
        </h3>
        <div class="flex items-center gap-1 bg-gray-100 dark:bg-gray-700 rounded-lg p-1">
          <button
            v-for="opt in periodOptions"
            :key="opt.value"
            class="px-3 py-1 rounded-md text-sm transition-all"
            :class="period === opt.value ? 'bg-white dark:bg-gray-600 text-primary shadow-sm' : 'text-gray-500 hover:text-gray-700'"
            @click="period = opt.value"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="relative overflow-hidden rounded-xl bg-gradient-primary p-5 text-white">
          <div class="absolute top-0 right-0 w-24 h-24 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
          <p class="text-3xl font-bold">{{ currentPeriodData.newNovels }}</p>
          <p class="text-white/80 mt-1">新增小说</p>
        </div>
        <div class="relative overflow-hidden rounded-xl bg-gradient-cool p-5 text-white">
          <div class="absolute top-0 right-0 w-24 h-24 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
          <p class="text-3xl font-bold">{{ currentPeriodData.newAuthors }}</p>
          <p class="text-white/80 mt-1">新增作者</p>
        </div>
        <div class="relative overflow-hidden rounded-xl bg-gradient-warm p-5 text-white">
          <div class="absolute top-0 right-0 w-24 h-24 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
          <p class="text-3xl font-bold">{{ currentPeriodData.newVisitors }}</p>
          <p class="text-white/80 mt-1">新增用户</p>
        </div>
      </div>
    </div>
    
    <!-- 排行榜 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 连载榜 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
            <el-icon class="text-orange-500" :size="18"><TrendCharts /></el-icon>
            连载榜
          </h3>
          <span class="text-xs text-gray-400">TOP 10</span>
        </div>
        
        <div v-if="ongoingRanking.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>
        <div v-else class="space-y-2">
          <div 
            v-for="(novel, index) in ongoingRanking"
            :key="novel.id"
            class="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          >
            <span 
              class="w-7 h-7 flex items-center justify-center rounded-lg text-sm font-bold flex-shrink-0"
              :class="{
                'bg-yellow-400 text-white': index === 0,
                'bg-gray-300 text-white': index === 1,
                'bg-amber-600 text-white': index === 2,
                'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
              }"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1 min-w-0">
              <p class="font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</p>
              <p class="text-sm text-gray-500">{{ novel.authorName }}</p>
            </div>
            <span class="text-sm text-gray-400 flex-shrink-0">{{ novel.chapterCount }}章</span>
          </div>
        </div>
      </div>
      
      <!-- 作者高产榜 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
            <el-icon class="text-green-500" :size="18"><Medal /></el-icon>
            作者高产榜
          </h3>
          <span class="text-xs text-gray-400">TOP 10</span>
        </div>
        
        <div v-if="authorRanking.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>
        <div v-else class="space-y-2">
          <div 
            v-for="(author, index) in authorRanking" 
            :key="author.id"
            class="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          >
            <span 
              class="w-7 h-7 flex items-center justify-center rounded-lg text-sm font-bold flex-shrink-0"
              :class="{
                'bg-yellow-400 text-white': index === 0,
                'bg-gray-300 text-white': index === 1,
                'bg-amber-600 text-white': index === 2,
                'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
              }"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1">
              <p class="font-medium text-gray-800 dark:text-gray-200">{{ author.name }}</p>
            </div>
            <span class="text-sm text-gray-400 flex-shrink-0">{{ author.novelCount }}部作品</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 新增排行榜 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 收藏榜 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
            <el-icon class="text-yellow-500" :size="18"><StarFilled /></el-icon>
            收藏榜
          </h3>
          <span class="text-xs text-gray-400">TOP 10</span>
        </div>
        
        <div v-if="collectRanking.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>
        <div v-else class="space-y-2">
          <div 
            v-for="(novel, index) in collectRanking" 
            :key="novel.id"
            class="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          >
            <span 
              class="w-7 h-7 flex items-center justify-center rounded-lg text-sm font-bold flex-shrink-0"
              :class="{
                'bg-yellow-400 text-white': index === 0,
                'bg-gray-300 text-white': index === 1,
                'bg-amber-600 text-white': index === 2,
                'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
              }"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1 min-w-0">
              <p class="font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</p>
              <p class="text-sm text-gray-500">{{ novel.authorName }}</p>
            </div>
            <span class="text-sm text-gray-400 flex-shrink-0">{{ novel.collectCount ?? 0 }}收藏</span>
          </div>
        </div>
      </div>
      
      <!-- 最新更新榜 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
            <el-icon class="text-blue-500" :size="18"><Clock /></el-icon>
            最新更新榜
          </h3>
          <span class="text-xs text-gray-400">TOP 10</span>
        </div>
        
        <div v-if="latestRanking.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>
        <div v-else class="space-y-2">
          <div 
            v-for="(novel, index) in latestRanking" 
            :key="novel.id"
            class="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          >
            <span 
              class="w-7 h-7 flex items-center justify-center rounded-lg text-sm font-bold flex-shrink-0"
              :class="{
                'bg-yellow-400 text-white': index === 0,
                'bg-gray-300 text-white': index === 1,
                'bg-amber-600 text-white': index === 2,
                'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
              }"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1 min-w-0">
              <p class="font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</p>
              <p class="text-sm text-gray-500">{{ novel.authorName }}</p>
            </div>
            <span class="text-sm text-gray-400 flex-shrink-0">{{ novel.chapterCount }}章</span>
          </div>
        </div>
      </div>
      
      <!-- 新书榜 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
            <el-icon class="text-purple-500" :size="18"><Promotion /></el-icon>
            新书榜
          </h3>
          <span class="text-xs text-gray-400">TOP 10</span>
        </div>
        
        <div v-if="newRanking.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>
        <div v-else class="space-y-2">
          <div 
            v-for="(novel, index) in newRanking" 
            :key="novel.id"
            class="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          >
            <span 
              class="w-7 h-7 flex items-center justify-center rounded-lg text-sm font-bold flex-shrink-0"
              :class="{
                'bg-yellow-400 text-white': index === 0,
                'bg-gray-300 text-white': index === 1,
                'bg-amber-600 text-white': index === 2,
                'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
              }"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1 min-w-0">
              <p class="font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</p>
              <p class="text-sm text-gray-500">{{ novel.authorName }}</p>
            </div>
            <span class="text-sm text-gray-400 flex-shrink-0">{{ novel.chapterCount }}章</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 平台统计 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 小说状态 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-5 flex items-center gap-2">
          <el-icon class="text-primary" :size="18"><DataAnalysis /></el-icon>
          小说状态分布
        </h3>
        <div class="grid grid-cols-2 gap-4">
          <div class="text-center p-4 rounded-xl bg-green-50 dark:bg-green-900/20">
            <p class="text-3xl font-bold text-green-600">{{ overview.finishedNovelCount }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">已完结</p>
          </div>
          <div class="text-center p-4 rounded-xl bg-blue-50 dark:bg-blue-900/20">
            <p class="text-3xl font-bold text-blue-600">{{ overview.novelCount - overview.finishedNovelCount }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">连载中</p>
          </div>
          <div class="text-center p-4 rounded-xl bg-red-50 dark:bg-red-900/20">
            <p class="text-3xl font-bold text-red-600">{{ overview.hotNovelCount }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">热门小说</p>
          </div>
          <div class="text-center p-4 rounded-xl bg-purple-50 dark:bg-purple-900/20">
            <p class="text-3xl font-bold text-purple-600">{{ overview.categoryCount }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">小说分类</p>
          </div>
        </div>
      </div>
      
      <!-- 快捷入口 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-5 flex items-center gap-2">
          <el-icon class="text-primary" :size="18"><Operation /></el-icon>
          快捷入口
        </h3>
        <div class="grid grid-cols-2 gap-3">
          <router-link 
            to="/manager/novels"
            class="flex items-center gap-3 p-4 rounded-xl bg-primary/5 hover:bg-primary/10 text-primary transition-colors"
          >
            <el-icon :size="24"><Document /></el-icon>
            <span class="font-medium">小说管理</span>
          </router-link>
          <router-link 
            to="/manager/authors"
            class="flex items-center gap-3 p-4 rounded-xl bg-green-500/5 hover:bg-green-500/10 text-green-600 transition-colors"
          >
            <el-icon :size="24"><User /></el-icon>
            <span class="font-medium">作者管理</span>
          </router-link>
          <router-link 
            to="/manager/visitors"
            class="flex items-center gap-3 p-4 rounded-xl bg-yellow-500/5 hover:bg-yellow-500/10 text-yellow-600 transition-colors"
          >
            <el-icon :size="24"><Avatar /></el-icon>
            <span class="font-medium">访客管理</span>
          </router-link>
          <router-link 
            to="/manager/categories"
            class="flex items-center gap-3 p-4 rounded-xl bg-red-500/5 hover:bg-red-500/10 text-red-600 transition-colors"
          >
            <el-icon :size="24"><List /></el-icon>
            <span class="font-medium">分类管理</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { 
  Reading, User, Avatar, Folder, Refresh, Calendar, 
  TrendCharts, Medal, DataAnalysis, Operation, Document, List,
  StarFilled, Clock, Promotion
} from '@element-plus/icons-vue'
import StatisticsCard from '@/components/business/statistics-card.vue'
import { getDashboardOverview, getNovelOngoingRanking, getAuthorProductiveRanking, getNovelCollectRanking, getNovelLatestRanking, getNovelNewRanking } from '@/api/manager'
import type { DashboardOverviewVO, NovelRankingItem, AuthorRankingItem } from '@/types'

const overview = reactive<DashboardOverviewVO>({
  novelCount: 0,
  authorCount: 0,
  visitorCount: 0,
  categoryCount: 0,
  todayNewNovels: 0,
  todayNewAuthors: 0,
  todayNewVisitors: 0,
  weekNewNovels: 0,
  weekNewAuthors: 0,
  weekNewVisitors: 0,
  monthNewNovels: 0,
  monthNewAuthors: 0,
  monthNewVisitors: 0,
  hotNovelCount: 0,
  finishedNovelCount: 0
})

const period = ref<'today' | 'week' | 'month'>('today')

const periodOptions = [
  { label: '今日', value: 'today' as const },
  { label: '本周', value: 'week' as const },
  { label: '本月', value: 'month' as const },
]

const currentPeriodData = computed(() => {
  if (period.value === 'week') {
    return {
      newNovels: overview.weekNewNovels,
      newAuthors: overview.weekNewAuthors,
      newVisitors: overview.weekNewVisitors,
    }
  }
  if (period.value === 'month') {
    return {
      newNovels: overview.monthNewNovels,
      newAuthors: overview.monthNewAuthors,
      newVisitors: overview.monthNewVisitors,
    }
  }
  return {
    newNovels: overview.todayNewNovels,
    newAuthors: overview.todayNewAuthors,
    newVisitors: overview.todayNewVisitors,
  }
})

const ongoingRanking = ref<NovelRankingItem[]>([])
const authorRanking = ref<AuthorRankingItem[]>([])
const collectRanking = ref<NovelRankingItem[]>([])
const latestRanking = ref<NovelRankingItem[]>([])
const newRanking = ref<NovelRankingItem[]>([])

const fetchData = async () => {
  try {
    // 概览数据单独获取，排行榜用 allSettled 避免单点故障
    const overviewRes = await getDashboardOverview()
    Object.assign(overview, overviewRes.data)

    const results = await Promise.allSettled([
      getNovelOngoingRanking(10),
      getAuthorProductiveRanking(10),
      getNovelCollectRanking(10),
      getNovelLatestRanking(10),
      getNovelNewRanking(10)
    ])

    const [novelRes, authorRes, collectRes, latestRes, newRes] = results
    if (novelRes.status === 'fulfilled') ongoingRanking.value = novelRes.value.data?.items || []
    if (authorRes.status === 'fulfilled') authorRanking.value = authorRes.value.data?.items || []
    if (collectRes.status === 'fulfilled') collectRanking.value = collectRes.value.data?.items || []
    if (latestRes.status === 'fulfilled') latestRanking.value = latestRes.value.data?.items || []
    if (newRes.status === 'fulfilled') newRanking.value = newRes.value.data?.items || []
  } catch (error) {
    console.error('Failed to fetch data:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>