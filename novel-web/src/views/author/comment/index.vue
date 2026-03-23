<template>
  <div class="author-comment-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">评论管理</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">查看和管理读者对您作品的评论</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4 mb-6">
      <div class="flex flex-wrap gap-4 items-center">
        <el-select 
          v-model="selectedNovelId" 
          placeholder="选择小说" 
          class="w-64"
          clearable
          @change="fetchComments"
        >
          <el-option 
            v-for="novel in novels" 
            :key="novel.id" 
            :label="novel.name" 
            :value="novel.id" 
          />
        </el-select>
        <el-radio-group v-model="commentType" @change="fetchComments">
          <el-radio-button :value="1">小说评论</el-radio-button>
          <el-radio-button :value="2">章节评论</el-radio-button>
        </el-radio-group>
        <el-button @click="fetchComments">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <el-table :data="comments" v-loading="loading" stripe>
        <el-table-column label="评论者" width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div 
                class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-bold"
                :class="row.userType === 3 ? 'bg-gradient-warm' : 'bg-gradient-primary'"
              >
                {{ row.userName?.charAt(0) }}
              </div>
              <div>
                <p class="font-medium text-gray-800 dark:text-gray-200">{{ row.userName }}</p>
                <p class="text-xs text-gray-400">{{ getUserType(row.userType) }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="300">
          <template #default="{ row }">
            <p class="text-gray-700 dark:text-gray-300 line-clamp-2">{{ row.content }}</p>
            <div v-if="row.targetType === 2" class="mt-1 text-xs text-gray-400">
              章节：{{ row.chapterTitle || `章节ID: ${row.targetId}` }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评论对象" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ row.targetType === 1 ? '小说' : '章节' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleReply(row)">
              回复
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="p-4 flex justify-center">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchComments"
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
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentList, addComment, deleteComment } from '@/api/comment'
import { searchNovels } from '@/api/novel'
import { useUserStore } from '@/stores'
import type { CommentVO, CommentDTO, CommentQueryDTO } from '@/types/comment'
import type { NovelListVO } from '@/types'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const novels = ref<NovelListVO[]>([])
const comments = ref<CommentVO[]>([])
const selectedNovelId = ref<number>()
const commentType = ref(1)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showReplyDialog = ref(false)
const replyTarget = ref<CommentVO | null>(null)
const replyContent = ref('')

const getUserType = (type: number) => {
  const types: Record<number, string> = {
    1: '访客',
    2: '作者',
    3: '管理员'
  }
  return types[type] || '未知'
}

const fetchNovels = async () => {
  try {
    const res = await searchNovels({ pageNum: 1, pageSize: 100 })
    novels.value = res.data?.list || []
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  }
}

const fetchComments = async () => {
  if (!selectedNovelId.value) {
    ElMessage.warning('请先选择小说')
    return
  }

  loading.value = true
  try {
    const query: CommentQueryDTO = {
      novelId: selectedNovelId.value,
      targetType: commentType.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getCommentList(query)
    comments.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch comments:', error)
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
      userType: 2, // 作者
      userName: userStore.userName!,
      targetType: replyTarget.value.targetType,
      targetId: replyTarget.value.targetId,
      novelId: replyTarget.value.novelId,
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
  } catch (error) {
    console.error('Failed to reply:', error)
    ElMessage.error('回复失败')
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

onMounted(() => {
  fetchNovels()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>