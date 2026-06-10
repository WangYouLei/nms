<template>
  <div class="home-page">
    <!-- Hero区域 -->
    <section class="hero-section relative overflow-hidden">
      <!-- 背景渐变 -->
      <div class="absolute inset-0 bg-gradient-primary"></div>
      
      <!-- 装饰元素 -->
      <div class="absolute inset-0 overflow-hidden">
        <div class="absolute -top-1/2 -right-1/4 w-96 h-96 bg-white/10 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-1/2 -left-1/4 w-96 h-96 bg-white/10 rounded-full blur-3xl"></div>
      </div>
      
      <!-- 内容 -->
      <div class="relative container mx-auto px-4 py-16 md:py-24">
        <div class="max-w-2xl mx-auto text-center">
          <h1 class="text-3xl md:text-5xl font-bold text-white mb-4 animate-slide-down">
            发现精彩小说
          </h1>
          <p class="text-lg md:text-xl text-white/80 mb-8 animate-slide-up">
            海量精品小说，等你来读
          </p>
          
          <!-- 搜索框 -->
          <div class="relative max-w-xl mx-auto animate-scale-in">
            <input 
              v-model="searchKeyword"
              type="text"
              placeholder="搜索小说、作者..."
              class="w-full px-6 py-4 pr-14 rounded-2xl bg-white/90 dark:bg-gray-800/90 backdrop-blur-md text-gray-800 dark:text-gray-200 placeholder-gray-400 shadow-xl focus:outline-none focus:ring-4 focus:ring-white/20 transition-all"
              @keyup.enter="handleSearch"
            />
            <button 
              class="absolute right-2 top-1/2 -translate-y-1/2 w-10 h-10 rounded-xl bg-gradient-primary text-white flex items-center justify-center hover:shadow-lg transition-shadow"
              @click="handleSearch"
            >
              <el-icon :size="20"><Search /></el-icon>
            </button>
          </div>
          
          <!-- 热门标签 -->
          <div class="flex flex-wrap justify-center gap-2 mt-6">
            <span class="text-white/60 text-sm">热门搜索：</span>
            <button 
              v-for="tag in hotTags" 
              :key="tag"
              class="px-3 py-1 rounded-full bg-white/20 text-white text-sm hover:bg-white/30 transition-colors"
              @click="searchKeyword = tag; handleSearch()"
            >
              {{ tag }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 主内容区域 -->
    <div class="container mx-auto px-4 py-8">
      <!-- 分类导航 -->
      <section class="mb-8">
        <CategoryNav 
          :categories="categories"
          :active-category="activeCategory"
          @change="handleCategoryChange"
          @channel-change="handleChannelChange"
        />
      </section>

      <!-- 内容网格 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 左侧主内容 -->
        <div class="lg:col-span-2 space-y-8">
          <!-- 热门推荐 -->
          <section>
            <div class="flex items-center justify-between mb-5">
              <div class="flex items-center gap-3">
                <div class="w-1 h-6 bg-gradient-primary rounded-full"></div>
                <h2 class="text-xl font-bold text-gray-800 dark:text-gray-200">热门推荐</h2>
              </div>
              <router-link 
                to="/category" 
                class="flex items-center gap-1 text-primary hover:text-primary-dark transition-colors"
              >
                <span>查看更多</span>
                <el-icon><ArrowRight /></el-icon>
              </router-link>
            </div>
            
            <div v-if="loading" class="flex justify-center py-16">
              <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            </div>
            <div v-else-if="hotNovels.length === 0" class="text-center py-16 text-gray-400">
              暂无数据
            </div>
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
              <NovelCard 
                v-for="novel in hotNovels" 
                :key="novel.id"
                :novel="novel"
                :show-new-tag="true"
                @click="handleNovelClick"
              />
            </div>
          </section>

          <!-- 最新更新 -->
          <section>
            <div class="flex items-center justify-between mb-5">
              <div class="flex items-center gap-3">
                <div class="w-1 h-6 bg-gradient-cool rounded-full"></div>
                <h2 class="text-xl font-bold text-gray-800 dark:text-gray-200">最新更新</h2>
              </div>
            </div>
            
            <div v-if="latestNovels.length === 0" class="text-center py-12 text-gray-400">
              暂无数据
            </div>
            <div v-else class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
              <div 
                v-for="(novel, index) in latestNovels" 
                :key="novel.id"
                class="flex items-center p-4 gap-4 border-b border-gray-100 dark:border-gray-700 last:border-b-0 hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
                @click="handleNovelClick(novel)"
              >
                <!-- 序号 -->
                <span 
                  class="w-6 h-6 rounded-lg flex items-center justify-center text-sm font-bold"
                  :class="index < 3 ? 'bg-gradient-warm text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-500'"
                >
                  {{ index + 1 }}
                </span>
                
                <!-- 封面 -->
                <img 
                  :src="getImageUrl(novel.url)" 
                  class="w-12 h-16 object-cover rounded-lg shadow-sm"
                />
                
                <!-- 信息 -->
                <div class="flex-1 min-w-0">
                  <h3 class="font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</h3>
                  <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ novel.authorName }}</p>
                  <div class="flex items-center gap-2 mt-1 text-xs text-gray-400">
                    <span>{{ novel.chapterCount }}章</span>
                    <span v-if="novel.isFinished" class="text-green-500">完结</span>
                    <span v-if="novel.isHot" class="text-red-500">热门</span>
                  </div>
                </div>
                
                <!-- 更新时间 -->
                <span class="text-xs text-gray-400 flex-shrink-0">{{ formatTime(novel.updateTime) }}</span>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧边栏 -->
        <div class="space-y-8">
          <!-- 人气排行榜 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-5">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-2">
                <el-icon class="text-orange-500" :size="20"><Trophy /></el-icon>
                <h3 class="font-bold text-gray-800 dark:text-gray-200">人气榜</h3>
              </div>
              <router-link to="/rank" class="text-sm text-primary">更多</router-link>
            </div>

            <div v-if="collectRanking.length > 0" class="space-y-3">
              <div
                v-for="(item, index) in collectRanking"
                :key="item.id"
                class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
                @click="router.push(`/novel/${item.id}`)"
              >
                <span
                  class="w-5 h-5 rounded flex items-center justify-center text-xs font-bold flex-shrink-0"
                  :class="{
                    'bg-yellow-400 text-white': index === 0,
                    'bg-gray-300 text-white': index === 1,
                    'bg-amber-600 text-white': index === 2,
                    'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
                  }"
                >
                  {{ item.rank }}
                </span>
                <span class="flex-1 text-sm text-gray-700 dark:text-gray-300 truncate">{{ item.name }}</span>
                <span class="text-xs text-gray-400 flex-shrink-0">{{ item.authorName }}</span>
              </div>
            </div>
          </section>

          <!-- 连载榜 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-5">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-2">
                <el-icon class="text-blue-500" :size="20"><Trophy /></el-icon>
                <h3 class="font-bold text-gray-800 dark:text-gray-200">连载榜</h3>
              </div>
              <router-link to="/rank" class="text-sm text-primary">更多</router-link>
            </div>

            <div v-if="ongoingRanking.length > 0" class="space-y-3">
              <div
                v-for="(item, index) in ongoingRanking"
                :key="item.id"
                class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
                @click="router.push(`/novel/${item.id}`)"
              >
                <span
                  class="w-5 h-5 rounded flex items-center justify-center text-xs font-bold flex-shrink-0"
                  :class="{
                    'bg-yellow-400 text-white': index === 0,
                    'bg-gray-300 text-white': index === 1,
                    'bg-amber-600 text-white': index === 2,
                    'bg-gray-100 dark:bg-gray-700 text-gray-500': index > 2
                  }"
                >
                  {{ item.rank }}
                </span>
                <span class="flex-1 text-sm text-gray-700 dark:text-gray-300 truncate">{{ item.name }}</span>
                <span class="text-xs text-gray-400 flex-shrink-0">{{ item.authorName }}</span>
              </div>
            </div>
          </section>

          <!-- 完结精选 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-5">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-2">
                <el-icon class="text-green-500" :size="20"><CircleCheck /></el-icon>
                <h3 class="font-bold text-gray-800 dark:text-gray-200">完结精选</h3>
              </div>
            </div>
            
            <div v-if="finishedNovels.length > 0" class="space-y-3">
              <div 
                v-for="novel in finishedNovels.slice(0, 5)" 
                :key="novel.id"
                class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
                @click="handleNovelClick(novel)"
              >
                <img 
                  :src="getImageUrl(novel.url)" 
                  class="w-10 h-14 object-cover rounded-lg"
                />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-700 dark:text-gray-300 truncate">{{ novel.name }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ novel.chapterCount }}章 · 完结</p>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-8 text-gray-400 text-sm">
              暂无完结小说
            </div>
          </section>

          <!-- 作者入驻 -->
          <section class="bg-gradient-purple rounded-2xl p-5 text-center">
            <el-icon class="text-primary mb-2" :size="32"><Edit /></el-icon>
            <h3 class="font-bold text-gray-800 mb-2">成为作者</h3>
            <p class="text-sm text-gray-600 mb-4">创作属于你的故事</p>
            <router-link 
              to="/author/register"
              class="inline-block px-6 py-2 bg-primary text-white rounded-full text-sm font-medium hover:bg-primary-dark transition-colors"
            >
              立即入驻
            </router-link>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight, Loading, Trophy, CircleCheck, Edit } from '@element-plus/icons-vue'
import { getHotNovels, getAllCategories, searchNovels } from '@/api'
import { getNovelCollectRanking, getNovelOngoingRanking } from '@/api/ranking'
import NovelCard from '@/components/business/novel-card.vue'
import CategoryNav from '@/components/business/category-nav.vue'
import type { NovelListVO, NovelCategoryVO, NovelRankingItem } from '@/types'
import { formatRelativeTime } from '@/utils/format'
import { getImageUrl } from '@/utils/file-url'
import { Channel } from '@/enums'

const router = useRouter()

const loading = ref(true)
const hotNovels = ref<NovelListVO[]>([])
const latestNovels = ref<NovelListVO[]>([])
const categories = ref<NovelCategoryVO[]>([])
const collectRanking = ref<NovelRankingItem[]>([])
const ongoingRanking = ref<NovelRankingItem[]>([])
const activeCategory = ref(0)
const currentChannel = ref<number>(Channel.MALE)
const searchKeyword = ref('')

const hotTags = ['玄幻', '都市', '言情', '修仙', '穿越']

const formatTime = formatRelativeTime

// 完结小说
const finishedNovels = computed(() => {
  return hotNovels.value.filter(n => n.isFinished)
})

const fetchData = async () => {
  loading.value = true
  try {
    // 核心数据和排行榜独立请求，避免排行榜失败影响主内容
    const [hotRes, latestRes, catRes] = await Promise.all([
      getHotNovels({ pageNum: 1, pageSize: 12 }),
      searchNovels({ pageNum: 1, pageSize: 10 }),
      getAllCategories()
    ])

    hotNovels.value = hotRes.data?.list || []
    latestNovels.value = latestRes.data?.list || []
    categories.value = catRes.data || []

    // 排行榜数据独立获取，失败不影响主内容
    Promise.allSettled([
      getNovelCollectRanking(5),
      getNovelOngoingRanking(5)
    ]).then(([collectRes, ongoingRes]) => {
      if (collectRes.status === 'fulfilled') collectRanking.value = collectRes.value.data?.items || []
      if (ongoingRes.status === 'fulfilled') ongoingRanking.value = ongoingRes.value.data?.items || []
    })
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = (categoryId: number) => {
  activeCategory.value = categoryId
  if (categoryId) {
    router.push(`/category/${categoryId}`)
  }
}

const handleChannelChange = (channel: number) => {
  currentChannel.value = channel
}

const handleNovelClick = (novel: NovelListVO) => {
  router.push(`/novel/${novel.id}`)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.hero-section {
  min-height: 320px;
}

@media (min-width: 768px) {
  .hero-section {
    min-height: 400px;
  }
}
</style>