<template>
  <div class="read-page h-screen flex flex-col">
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="text-center">
        <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
        <p class="text-gray-400 mt-4">加载中...</p>
      </div>
    </div>
    
    <template v-else>
      <!-- 阅读器区域 -->
      <div class="flex-1 overflow-hidden">
        <Reader
          :content="chapterContent"
          :title="chapter?.title || ''"
          :has-prev="hasPrev"
          :has-next="hasNext"
          @prev="goPrev"
          @next="goNext"
          @catalog="showCatalog = true"
          @back="goBack"
        />
      </div>

      <!-- 评论区域切换按钮 -->
      <div class="border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 px-4 py-2 flex items-center justify-between">
        <button
          class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-primary transition-colors"
          @click="showCommentSection = !showCommentSection"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ showCommentSection ? '收起评论' : '查看章节评论' }}</span>
          <el-icon :class="{ 'rotate-180': showCommentSection }"><ArrowDown /></el-icon>
        </button>
        <span v-if="!showCommentSection" class="text-xs text-gray-400">
          点击展开查看本章评论
        </span>
      </div>

      <!-- 评论区抽屉（从底部滑出） -->
      <Transition name="slide-up-drawer">
        <div 
          v-show="showCommentSection"
          class="comment-section bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700"
          :style="{ height: commentSectionHeight }"
        >
          <!-- 拖拽调整区域 -->
          <div 
            class="drag-handle h-8 flex items-center justify-center cursor-row-resize bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700"
            @mousedown="startResize"
          >
            <div class="w-12 h-1 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
          </div>
          
          <!-- 评论标题 -->
          <div class="px-4 py-3 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <el-icon class="text-primary"><ChatDotRound /></el-icon>
              <span class="font-medium text-gray-800 dark:text-gray-200">章节评论</span>
            </div>
            <button 
              class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors"
              @click="showCommentSection = false"
            >
              <el-icon><Close /></el-icon>
            </button>
          </div>

          <!-- 评论列表 -->
          <div class="overflow-y-auto px-4 py-4" style="height: calc(100% - 64px);">
            <CommentList 
              v-if="chapter"
              :target-type="CommentTargetType.CHAPTER"
              :target-id="chapter.id"
              :novel-id="Number(route.params.novelId)"
            />
          </div>
        </div>
      </Transition>
    </template>
    
    <!-- 章节目录抽屉 -->
    <el-drawer
      v-model="showCatalog"
      title="章节目录"
      direction="rtl"
      size="320px"
      :with-header="true"
      class="catalog-drawer"
    >
      <template #header>
        <div class="flex items-center justify-between w-full pr-4">
          <span class="font-bold text-lg">章节目录</span>
          <span class="text-sm text-gray-400">{{ chapters.length }}章</span>
        </div>
      </template>
      
      <!-- 搜索章节 -->
      <div class="mb-4">
        <el-input 
          v-model="chapterSearch"
          placeholder="搜索章节"
          clearable
          class="w-full"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <!-- 章节列表 -->
      <div class="space-y-1 max-h-[calc(100vh-200px)] overflow-y-auto">
        <button 
          v-for="chapter in filteredChapters" 
          :key="chapter.id"
          class="w-full px-4 py-3 rounded-xl text-left transition-colors"
          :class="currentChapterId === chapter.id 
            ? 'bg-primary/10 text-primary font-medium' 
            : 'hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300'"
          @click="handleChapterSelect(chapter)"
        >
          <div class="flex items-center justify-between">
            <span class="truncate">{{ chapter.title }}</span>
            <el-icon v-if="currentChapterId === chapter.id" class="text-primary"><Check /></el-icon>
          </div>
        </button>
      </div>
      
      <!-- 当前阅读位置 -->
      <div class="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
        <div class="flex items-center justify-between text-sm text-gray-500">
          <span>当前进度</span>
          <span>{{ currentIndex + 1 }} / {{ chapters.length }}</span>
        </div>
        <div class="mt-2 h-2 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
          <div 
            class="h-full bg-gradient-primary rounded-full transition-all duration-300"
            :style="{ width: `${((currentIndex + 1) / chapters.length) * 100}%` }"
          ></div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading, Search, Check, ChatDotRound, ArrowDown, Close } from '@element-plus/icons-vue'
import { getChapterList, getChapterContent, getChapterDetail } from '@/api'
import Reader from '@/components/business/reader.vue'
import CommentList from '@/components/business/CommentList.vue'
import { useNovelStore } from '@/stores'
import { CommentTargetType } from '@/types/comment'
import type { NovelChapterVO } from '@/types'

const router = useRouter()
const route = useRoute()
const novelStore = useNovelStore()

const loading = ref(true)
const chapters = ref<NovelChapterVO[]>([])
const chapter = ref<NovelChapterVO | null>(null)
const chapterContent = ref('')
const showCatalog = ref(false)
const chapterSearch = ref('')

// 评论区域状态
const showCommentSection = ref(false)
const commentSectionHeight = ref('40vh')

// 拖拽调整大小
const isResizing = ref(false)
const startResize = (e: MouseEvent) => {
  isResizing.value = true
  const startY = e.clientY
  const startHeight = parseInt(commentSectionHeight.value)
  
  const handleMove = (moveEvent: MouseEvent) => {
    if (!isResizing.value) return
    const diff = startY - moveEvent.clientY
    const newHeight = Math.min(Math.max(startHeight + diff, 200), window.innerHeight * 0.7)
    commentSectionHeight.value = `${newHeight}px`
  }
  
  const handleUp = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', handleMove)
    document.removeEventListener('mouseup', handleUp)
  }
  
  document.addEventListener('mousemove', handleMove)
  document.addEventListener('mouseup', handleUp)
}

const currentChapterId = computed(() => chapter.value?.id)

const currentIndex = computed(() => {
  return chapters.value.findIndex(c => c.id === chapter.value?.id)
})

const hasPrev = computed(() => currentIndex.value > 0)
const hasNext = computed(() => currentIndex.value < chapters.value.length - 1)

const filteredChapters = computed(() => {
  if (!chapterSearch.value) return chapters.value
  return chapters.value.filter(c => 
    c.title.toLowerCase().includes(chapterSearch.value.toLowerCase())
  )
})

const fetchChapters = async () => {
  const novelId = Number(route.params.novelId)
  try {
    const res = await getChapterList(novelId)
    chapters.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch chapters:', error)
  }
}

const fetchChapter = async () => {
  const chapterId = Number(route.params.chapterId)
  loading.value = true
  try {
    const [detailRes, contentRes] = await Promise.all([
      getChapterDetail(chapterId),
      getChapterContent(chapterId)
    ])
    
    chapter.value = detailRes.data
    chapterContent.value = contentRes.data?.content || ''
    
    if (chapter.value) {
      novelStore.setCurrentChapter(chapter.value)
    }
  } catch (error) {
    console.error('Failed to fetch chapter:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(`/novel/${route.params.novelId}`)
}

const goPrev = () => {
  if (hasPrev.value) {
    const prevChapter = chapters.value[currentIndex.value - 1]
    router.push(`/read/${route.params.novelId}/${prevChapter.id}`)
  }
}

const goNext = () => {
  if (hasNext.value) {
    const nextChapter = chapters.value[currentIndex.value + 1]
    router.push(`/read/${route.params.novelId}/${nextChapter.id}`)
  }
}

const handleChapterSelect = (selectedChapter: NovelChapterVO) => {
  showCatalog.value = false
  router.push(`/read/${route.params.novelId}/${selectedChapter.id}`)
}

watch(() => route.params.chapterId, () => {
  fetchChapter()
})

onMounted(async () => {
  await fetchChapters()
  fetchChapter()
})
</script>

<style>
/* 抽屉样式优化 */
.catalog-drawer .el-drawer__header {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.catalog-drawer .el-drawer__body {
  padding: 20px;
}

/* 评论区域过渡动画 */
.slide-up-drawer-enter-active,
.slide-up-drawer-leave-active {
  transition: all 0.3s ease;
}

.slide-up-drawer-enter-from,
.slide-up-drawer-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

/* 评论区域样式 */
.comment-section {
  position: relative;
  min-height: 200px;
  max-height: 70vh;
}

.drag-handle:active {
  cursor: row-resize;
}
</style>