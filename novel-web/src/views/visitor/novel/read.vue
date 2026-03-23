<template>
  <div class="read-page h-screen flex flex-col">
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="text-center">
        <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
        <p class="text-gray-400 mt-4">加载中...</p>
      </div>
    </div>
    
    <Reader
      v-else
      :content="chapterContent"
      :title="chapter?.title || ''"
      :has-prev="hasPrev"
      :has-next="hasNext"
      @prev="goPrev"
      @next="goNext"
      @catalog="showCatalog = true"
      @back="goBack"
    />
    
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

      <!-- 评论入口 -->
      <div class="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
        <el-button 
          type="primary" 
          class="w-full"
          @click="showComments = true"
        >
          <el-icon class="mr-1"><ChatDotRound /></el-icon>
          查看本章评论
        </el-button>
      </div>
    </el-drawer>

    <!-- 评论抽屉 -->
    <el-drawer
      v-model="showComments"
      title="章节评论"
      direction="rtl"
      size="400px"
      :with-header="true"
      class="comment-drawer"
    >
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon class="text-primary"><ChatDotRound /></el-icon>
          <span class="font-bold text-lg">{{ chapter?.title }} - 评论</span>
        </div>
      </template>
      
      <CommentList 
        v-if="chapter"
        :target-type="CommentTargetType.CHAPTER"
        :target-id="chapter.id"
        :novel-id="Number(route.params.novelId)"
      />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading, Search, Check, ChatDotRound } from '@element-plus/icons-vue'
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
const showComments = ref(false)
const chapterSearch = ref('')

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

.comment-drawer .el-drawer__header {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-drawer .el-drawer__body {
  padding: 16px;
}
</style>