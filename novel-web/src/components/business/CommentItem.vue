<template>
  <div class="comment-item group">
    <!-- 用户头像 -->
    <div class="flex-shrink-0">
      <div 
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
        <!-- 点赞 -->
        <button 
          class="flex items-center gap-1 hover:text-primary transition-colors"
          :class="{ 'text-red-500': comment.isLiked }"
          @click="$emit('like', comment)"
        >
          <el-icon :size="16">
            <component :is="comment.isLiked ? 'StarFilled' : 'Star'" />
          </el-icon>
          <span>{{ comment.likeCount || 0 }}</span>
        </button>

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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import type { CommentVO } from '@/types/comment'
import { formatRelativeTime } from '@/utils/format'
import { useUserStore } from '@/stores'

interface Props {
  comment: CommentVO
}

const props = defineProps<Props>()

defineEmits<{
  (e: 'like', comment: CommentVO): void
  (e: 'reply', comment: CommentVO): void
  (e: 'delete', comment: CommentVO): void
}>()

const userStore = useUserStore()

const isOfficial = computed(() => props.comment.userType === 3)

const canDelete = computed(() => {
  return props.comment.userId === userStore.userId
})

const formatTime = formatRelativeTime

const previewImage = (url: string) => {
  // 简单实现，可以用更复杂的图片预览组件
  window.open(url, '_blank')
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