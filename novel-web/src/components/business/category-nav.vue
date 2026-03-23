<template>
  <div class="category-nav">
    <!-- 频道切换 -->
    <div v-if="showChannel" class="flex items-center gap-2 mb-5">
      <button 
        v-for="item in channelOptions" 
        :key="item.value"
        class="relative px-5 py-2 rounded-full text-sm font-medium transition-all duration-300"
        :class="currentChannel === item.value 
          ? 'text-white shadow-lg' 
          : 'text-gray-600 dark:text-gray-300 hover:text-primary dark:hover:text-primary'"
        @click="handleChannelChange(item.value)"
      >
        <!-- 选中状态的渐变背景 -->
        <span 
          v-if="currentChannel === item.value" 
          class="absolute inset-0 rounded-full bg-gradient-primary"
        ></span>
        <span class="relative z-10">{{ item.label }}</span>
      </button>
      
      <!-- 分隔线 -->
      <div class="w-px h-6 bg-gray-200 dark:bg-gray-700 mx-2"></div>
      
      <!-- 排序选项 -->
      <div v-if="showSort" class="flex items-center gap-2">
        <button 
          v-for="sort in sortOptions" 
          :key="sort.value"
          class="px-3 py-1.5 rounded-lg text-sm transition-colors"
          :class="currentSort === sort.value 
            ? 'bg-primary/10 text-primary font-medium' 
            : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'"
          @click="handleSortChange(sort.value)"
        >
          {{ sort.label }}
        </button>
      </div>
    </div>

    <!-- 分类列表 - 横向滚动 -->
    <div class="relative">
      <!-- 左侧渐变遮罩 -->
      <div 
        v-if="showLeftArrow" 
        class="absolute left-0 top-0 bottom-0 w-8 bg-gradient-to-r from-white dark:from-gray-900 to-transparent z-10 pointer-events-none"
      ></div>
      
      <!-- 分类标签容器 -->
      <div 
        ref="scrollContainer"
        class="flex gap-2 overflow-x-auto scrollbar-hide py-1"
        @scroll="handleScroll"
      >
        <!-- 全部按钮 -->
        <button 
          class="category-item flex-shrink-0 px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200"
          :class="!activeCategory 
            ? 'bg-gradient-primary text-white shadow-md' 
            : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'"
          @click="handleCategoryChange(0)"
        >
          全部
        </button>
        
        <!-- 分类按钮 -->
        <button 
          v-for="cat in filteredCategories" 
          :key="cat.id"
          class="category-item flex-shrink-0 px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200"
          :class="activeCategory === cat.id 
            ? 'bg-gradient-primary text-white shadow-md' 
            : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'"
          @click="handleCategoryChange(cat.id)"
        >
          {{ cat.type }}
        </button>
      </div>
      
      <!-- 右侧渐变遮罩 -->
      <div 
        v-if="showRightArrow" 
        class="absolute right-0 top-0 bottom-0 w-8 bg-gradient-to-l from-white dark:from-gray-900 to-transparent z-10 pointer-events-none"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Channel, ChannelLabels } from '@/enums'
import type { NovelCategoryVO } from '@/types'

interface Props {
  categories: NovelCategoryVO[]
  activeCategory?: number
  showChannel?: boolean
  showSort?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  activeCategory: 0,
  showChannel: true,
  showSort: false
})

const emit = defineEmits<{
  (e: 'change', categoryId: number): void
  (e: 'channelChange', channel: number): void
  (e: 'sortChange', sort: string): void
}>()

const currentChannel = ref<number>(Channel.MALE)
const currentSort = ref<string>('update')
const scrollContainer = ref<HTMLElement | null>(null)
const showLeftArrow = ref(false)
const showRightArrow = ref(false)

const channelOptions = [
  { value: Channel.MALE, label: ChannelLabels[Channel.MALE] },
  { value: Channel.FEMALE, label: ChannelLabels[Channel.FEMALE] }
]

const sortOptions = [
  { value: 'update', label: '最新更新' },
  { value: 'hot', label: '最热' },
  { value: 'collect', label: '收藏最多' }
]

const filteredCategories = computed(() => {
  return props.categories.filter(cat => cat.category === currentChannel.value)
})

const handleChannelChange = (channel: number) => {
  currentChannel.value = channel
  emit('channelChange', channel)
}

const handleCategoryChange = (categoryId: number) => {
  emit('change', categoryId)
}

const handleSortChange = (sort: string) => {
  currentSort.value = sort
  emit('sortChange', sort)
}

const handleScroll = () => {
  if (!scrollContainer.value) return
  const { scrollLeft, scrollWidth, clientWidth } = scrollContainer.value
  showLeftArrow.value = scrollLeft > 10
  showRightArrow.value = scrollLeft < scrollWidth - clientWidth - 10
}

onMounted(() => {
  handleScroll()
})
</script>

<style scoped>
/* 隐藏滚动条但保持滚动功能 */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* 分类标签动画 */
.category-item {
  transform: translateY(0);
}

.category-item:active {
  transform: scale(0.95);
}
</style>