<template>
  <div class="novel-detail-page">
    <!-- 模糊背景封面 -->
    <div class="relative">
      <!-- 背景图 -->
      <div class="absolute inset-0 overflow-hidden">
        <img 
          :src="getImageUrl(novel?.url)"
          class="w-full h-full object-cover scale-110 blur-xl opacity-30 dark:opacity-20"
        />
        <div class="absolute inset-0 bg-gradient-to-b from-transparent via-white/80 to-white dark:via-gray-900/80 dark:to-gray-900"></div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="relative container mx-auto px-4 py-20 flex justify-center">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      </div>
      
      <!-- 小说信息卡片 -->
      <div v-else-if="novel" class="relative container mx-auto px-4 py-8">
        <div class="flex flex-col md:flex-row gap-8">
          <!-- 封面 -->
          <div class="flex-shrink-0 mx-auto md:mx-0">
            <div class="relative group">
              <img 
                :src="getImageUrl(novel.url)" 
                :alt="novel.name"
                class="w-48 h-64 md:w-56 md:h-72 object-cover rounded-2xl shadow-2xl transition-transform duration-300 group-hover:scale-105"
              />
              <!-- 状态角标 -->
              <div class="absolute top-3 right-3 flex flex-col gap-2">
                <span 
                  v-if="novel.isFinished"
                  class="px-2 py-1 bg-green-500 text-white text-xs font-medium rounded-lg shadow-lg"
                >
                  完结
                </span>
                <span 
                  v-if="novel.isHot"
                  class="px-2 py-1 bg-gradient-warm text-white text-xs font-medium rounded-lg shadow-lg"
                >
                  热门
                </span>
              </div>
            </div>
          </div>
          
          <!-- 信息 -->
          <div class="flex-1 text-center md:text-left">
            <h1 class="text-2xl md:text-3xl font-bold text-gray-800 dark:text-gray-200">
              {{ novel.name }}
            </h1>
            <p v-if="novel.subName" class="text-gray-500 dark:text-gray-400 mt-2 text-lg">
              {{ novel.subName }}
            </p>
            
            <!-- 作者信息 -->
            <div class="flex items-center justify-center md:justify-start gap-3 mt-4">
              <div class="w-8 h-8 rounded-full bg-gradient-primary flex items-center justify-center text-white text-sm">
                {{ novel.authorName?.charAt(0) }}
              </div>
              <span class="text-gray-700 dark:text-gray-300 font-medium">{{ novel.authorName }}</span>
            </div>
            
            <!-- 数据统计 -->
            <div class="flex items-center justify-center md:justify-start gap-6 mt-4 text-sm text-gray-500 dark:text-gray-400">
              <span class="flex items-center gap-1">
                <el-icon><Document /></el-icon>
                {{ novel.chapterCount }} 章
              </span>
              <span class="flex items-center gap-1">
                <el-icon><Clock /></el-icon>
                {{ formatTime(novel.updateTime) }}更新
              </span>
            </div>
            
            <!-- 分类标签 -->
            <div class="flex flex-wrap justify-center md:justify-start gap-2 mt-4">
              <el-tag 
                v-for="cat in novel.categories" 
                :key="cat.id"
                class="rounded-full"
                effect="plain"
              >
                {{ cat.type }}
              </el-tag>
              <template v-if="novel.tags">
                <el-tag 
                  v-for="tag in novel.tags.split(',')" 
                  :key="tag"
                  type="info"
                  class="rounded-full"
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </template>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex flex-wrap justify-center md:justify-start gap-3 mt-6">
              <el-button 
                type="primary" 
                size="large"
                class="rounded-xl px-8"
                @click="startReading"
              >
                <el-icon class="mr-1"><Reading /></el-icon>
                开始阅读
              </el-button>
              <el-button 
                size="large"
                class="rounded-xl"
                @click="addToShelf"
              >
                <el-icon class="mr-1"><FolderAdd /></el-icon>
                加入书架
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div v-if="novel" class="container mx-auto px-4 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 左侧主内容 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 简介 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-1 h-5 bg-gradient-primary rounded-full"></div>
              <h3 class="text-lg font-bold text-gray-800 dark:text-gray-200">简介</h3>
            </div>
            <p 
              class="text-gray-600 dark:text-gray-300 leading-relaxed whitespace-pre-wrap"
              :class="{ 'line-clamp-4': !showFullIntro }"
            >
              {{ novel.introduction || '暂无简介' }}
            </p>
            <button 
              v-if="novel.introduction && novel.introduction.length > 100"
              class="mt-3 text-primary text-sm hover:text-primary-dark transition-colors"
              @click="showFullIntro = !showFullIntro"
            >
              {{ showFullIntro ? '收起' : '展开全部' }}
            </button>
          </section>

          <!-- 章节目录 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-2">
                <div class="w-1 h-5 bg-gradient-cool rounded-full"></div>
                <h3 class="text-lg font-bold text-gray-800 dark:text-gray-200">章节目录</h3>
                <span class="text-sm text-gray-400">共{{ chapters.length }}章</span>
              </div>
              <el-input 
                v-model="chapterSearch"
                placeholder="搜索章节"
                class="w-48"
                size="small"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            
            <div v-if="filteredChapters.length === 0" class="text-center py-8 text-gray-400">
              暂无章节
            </div>
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
              <button
                v-for="chapter in filteredChapters"
                :key="chapter.id"
                class="chapter-item p-3 text-left rounded-xl transition-all duration-200"
                :class="currentChapterId === chapter.id 
                  ? 'bg-primary/10 text-primary font-medium' 
                  : 'bg-gray-50 dark:bg-gray-700/50 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
                @click="handleChapterSelect(chapter)"
              >
                <span class="text-sm truncate block">{{ chapter.title }}</span>
              </button>
            </div>
            
            <!-- 章节分页 -->
            <div v-if="chapters.length > chapterPageSize" class="mt-4 flex justify-center">
              <el-pagination
                v-model:current-page="chapterPage"
                :page-size="chapterPageSize"
                :total="chapters.length"
                layout="prev, pager, next"
                small
              />
            </div>
          </section>

          <!-- 评论区 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
            <CommentList 
              v-if="novel"
              :target-type="CommentTargetType.NOVEL"
              :target-id="novel.id"
              :novel-id="novel.id"
            />
          </section>
        </div>

        <!-- 右侧边栏 -->
        <div class="space-y-6">
          <!-- 作者信息卡 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-5">
            <div class="flex items-center gap-4">
              <div class="w-14 h-14 rounded-full bg-gradient-primary flex items-center justify-center text-white text-xl font-bold">
                {{ novel.authorName?.charAt(0) }}
              </div>
              <div>
                <p class="font-bold text-gray-800 dark:text-gray-200">{{ novel.authorName }}</p>
                <p class="text-sm text-gray-500">作者</p>
              </div>
            </div>
            <div class="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
              <p class="text-sm text-gray-500">
                <span class="text-primary font-medium">{{ novel.chapterCount }}</span> 篇作品
              </p>
            </div>
          </section>

          <!-- 同类推荐 -->
          <section class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-5">
            <div class="flex items-center gap-2 mb-4">
              <el-icon class="text-primary" :size="18"><StarFilled /></el-icon>
              <h3 class="font-bold text-gray-800 dark:text-gray-200">同类推荐</h3>
            </div>
            <div v-if="recommendNovels.length === 0" class="text-center py-4 text-gray-400 text-sm">
              暂无推荐
            </div>
            <div v-else class="space-y-3">
              <div 
                v-for="item in recommendNovels.slice(0, 5)" 
                :key="item.id"
                class="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
                @click="handleNovelClick(item)"
              >
                <img 
                  :src="getImageUrl(item.url)" 
                  class="w-10 h-14 object-cover rounded-lg"
                />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-700 dark:text-gray-300 truncate">{{ item.name }}</p>
                  <p class="text-xs text-gray-400">{{ item.chapterCount }}章</p>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="container mx-auto px-4 py-20 text-center text-gray-400">
      <el-icon :size="60" class="mb-4"><Warning /></el-icon>
      <p>小说不存在或已被删除</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  Loading, Reading, Document, Clock, FolderAdd, Search, 
  StarFilled, Warning 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNovelDetail, getChapterList, getHotNovels } from '@/api'
import { useNovelStore } from '@/stores'
import { getImageUrl } from '@/utils/file-url'
import { formatRelativeTime } from '@/utils/format'
import CommentList from '@/components/business/CommentList.vue'
import { CommentTargetType } from '@/types/comment'
import type { NovelDetailVO, NovelChapterVO, NovelListVO } from '@/types'

const router = useRouter()
const route = useRoute()
const novelStore = useNovelStore()

const loading = ref(true)
const novel = ref<NovelDetailVO | null>(null)
const chapters = ref<NovelChapterVO[]>([])
const recommendNovels = ref<NovelListVO[]>([])
const showFullIntro = ref(false)
const chapterSearch = ref('')
const chapterPage = ref(1)
const chapterPageSize = 24

const currentChapterId = computed(() => novelStore.currentChapter?.id)

const formatTime = formatRelativeTime

// 过滤章节
const filteredChapters = computed(() => {
  let result = chapters.value
  if (chapterSearch.value) {
    result = result.filter(c => 
      c.title.toLowerCase().includes(chapterSearch.value.toLowerCase())
    )
  }
  const start = (chapterPage.value - 1) * chapterPageSize
  return result.slice(start, start + chapterPageSize)
})

const fetchData = async () => {
  const novelId = Number(route.params.id)
  if (!novelId) return
  
  loading.value = true
  try {
    const [novelRes, chapterRes, hotRes] = await Promise.all([
      getNovelDetail(novelId),
      getChapterList(novelId),
      getHotNovels({ pageNum: 1, pageSize: 6 })
    ])
    
    novel.value = novelRes.data
    chapters.value = chapterRes.data || []
    recommendNovels.value = hotRes.data?.list || []
    
    if (novel.value) {
      novelStore.setCurrentNovel(novel.value)
    }
  } catch (error) {
    console.error('Failed to fetch novel:', error)
  } finally {
    loading.value = false
  }
}

const startReading = () => {
  if (chapters.value.length > 0) {
    const firstChapter = chapters.value[0]
    router.push(`/read/${novel.value?.id}/${firstChapter.id}`)
  } else {
    ElMessage.warning('暂无章节')
  }
}

const addToShelf = () => {
  ElMessage.success('已加入书架')
}

const handleChapterSelect = (chapter: NovelChapterVO) => {
  router.push(`/read/${novel.value?.id}/${chapter.id}`)
}

const handleNovelClick = (item: NovelListVO) => {
  router.push(`/novel/${item.id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.line-clamp-4 {
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>