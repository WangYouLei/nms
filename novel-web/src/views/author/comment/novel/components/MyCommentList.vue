<template>
  <div class="my-comment-list">
    <!-- 评论列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <div v-if="loading" class="flex justify-center py-8">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>

      <div v-else-if="comments.length === 0" class="text-center py-12">
        <el-icon :size="48" class="text-gray-300 dark:text-gray-600 mb-3"><ChatDotRound /></el-icon>
        <p class="text-gray-400">您还没有发表过评论</p>
      </div>

      <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
        <div 
          v-for="comment in comments" 
          :key="comment.id" 
          class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        >
          <div class="flex gap-3">
            <!-- 头像 -->
            <div class="flex-shrink-0">
              <img
                v-if="comment.userAvatar"
                :src="getAvatarUrl(comment.userAvatar)"
                :alt="comment.userName"
                class="w-10 h-10 rounded-full object-cover"
              />
              <div
                v-else
                class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold bg-gradient-primary"
              >
                {{ comment.userName?.charAt(0) }}
              </div>
            </div>
            
            <!-- 内容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-medium text-gray-800 dark:text-gray-200">{{ comment.userName }}</span>
                <el-tag size="small" effect="plain" :type="comment.targetType === 1 ? 'primary' : 'success'">
                  {{ comment.targetType === 1 ? '小说评论' : '章节评论' }}
                </el-tag>
                <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
              </div>
              
              <!-- 回复对象 -->
              <div v-if="comment.replyUserName" class="text-sm text-gray-500 dark:text-gray-400 mb-1">
                回复 <span class="text-primary">@{{ comment.replyUserName }}</span>
              </div>
              
              <p class="text-gray-700 dark:text-gray-300 text-sm leading-relaxed mb-2">
                {{ comment.content }}
              </p>

              <!-- 操作按钮 -->
              <div class="flex items-center gap-3 text-sm">
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
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="p-4 flex justify-center border-t border-gray-100 dark:border-gray-700">
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
import { Loading, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyComments, deleteComment } from '@/api/comment'
import { useUserStore } from '@/stores'
import type { CommentVO } from '@/types/comment'
import { getAvatarUrl } from '@/utils/file-url'
import { formatRelativeTime } from '@/utils/format'

interface Props {
  novelId: number
  userId: number
}

const props = defineProps<Props>()
const userStore = useUserStore()

const loading = ref(false)
const comments = ref<CommentVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatTime = formatRelativeTime

const fetchComments = async () => {
  loading.value = true
  try {
    // 获取我的评论（作者类型=2）
    const res = await getMyComments(
      props.userId,
      2, // 作者类型
      currentPage.value,
      pageSize.value
    )
    
    // 如果有novelId，过滤该小说的评论
    let list = res.data?.list || []
    if (props.novelId) {
      list = list.filter((c: CommentVO) => c.novelId === props.novelId)
    }
    
    comments.value = list
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch my comments:', error)
    ElMessage.error('获取我的评论失败')
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

    await deleteComment(comment.id, userStore.userId!, 2)
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