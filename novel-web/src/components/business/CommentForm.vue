<template>
  <div class="comment-form">
    <!-- 回复提示 -->
    <div v-if="replyTo" class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-xl mb-3">
      <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
        <span>回复</span>
        <span class="text-primary font-medium">@{{ replyTo.userName }}</span>
      </div>
      <button 
        class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        @click="$emit('cancel-reply')"
      >
        <el-icon><Close /></el-icon>
      </button>
    </div>

    <!-- 输入框 -->
    <div class="relative">
      <el-input
        v-model="content"
        type="textarea"
        :rows="3"
        :placeholder="placeholder"
        :maxlength="2000"
        show-word-limit
        class="comment-input"
      />
    </div>

    <!-- 操作栏 -->
    <div class="flex items-center justify-between mt-3">
      <div class="flex items-center gap-2">
        <!-- 表情按钮 -->
        <el-popover trigger="click" placement="top" :width="300">
          <template #reference>
            <button class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </button>
          </template>
          <div class="grid grid-cols-8 gap-1">
            <button 
              v-for="emoji in emojis" 
              :key="emoji"
              class="w-8 h-8 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700 rounded text-lg"
              @click="insertEmoji(emoji)"
            >
              {{ emoji }}
            </button>
          </div>
        </el-popover>
      </div>

      <el-button 
        type="primary" 
        :loading="submitting"
        :disabled="!content.trim()"
        @click="handleSubmit"
      >
        {{ replyTo ? '回复' : '发表评论' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Close, ChatDotRound } from '@element-plus/icons-vue'
import type { CommentVO } from '@/types/comment'

interface Props {
  replyTo?: CommentVO | null
  placeholder?: string
}

withDefaults(defineProps<Props>(), {
  replyTo: null,
  placeholder: '写下你的评论...'
})

const emit = defineEmits<{
  (e: 'submit', content: string): void
  (e: 'cancel-reply'): void
}>()

const content = ref('')
const submitting = ref(false)

const emojis = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊',
  '😇', '🙂', '😉', '😌', '😍', '🥰', '😘', '😗',
  '😙', '😚', '😋', '😛', '😜', '🤪', '😝', '🤑',
  '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑',
  '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔',
  '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮',
  '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳',
  '😎', '🤓', '🧐', '😕', '😟', '🙁', '😮', '😯'
]

const insertEmoji = (emoji: string) => {
  content.value += emoji
}

const handleSubmit = async () => {
  if (!content.value.trim()) return
  
  submitting.value = true
  try {
    emit('submit', content.value.trim())
    content.value = ''
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.comment-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
}

.comment-input :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
</style>