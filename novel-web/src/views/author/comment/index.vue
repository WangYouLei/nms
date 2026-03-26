<template>
  <div class="author-comment-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">评论管理</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">选择小说查看和管理读者评论</p>
      </div>
    </div>

    <!-- 小说列表 -->
    <div v-if="loading" class="flex justify-center py-12">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <div v-else-if="novels.length === 0" class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-12 text-center">
      <el-icon :size="48" class="text-gray-300 dark:text-gray-600 mb-3"><Document /></el-icon>
      <p class="text-gray-400">您还没有发布任何小说</p>
      <el-button type="primary" class="mt-4" @click="$router.push('/author/novels')">
        去发布小说
      </el-button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <!-- 小说卡片 -->
      <div 
        v-for="novel in novels" 
        :key="novel.id"
        class="novel-card bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden hover:shadow-lg transition-shadow cursor-pointer relative"
        @click="goToNovelComments(novel.id)"
      >
        <!-- 封面 -->
        <div class="aspect-[3/4] relative overflow-hidden">
          <img 
            v-if="novel.url"
            :src="getImageUrl(novel.url)"
            :alt="novel.name"
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full bg-gradient-primary flex items-center justify-center">
            <el-icon :size="64" class="text-white/50"><Document /></el-icon>
          </div>
          
          <!-- 统计信息 -->
          <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-3">
            <div class="flex items-center gap-3 text-white text-sm">
              <span class="flex items-center gap-1">
                <el-icon><ChatDotRound /></el-icon>
                {{ novel.commentCount || 0 }} 评论
              </span>
              <span class="flex items-center gap-1">
                <el-icon><View /></el-icon>
                {{ novel.readCount || 0 }}
              </span>
            </div>
          </div>
        </div>

        <!-- 信息 -->
        <div class="p-4">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 truncate mb-1">
            {{ novel.name }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 truncate mb-2">
            {{ novel.introduction || '暂无简介' }}
          </p>
          <div class="flex items-center justify-between text-xs text-gray-400">
            <span>{{ novel.chapterCount || 0 }} 章节</span>
            <el-tag size="small" :type="novel.isFinished ? 'success' : 'warning'">
              {{ novel.isFinished ? '已完结' : '连载中' }}
            </el-tag>
          </div>
        </div>

        <!-- 悬浮操作 -->
        <div class="novel-card-overlay">
          <el-button type="primary" size="large">
            <el-icon class="mr-2"><ChatDotRound /></el-icon>
            查看评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="flex justify-center mt-6">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="fetchNovels"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, Document, ChatDotRound, View } from '@element-plus/icons-vue'
import { searchNovels } from '@/api/novel'
import { useUserStore } from '@/stores'
import { getImageUrl } from '@/utils/file-url'
import type { NovelListVO } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const novels = ref<NovelListVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const fetchNovels = async () => {
  loading.value = true
  try {
    const res = await searchNovels({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      authorId: userStore.userId
    })
    novels.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  } finally {
    loading.value = false
  }
}

const goToNovelComments = (novelId: number) => {
  router.push(`/author/comments/novel/${novelId}`)
}

onMounted(() => {
  fetchNovels()
})
</script>

<style scoped>
.author-comment-page {
  min-height: calc(100vh - 120px);
}

/* 小说卡片悬浮效果 - 父元素hover时显示overlay */
.novel-card .novel-card-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.3s ease;
  pointer-events: none; /* 不阻挡下层点击 */
}

.novel-card:hover .novel-card-overlay {
  opacity: 1;
  pointer-events: auto; /* hover时允许点击 */
}
</style>