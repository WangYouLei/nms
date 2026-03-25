<template>
  <div class="my-comments-page container mx-auto px-4 py-6">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="flex items-center gap-3 mb-6">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="text-xl font-bold text-gray-800 dark:text-gray-200">我的评论</h1>
      </div>

      <!-- 评论列表 -->
      <div v-if="loading" class="flex justify-center py-12">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>

      <div v-else-if="comments.length === 0" class="text-center py-12 bg-white dark:bg-gray-800 rounded-2xl">
        <el-icon :size="48" class="text-gray-300 dark:text-gray-600 mb-3"><ChatDotRound /></el-icon>
        <p class="text-gray-400">暂无评论记录</p>
        <el-button type="primary" class="mt-4" @click="router.push('/home')">
          去发现小说
        </el-button>
      </div>

      <div v-else class="space-y-4">
        <div 
          v-for="comment in comments" 
          :key="comment.id"
          class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4"
        >
          <!-- 评论内容 -->
          <p class="text-gray-700 dark:text-gray-300 text-sm leading-relaxed mb-3">
            {{ comment.content }}
          </p>

          <!-- 评论对象 -->
          <div class="flex items-center justify-between text-sm">
            <div class="flex items-center gap-2 text-gray-500 dark:text-gray-400">
              <el-tag size="small" effect="plain">
                {{ comment.targetType === 1 ? '小说评论' : '章节评论' }}
              </el-tag>
              <span>{{ formatTime(comment.createTime) }}</span>
            </div>
            <div class="flex items-center gap-3">
              <el-button 
                type="danger" 
                text 
                size="small"
                @click="handleDelete(comment)"
              >
                删除
              </el-button>
            </div>
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
          @current-change="fetchComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Loading, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyComments, deleteComment } from '@/api/comment'
import { useUserStore } from '@/stores'
import { formatRelativeTime } from '@/utils/format'
import type { CommentVO } from '@/types/comment'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const comments = ref<CommentVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatTime = formatRelativeTime

const fetchComments = async () => {
  if (!userStore.userId) return

  loading.value = true
  try {
    const res = await getMyComments(
      userStore.userId,
      userStore.role === 'VISITOR' ? 1 : userStore.role === 'AUTHOR' ? 2 : 3,
      currentPage.value,
      pageSize.value
    )
    comments.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch comments:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (comment: CommentVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteComment(
      comment.id,
      userStore.userId!,
      userStore.role === 'VISITOR' ? 1 : userStore.role === 'AUTHOR' ? 2 : 3
    )
    
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchComments()
})
</script>