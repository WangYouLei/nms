<template>
  <div class="chapter-list">
    <!-- 排序控制 -->
    <div class="flex items-center justify-between mb-4">
      <span class="text-gray-600 dark:text-gray-300">共 {{ chapters.length }} 章</span>
      <el-button size="small" @click="reverse = !reverse">
        <el-icon><Sort /></el-icon>
        {{ reverse ? '正序' : '倒序' }}
      </el-button>
    </div>

    <!-- 章节列表 -->
    <div class="max-h-[600px] overflow-y-auto">
      <div 
        v-for="chapter in displayChapters" 
        :key="chapter.id"
        class="chapter-item p-3 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
        :class="{ 'bg-primary-light-9 dark:bg-gray-700': chapter.id === currentChapterId }"
        @click="handleSelect(chapter)"
      >
        <div class="flex items-center justify-between">
          <span class="text-gray-700 dark:text-gray-200">{{ chapter.title }}</span>
          <span class="text-xs text-gray-400">{{ formatTime(chapter.updateTime) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Sort } from '@element-plus/icons-vue'
import type { NovelChapterVO } from '@/types'
import { formatDate } from '@/utils/format'

interface Props {
  chapters: NovelChapterVO[]
  currentChapterId?: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'select', chapter: NovelChapterVO): void
}>()

const reverse = ref(false)

const displayChapters = computed(() => {
  const list = [...props.chapters]
  return reverse.value ? list.reverse() : list
})

const handleSelect = (chapter: NovelChapterVO) => {
  emit('select', chapter)
}

const formatTime = (time: string) => {
  return formatDate(time, 'MM-DD')
}
</script>