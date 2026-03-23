<template>
  <div 
    class="novel-card group relative bg-white dark:bg-gray-800 rounded-2xl overflow-hidden shadow-card hover:shadow-card-hover transition-all duration-300 cursor-pointer"
    :class="{ 'hover:-translate-y-1': !disableHover }"
    @click="handleClick"
  >
    <!-- 封面 -->
    <div class="relative aspect-[3/4] overflow-hidden">
      <img 
        :src="coverUrl" 
        :alt="novel.name"
        class="w-full h-full object-cover transition-transform duration-500"
        :class="{ 'group-hover:scale-110': !disableHover }"
        @error="handleImageError"
      />
      
      <!-- 渐变遮罩 -->
      <div 
        class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
      >
        <!-- 分类标签 -->
        <div v-if="novel.categoryName" class="absolute bottom-2 left-2">
          <span class="px-2 py-1 bg-white/20 backdrop-blur-sm rounded text-white text-xs">
            {{ novel.categoryName }}
          </span>
        </div>
      </div>
      
      <!-- 状态标签 -->
      <div class="absolute top-2 left-2 flex flex-wrap gap-1">
        <span 
          v-if="novel.isFinished" 
          class="px-2 py-0.5 text-xs font-medium bg-green-500 text-white rounded-full shadow-sm backdrop-blur-sm"
        >
          完结
        </span>
        <span 
          v-if="novel.isHot" 
          class="px-2 py-0.5 text-xs font-medium bg-gradient-warm text-white rounded-full shadow-sm"
        >
          热门
        </span>
        <span 
          v-if="showNewTag && isNewNovel" 
          class="px-2 py-0.5 text-xs font-medium bg-primary text-white rounded-full shadow-sm"
        >
          新书
        </span>
      </div>

      <!-- 章节数角标 -->
      <div 
        v-if="showChapterCount && novel.chapterCount" 
        class="absolute bottom-2 right-2 px-2 py-1 bg-black/50 backdrop-blur-sm rounded-lg text-white text-xs opacity-0 group-hover:opacity-100 transition-opacity duration-300"
      >
        {{ novel.chapterCount }}章
      </div>
    </div>
    
    <!-- 信息 -->
    <div class="p-3">
      <!-- 书名 -->
      <h3 
        class="text-sm font-medium text-gray-800 dark:text-gray-200 truncate group-hover:text-primary transition-colors duration-200"
        :title="novel.name"
      >
        {{ novel.name }}
      </h3>
      
      <!-- 作者 -->
      <p 
        v-if="showAuthor" 
        class="text-xs text-gray-500 dark:text-gray-400 mt-1 truncate"
      >
        {{ novel.authorName || '佚名' }}
      </p>
      
      <!-- 底部信息 -->
      <div class="flex items-center justify-between mt-2 text-xs text-gray-400">
        <span class="flex items-center gap-1">
          <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z"/>
          </svg>
          {{ novel.chapterCount || 0 }}章
        </span>
        <span 
          v-if="novel.categoryName" 
          class="px-1.5 py-0.5 bg-gray-100 dark:bg-gray-700 rounded text-gray-500 dark:text-gray-400"
        >
          {{ novel.categoryName }}
        </span>
      </div>
    </div>

    <!-- 悬浮时的边框发光效果 -->
    <div 
      class="absolute inset-0 rounded-2xl border-2 border-transparent group-hover:border-primary/20 transition-colors duration-300 pointer-events-none"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { watch, ref, computed } from 'vue'
import type { NovelListVO } from '@/types'
import { getPresignedFileUrl } from '@/utils/file-url'

interface Props {
  novel: NovelListVO
  showAuthor?: boolean
  showChapterCount?: boolean
  showNewTag?: boolean
  disableHover?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showAuthor: true,
  showChapterCount: true,
  showNewTag: false,
  disableHover: false
})

const emit = defineEmits<{
  (e: 'click', novel: NovelListVO): void
}>()

const handleClick = () => {
  emit('click', props.novel)
}

// 判断是否是新书（30天内更新）
const isNewNovel = computed(() => {
  if (!props.novel.updateTime) return false
  const updateTime = new Date(props.novel.updateTime).getTime()
  const now = Date.now()
  const thirtyDays = 30 * 24 * 60 * 60 * 1000
  return now - updateTime < thirtyDays
})

// 使用响应式图片URL
const coverUrl = ref('/default-cover.jpg')

const updateCoverUrl = async () => {
  if (!props.novel.url) {
    coverUrl.value = '/default-cover.jpg'
    return
  }
  
  try {
    coverUrl.value = await getPresignedFileUrl(props.novel.url) || '/default-cover.jpg'
  } catch (error) {
    console.error('Failed to get cover URL:', error)
    coverUrl.value = '/default-cover.jpg'
  }
}

watch(() => props.novel.url, updateCoverUrl, { immediate: true })

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = '/default-cover.jpg'
}
</script>

<style scoped>
.novel-card {
  will-change: transform, box-shadow;
}

/* 深色模式下的卡片阴影 */
.dark .novel-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.dark .novel-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}
</style>