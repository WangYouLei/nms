<template>
  <div class="comment-manage-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">评论审核</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">审核用户评论内容</p>
      </div>
    </div>

    <!-- Tab切换 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4 mb-6">
      <div class="flex gap-4">
        <button 
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="activeTab === 'pending' ? 'bg-primary text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
          @click="activeTab = 'pending'; fetchList()"
        >
          待审核
          <span v-if="pendingCount > 0" class="ml-1 px-1.5 py-0.5 bg-white/20 rounded-full text-xs">
            {{ pendingCount }}
          </span>
        </button>
        <button 
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="activeTab === 'all' ? 'bg-primary text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
          @click="activeTab = 'all'; fetchList()"
        >
          全部评论
        </button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <el-table :data="commentList" v-loading="loading" stripe>
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
            <!-- 敏感词命中提示 -->
            <div v-if="row.status === 3 && row.sensitiveWords" class="mt-1">
              <el-tag type="warning" size="small">命中敏感词</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评论对象" width="120">
          <template #default="{ row }">
            <span class="text-gray-600 dark:text-gray-400">
              {{ row.targetType === 1 ? '小说' : '章节' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 3">
              <el-button type="success" size="small" @click="handleAudit(row, true)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, false)">
                拒绝
              </el-button>
            </template>
            <el-button v-else type="danger" text size="small" @click="handleDelete(row)">
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
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog v-model="showAuditDialog" title="审核评论" width="500px">
      <div class="space-y-4">
        <div>
          <p class="text-sm text-gray-500">评论内容</p>
          <p class="mt-1 text-gray-800 dark:text-gray-200">{{ currentComment?.content }}</p>
        </div>
        <el-input
          v-model="auditReason"
          type="textarea"
          :rows="3"
          placeholder="请输入审核意见（拒绝时必填）"
        />
      </div>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button 
          v-if="auditApproved === false"
          type="danger" 
          :disabled="!auditReason"
          @click="submitAudit"
        >
          确认拒绝
        </el-button>
        <el-button 
          v-else
          type="success"
          @click="submitAudit"
        >
          确认通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingComments, auditComment, managerDeleteComment } from '@/api/comment'
import type { CommentVO } from '@/types/comment'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

const loading = ref(false)
const commentList = ref<CommentVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('pending')
const pendingCount = ref(0)
const showAuditDialog = ref(false)
const currentComment = ref<CommentVO | null>(null)
const auditApproved = ref(false)
const auditReason = ref('')

const getUserType = (type: number) => {
  const types: Record<number, string> = {
    1: '访客',
    2: '作者',
    3: '管理员'
  }
  return types[type] || '未知'
}

const getStatusType = (status: number): 'warning' | 'success' | 'danger' | 'info' => {
  const types: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '待人工审核'
  }
  return texts[status] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'pending') {
      const res = await getPendingComments(currentPage.value, pageSize.value)
      commentList.value = res.data?.list || []
      total.value = res.data?.total || 0
      pendingCount.value = total.value
    } else {
      // TODO: 获取全部评论的接口
      commentList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('Failed to fetch list:', error)
  } finally {
    loading.value = false
  }
}

const handleAudit = (comment: CommentVO, approved: boolean) => {
  currentComment.value = comment
  auditApproved.value = approved
  auditReason.value = ''
  showAuditDialog.value = true
}

const submitAudit = async () => {
  if (!currentComment.value) return

  try {
    await auditComment(
      currentComment.value.id,
      auditApproved.value,
      auditReason.value,
      userStore.userId!,
      userStore.userName!
    )
    
    ElMessage.success(auditApproved.value ? '审核通过' : '审核拒绝')
    showAuditDialog.value = false
    fetchList()
  } catch (error) {
    console.error('Failed to audit:', error)
  }
}

const handleDelete = async (row: CommentVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await managerDeleteComment(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchList()
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