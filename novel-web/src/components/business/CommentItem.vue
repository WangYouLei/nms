<template>
  <div class="comment-item group">
    <!-- 用户头像 -->
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
        :class="isOfficial ? 'bg-gradient-warm' : 'bg-gradient-primary'"
      >
        {{ comment.userName?.charAt(0) }}
      </div>
    </div>

    <!-- 评论内容 -->
    <div class="flex-1 min-w-0">
      <!-- 用户信息 -->
      <div class="flex items-center gap-2 mb-1">
        <span class="font-medium text-gray-800 dark:text-gray-200">{{ comment.userName }}</span>
        <span 
          v-if="isOfficial" 
          class="px-2 py-0.5 text-xs font-medium bg-gradient-warm text-white rounded-full"
        >
          官方
        </span>
        <span 
          v-if="isNovelAuthor" 
          class="px-2 py-0.5 text-xs font-medium bg-gradient-primary text-white rounded-full"
        >
          作者
        </span>
        <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
      </div>

      <!-- 回复对象 -->
      <div v-if="comment.replyUserName" class="text-sm text-gray-500 dark:text-gray-400 mb-1">
        回复 <span class="text-primary">@{{ comment.replyUserName }}</span>
      </div>

      <!-- 评论内容 -->
      <p class="text-gray-700 dark:text-gray-300 text-sm leading-relaxed">
        {{ comment.content }}
      </p>

      <!-- 图片 -->
      <div v-if="comment.images && comment.images.length > 0" class="flex gap-2 mt-2">
        <img 
          v-for="(img, index) in comment.images" 
          :key="index"
          :src="img" 
          class="w-20 h-20 object-cover rounded-lg cursor-pointer hover:opacity-80 transition-opacity"
          @click="previewImage(img)"
        />
      </div>

      <!-- 操作按钮 -->
      <div class="flex items-center gap-4 mt-2 text-sm text-gray-400">
        <!-- 回复 -->
        <button 
          class="flex items-center gap-1 hover:text-primary transition-colors"
          @click="$emit('reply', comment)"
        >
          <el-icon :size="16"><ChatDotRound /></el-icon>
          <span>回复</span>
        </button>

        <!-- 删除 -->
        <button 
          v-if="canDelete"
          class="flex items-center gap-1 hover:text-red-500 transition-colors"
          @click="$emit('delete', comment)"
        >
          <el-icon :size="16"><Delete /></el-icon>
          <span>删除</span>
        </button>

        <!-- 展开回复 -->
        <button 
          v-if="comment.replyCount > 0 && !isReply"
          class="flex items-center gap-1 hover:text-primary transition-colors text-primary"
          @click="toggleReplies"
        >
          <el-icon :size="16"><ChatLineRound /></el-icon>
          <span>{{ showReplies ? '收起' : `${comment.replyCount}条回复` }}</span>
        </button>
      </div>

      <!-- 子评论列表 -->
      <div v-if="showReplies && replies.length > 0" class="mt-3 pl-4 border-l-2 border-gray-200 dark:border-gray-700">
        <CommentItem
          v-for="reply in replies"
          :key="reply.id"
          :comment="reply"
          :is-reply="true"
          @reply="$emit('reply', $event)"
          @delete="$emit('delete', $event)"
        />
        
        <!-- 加载更多回复 -->
        <div v-if="hasMoreReplies" class="py-2">
          <button 
            v-if="!loadingReplies"
            class="text-sm text-primary hover:underline"
            @click="loadMoreReplies"
          >
            加载更多回复...
          </button>
          <el-icon v-else class="is-loading text-primary" :size="16">
            <Loading />
          </el-icon>
        </div>
      </div>

      <!-- 加载回复中 -->
      <div v-if="loadingReplies && replies.length === 0" class="mt-3 pl-4">
        <el-icon class="is-loading text-gray-400" :size="16"><Loading /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChatDotRound, Delete, ChatLineRound, Loading } from '@element-plus/icons-vue'
import type { CommentVO } from '@/types/comment'
import { formatRelativeTime } from '@/utils/format'
import { useUserStore } from '@/stores'
import { getAvatarUrl } from '@/utils/file-url'
import { getReplies } from '@/api/comment'

interface Props {
  comment: CommentVO
  isReply?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isReply: false
})

defineEmits<{
  (e: 'reply', comment: CommentVO): void
  (e: 'delete', comment: CommentVO): void
}>()

const userStore = useUserStore()

const isOfficial = computed(() => props.comment.userType === 3)
    
    const isNovelAuthor = computed(() => {
      // 判断评论者是否是该小说的作者
      return props.comment.novelAuthorId && props.comment.userId === props.comment.novelAuthorId
    })

const canDelete = computed(() => {
  return props.comment.userId === userStore.userId
})

const formatTime = formatRelativeTime

const previewImage = (url: string) => {
  window.open(url, '_blank')
}

// 子评论相关
const showReplies = ref(false)
const loadingReplies = ref(false)
const replies = ref<CommentVO[]>(props.comment.replies || [])
const replyPage = ref(1)
const replyPageSize = 5
const hasMoreReplies = computed(() => replies.value.length < props.comment.replyCount)

const toggleReplies = async () => {
  showReplies.value = !showReplies.value
  if (showReplies.value && replies.value.length === 0 && props.comment.replyCount > 0) {
    await loadReplies()
  }
}

const loadReplies = async () => {
  if (!props.comment.id) return
  
  loadingReplies.value = true
  try {
    const res = await getReplies(props.comment.id, 1, replyPageSize)
    replies.value = res.data?.list || []
    replyPage.value = 1
  } catch (error) {
    console.error('Failed to load replies:', error)
  } finally {
    loadingReplies.value = false
  }
}

const loadMoreReplies = async () => {
  if (!props.comment.id) return
  
  loadingReplies.value = true
  try {
    replyPage.value++
    const res = await getReplies(props.comment.id, replyPage.value, replyPageSize)
    const newReplies = res.data?.list || []
    replies.value = [...replies.value, ...newReplies]
  } catch (error) {
    console.error('Failed to load more replies:', error)
    replyPage.value--
  } finally {
    loadingReplies.value = false
  }
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-item:last-child {
  border-bottom: none;
}
</style>