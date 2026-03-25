<template>
  <div class="comment-list">
    <!-- 标题栏 -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-2">
        <div class="w-1 h-5 bg-gradient-primary rounded-full"></div>
        <h3 class="font-bold text-gray-800 dark:text-gray-200">评论</h3>
        <span class="text-sm text-gray-400">({{ total }})</span>
      </div>
      
      <!-- 排序 -->
      <div class="flex items-center gap-2">
        <button 
          class="px-3 py-1 text-sm rounded-lg transition-colors"
          :class="sortBy === 'time' ? 'bg-primary/10 text-primary' : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'"
          @click="changeSort('time')"
        >
          最新
        </button>
        <button 
          class="px-3 py-1 text-sm rounded-lg transition-colors"
          :class="sortBy === 'hot' ? 'bg-primary/10 text-primary' : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'"
          @click="changeSort('hot')"
        >
          最热
        </button>
      </div>
    </div>

    <!-- 发表评论 -->
    <div class="mb-6 p-4 bg-white dark:bg-gray-800 rounded-2xl shadow-card">
      <CommentForm 
        :reply-to="replyTo"
        @submit="handleCommentSubmit"
        @cancel-reply="cancelReply"
      />
    </div>

    <!-- 评论列表 -->
    <div v-if="loading" class="flex justify-center py-8">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <div v-else-if="comments.length === 0" class="text-center py-12">
      <el-icon :size="48" class="text-gray-300 dark:text-gray-600 mb-3"><ChatDotRound /></el-icon>
      <p class="text-gray-400">暂无评论，快来发表第一条评论吧~</p>
    </div>

    <div v-else class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4">
      <CommentItem 
        v-for="comment in comments" 
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
        @delete="handleDelete"
      />
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="flex justify-center mt-6">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Loading, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentForm from './CommentForm.vue'
import CommentItem from './CommentItem.vue'
import { getCommentsByTarget, addComment, deleteComment } from '@/api/comment'
import type { CommentVO, CommentDTO, CommentTargetType } from '@/types/comment'
import { useUserStore } from '@/stores'

interface Props {
  targetType: CommentTargetType
  targetId: number
  novelId: number
}

const props = defineProps<Props>()

const userStore = useUserStore()

const loading = ref(false)
const comments = ref<CommentVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('time')
const replyTo = ref<CommentVO | null>(null)

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getCommentsByTarget(
      props.targetType,
      props.targetId,
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

const handleCommentSubmit = async (content: string) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const dto: CommentDTO = {
      userId: userStore.userId!,
      userType: userStore.role === 'VISITOR' ? 1 : userStore.role === 'AUTHOR' ? 2 : 3,
      userName: userStore.userName!,
      userAvatar: userStore.userInfo?.avatar || undefined,
      targetType: props.targetType,
      targetId: props.targetId,
      novelId: props.novelId,
      content,
      parentId: replyTo.value?.id,
      replyUserId: replyTo.value?.userId,
      replyUserName: replyTo.value?.userName,
      rootId: replyTo.value?.rootId || replyTo.value?.id
    }

    await addComment(dto)
    ElMessage.success('评论发表成功')
    replyTo.value = null
    fetchComments()
  } catch (error: any) {
    console.error('Failed to add comment:', error)
    // 从错误中获取后端返回的消息并提供更准确的反馈
    const errorMessage = error?.response?.data?.msg || '评论发表失败'
    
    // 检查错误信息中是否包含预设的特定关键词，以提供更直观的反馈
    if (errorMessage.includes('高危')) {
      ElMessage.error('您的评论内容包含敏感词汇，不允许发布')
    } else if (errorMessage.includes('AI审核')) {
      ElMessage.warning('您的评论内容AI审核未通过，建议修改后重试')
    } else {
      ElMessage.error(errorMessage)
    }
  }
}

const handleReply = (comment: CommentVO) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  replyTo.value = comment
}

const cancelReply = () => {
  replyTo.value = null
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
    // 用户取消不提示
  }
}

const changeSort = (sort: string) => {
  sortBy.value = sort
  currentPage.value = 1
  fetchComments()
}

const handlePageChange = () => {
  fetchComments()
}

watch(() => props.targetId, () => {
  currentPage.value = 1
  fetchComments()
})

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  min-height: 200px;
}
</style>