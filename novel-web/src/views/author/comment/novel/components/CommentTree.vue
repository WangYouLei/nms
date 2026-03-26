<template>
  <div class="comment-tree">
    <!-- 筛选区域 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4 mb-4">
      <div class="flex items-center gap-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索评论内容..."
          class="w-64"
          clearable
          @keyup.enter="fetchComments"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="fetchComments">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <div v-if="loading" class="flex justify-center py-8">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>

      <div v-else-if="comments.length === 0" class="text-center py-12">
        <el-icon :size="48" class="text-gray-300 dark:text-gray-600 mb-3"><ChatDotRound /></el-icon>
        <p class="text-gray-400">暂无评论</p>
      </div>

      <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
        <div 
          v-for="comment in comments" 
          :key="comment.id" 
          class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        >
          <!-- 根评论 -->
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
                class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold"
                :class="getUserAvatarClass(comment)"
              >
                {{ comment.userName?.charAt(0) }}
              </div>
            </div>
            
            <!-- 内容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-medium text-gray-800 dark:text-gray-200">{{ comment.userName }}</span>
                <span 
                  v-if="comment.userType === 3" 
                  class="px-2 py-0.5 text-xs font-medium bg-gradient-warm text-white rounded-full"
                >
                  官方
                </span>
                <span 
                  v-if="isNovelAuthor(comment)" 
                  class="px-2 py-0.5 text-xs font-medium bg-gradient-primary text-white rounded-full"
                >
                  作者
                </span>
                <el-tag size="small" effect="plain" :type="comment.targetType === 1 ? 'primary' : 'success'">
                  {{ comment.targetType === 1 ? '小说' : '章节' }}
                </el-tag>
                <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
              </div>
              
              <p class="text-gray-700 dark:text-gray-300 text-sm leading-relaxed mb-2">
                {{ comment.content }}
              </p>

              <!-- 操作按钮 -->
              <div class="flex items-center gap-3 text-sm">
                <el-button type="primary" text size="small" @click="handleReply(comment)">
                  回复
                </el-button>
                <el-button 
                  v-if="canDelete(comment)" 
                  type="danger" 
                  text 
                  size="small" 
                  @click="handleDelete(comment)"
                >
                  删除
                </el-button>
              </div>

              <!-- 回复列表 -->
              <div 
                v-if="comment.replies && comment.replies.length > 0" 
                class="mt-3 pl-4 border-l-2 border-gray-200 dark:border-gray-700 space-y-3"
              >
                <div 
                  v-for="reply in comment.replies" 
                  :key="reply.id" 
                  class="flex gap-2"
                >
                  <div class="flex-shrink-0">
                    <img
                      v-if="reply.userAvatar"
                      :src="getAvatarUrl(reply.userAvatar)"
                      :alt="reply.userName"
                      class="w-8 h-8 rounded-full object-cover"
                    />
                    <div
                      v-else
                      class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-bold"
                      :class="getUserAvatarClass(reply)"
                    >
                      {{ reply.userName?.charAt(0) }}
                    </div>
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-2">
                      <span class="font-medium text-gray-800 dark:text-gray-200 text-sm">{{ reply.userName }}</span>
                      <span 
                        v-if="reply.userType === 3" 
                        class="px-1.5 py-0.5 text-xs font-medium bg-gradient-warm text-white rounded-full"
                      >
                        官方
                      </span>
                      <span 
                        v-if="isNovelAuthor(reply)" 
                        class="px-1.5 py-0.5 text-xs font-medium bg-gradient-primary text-white rounded-full"
                      >
                        作者
                      </span>
                      <span v-if="reply.replyUserName" class="text-gray-400 text-xs">
                        回复 <span class="text-primary">@{{ reply.replyUserName }}</span>
                      </span>
                    </div>
                    <p class="text-gray-600 dark:text-gray-400 text-sm">{{ reply.content }}</p>
                    <div class="flex items-center gap-3 mt-1 text-xs">
                      <span class="text-gray-400">{{ formatTime(reply.createTime) }}</span>
                      <el-button type="primary" text size="small" @click="handleReply(reply)">
                        回复
                      </el-button>
                      <el-button 
                        v-if="canDelete(reply)" 
                        type="danger" 
                        text 
                        size="small" 
                        @click="handleDelete(reply)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
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

    <!-- 回复对话框 -->
    <el-dialog v-model="showReplyDialog" title="回复评论" width="500px">
      <div class="space-y-4">
        <div class="p-3 bg-gray-50 dark:bg-gray-700 rounded-xl">
          <p class="text-sm text-gray-500">原评论</p>
          <p class="mt-1 text-gray-800 dark:text-gray-200">{{ replyTarget?.content }}</p>
        </div>
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          placeholder="写下你的回复..."
          :maxlength="2000"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!replyContent.trim()" @click="submitReply">
          发送回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
  import { Search, Loading, ChatDotRound } from '@element-plus/icons-vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { getNovelCommentTree, addComment, deleteComment } from '@/api/comment'
  import { useUserStore } from '@/stores'
  import type { CommentVO, CommentDTO } from '@/types/comment'
  import { formatRelativeTime } from '@/utils/format'
  import { getAvatarUrl } from '@/utils/file-url'

interface Props {
  novelId: number
  targetType: number
  authorId?: number
}

const props = defineProps<Props>()
const userStore = useUserStore()

const loading = ref(false)
const comments = ref<CommentVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const showReplyDialog = ref(false)
const replyTarget = ref<CommentVO | null>(null)
const replyContent = ref('')
const submitting = ref(false)

const getUserAvatarClass = (comment: CommentVO) => {
  if (comment.userType === 3) return 'bg-gradient-warm'
  if (isNovelAuthor(comment)) return 'bg-gradient-primary'
  return 'bg-gray-400'
}

const isNovelAuthor = (comment: CommentVO) => {
  return props.authorId && comment.userId === props.authorId
}

const canDelete = (comment: CommentVO) => {
  return comment.userId === userStore.userId || props.authorId === userStore.userId
}

const formatTime = formatRelativeTime

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getNovelCommentTree(
      props.novelId,
      props.targetType,
      currentPage.value,
      pageSize.value
    )
    
    let list = res.data?.list || []
    
    // 如果有搜索关键词，进行前端过滤
    if (searchKeyword.value.trim()) {
      const keyword = searchKeyword.value.toLowerCase()
      list = list.filter((c: CommentVO) => 
        c.content.toLowerCase().includes(keyword) ||
        (c.replies && c.replies.some((r: CommentVO) => r.content.toLowerCase().includes(keyword)))
      )
    }
    
    comments.value = list
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch comments:', error)
    ElMessage.error('获取评论失败')
  } finally {
    loading.value = false
  }
}

const handleReply = (comment: CommentVO) => {
  replyTarget.value = comment
  replyContent.value = ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  if (!replyTarget.value || !replyContent.value.trim()) return

  submitting.value = true
  try {
    const dto: CommentDTO = {
      userId: userStore.userId!,
      userType: 2,
      userName: userStore.userName!,
      targetType: replyTarget.value.targetType,
      targetId: replyTarget.value.targetId,
      novelId: props.novelId,
      content: replyContent.value.trim(),
      parentId: replyTarget.value.id,
      replyUserId: replyTarget.value.userId,
      replyUserName: replyTarget.value.userName,
      rootId: replyTarget.value.rootId || replyTarget.value.id
    }

    await addComment(dto)
    ElMessage.success('回复成功')
    showReplyDialog.value = false
    fetchComments()
  } catch (error: any) {
    console.error('Failed to reply:', error)
    const errorMessage = error?.response?.data?.msg || '回复失败'
    if (errorMessage.includes('高危')) {
      ElMessage.error('您的回复内容包含敏感词汇，不允许发布')
    } else {
      ElMessage.error(errorMessage)
    }
  } finally {
    submitting.value = false
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

watch(() => props.targetType, () => {
  currentPage.value = 1
  fetchComments()
})

onMounted(() => {
  fetchComments()
})
</script>