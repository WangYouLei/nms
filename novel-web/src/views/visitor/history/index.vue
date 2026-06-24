<template>
  <div class="history-page container mx-auto px-4 py-6">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">阅读历史</h1>
        <span class="text-gray-500">共 {{ readingList.length }} 本</span>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      </div>

      <!-- 空状态 -->
      <div v-else-if="readingList.length === 0" class="text-center py-20">
        <el-icon :size="60" class="text-gray-300 mb-4"><Clock /></el-icon>
        <p class="text-gray-400 mb-4">暂无阅读记录</p>
        <el-button type="primary" @click="router.push('/home')">
          去发现
        </el-button>
      </div>

      <!-- 阅读历史列表 -->
      <div v-else class="space-y-4">
        <div
          v-for="item in readingList"
          :key="item.id"
          class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden hover:shadow-card-hover transition-all duration-300 cursor-pointer group"
          @click="handleContinueReading(item)"
        >
          <div class="flex gap-4 p-4">
            <!-- 封面 -->
            <img
              :src="getImageUrl(item.novelUrl)"
              class="w-20 h-28 object-cover rounded-xl flex-shrink-0"
            />
            <!-- 信息 -->
            <div class="flex-1 min-w-0 py-1">
              <h3 class="font-bold text-gray-800 dark:text-gray-200 text-lg truncate">
                {{ item.novelName }}
              </h3>
              <p class="text-gray-500 dark:text-gray-400 mt-1 text-sm">
                {{ item.authorName || '未知作者' }}
              </p>
              <div class="flex items-center gap-3 mt-2 text-sm text-gray-400">
                <span>读到第 {{ item.chapterOrder }} 章</span>
              </div>
              <p class="text-xs text-gray-400 mt-1">
                {{ formatTime(item.lastReadTime) }}
              </p>
            </div>
            <!-- 操作按钮 -->
            <div class="flex flex-col justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <el-button size="small" type="primary" @click.stop="handleContinueReading(item)">
                继续
              </el-button>
              <el-button size="small" @click.stop="handleDelete(item)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecentReading, deleteReadingProgress } from '@/api/reading-progress'
import { getImageUrl } from '@/utils/file-url'
import type { VisitorReadingProgressVO } from '@/types'

const router = useRouter()

const loading = ref(true)
const readingList = ref<VisitorReadingProgressVO[]>([])

const fetchReadingList = async () => {
  loading.value = true
  try {
    const res = await getRecentReading()
    readingList.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch reading list:', error)
    ElMessage.error('获取阅读历史失败')
  } finally {
    loading.value = false
  }
}

const handleContinueReading = (item: VisitorReadingProgressVO) => {
  router.push(`/read/${item.novelId}/${item.chapterId}`)
}

const handleDelete = async (item: VisitorReadingProgressVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除《${item.novelName}》的阅读记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteReadingProgress(item.novelId)
    readingList.value = readingList.value.filter(r => r.id !== item.id)
    ElMessage.success('已删除')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete reading progress:', error)
      ElMessage.error('操作失败')
    }
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchReadingList()
})
</script>
