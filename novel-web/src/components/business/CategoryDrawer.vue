<template>
  <el-drawer
    v-model="visible"
    title="选择分类"
    size="400px"
    :destroy-on-close="true"
  >
    <!-- 频道切换 -->
    <div class="flex items-center gap-2 mb-6">
      <button 
        v-for="item in channelOptions" 
        :key="item.value"
        class="relative px-5 py-2 rounded-full text-sm font-medium transition-all duration-300"
        :class="currentChannel === item.value 
          ? 'text-white shadow-lg' 
          : 'text-gray-600 dark:text-gray-300 hover:text-primary dark:hover:text-primary'"
        @click="handleChannelChange(item.value)"
      >
        <span 
          v-if="currentChannel === item.value" 
          class="absolute inset-0 rounded-full bg-gradient-primary"
        ></span>
        <span class="relative z-10">{{ item.label }}</span>
      </button>
    </div>

    <!-- 分类列表 -->
    <div class="space-y-2">
      <div 
        v-for="category in filteredCategories" 
        :key="category.id"
        class="flex items-center justify-between p-3 rounded-lg cursor-pointer transition-all duration-200"
        :class="selectedIds.includes(category.id) 
          ? 'bg-primary/10 border border-primary' 
          : 'bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700'"
        @click="toggleCategory(category.id)"
      >
        <span 
          class="text-sm font-medium"
          :class="selectedIds.includes(category.id) ? 'text-primary' : 'text-gray-700 dark:text-gray-300'"
        >
          {{ category.type }}
        </span>
        <el-icon v-if="selectedIds.includes(category.id)" class="text-primary">
          <Check />
        </el-icon>
      </div>
      
      <div v-if="filteredCategories.length === 0" class="text-center py-8 text-gray-400">
        暂无分类
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-500">
          已选择 {{ selectedIds.length }} 个分类
        </span>
        <div class="flex gap-2">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirm">确认</el-button>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { Channel, ChannelLabels } from '@/enums'
import { getAllCategories } from '@/api'
import type { NovelCategoryVO } from '@/types'

interface Props {
  modelValue: boolean
  selectedCategoryIds?: number[]
}

const props = withDefaults(defineProps<Props>(), {
  selectedCategoryIds: () => []
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', categoryIds: number[]): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const categories = ref<NovelCategoryVO[]>([])
const selectedIds = ref<number[]>([])
const currentChannel = ref<number>(Channel.MALE)

const channelOptions = [
  { value: Channel.MALE, label: ChannelLabels[Channel.MALE] },
  { value: Channel.FEMALE, label: ChannelLabels[Channel.FEMALE] }
]

const filteredCategories = computed(() => {
  return categories.value.filter(cat => cat.category === currentChannel.value)
})

const fetchCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const handleChannelChange = (channel: number) => {
  currentChannel.value = channel
}

const toggleCategory = (categoryId: number) => {
  const index = selectedIds.value.indexOf(categoryId)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(categoryId)
  }
}

const handleConfirm = () => {
  emit('confirm', [...selectedIds.value])
  visible.value = false
}

// 监听抽屉打开，初始化选中状态
watch(visible, (newVal) => {
  if (newVal) {
    selectedIds.value = [...props.selectedCategoryIds]
    fetchCategories()
  }
})
</script>
