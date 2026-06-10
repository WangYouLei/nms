<template>
  <div class="comment-manage-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">评论管理</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">管理所有评论，支持筛选、审核和删除</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4 mb-6">
      <el-form :model="queryParams" inline class="flex flex-wrap gap-4">
        <el-form-item label="小说ID">
          <el-input v-model="queryParams.novelId" placeholder="请输入小说ID" clearable class="w-32" />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="queryParams.userName" placeholder="请输入用户昵称" clearable class="w-32" />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input v-model="queryParams.content" placeholder="请输入评论内容" clearable class="w-48" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="queryParams.userType" placeholder="全部" clearable class="w-28">
            <el-option label="访客" :value="1" />
            <el-option label="作者" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditLevel" placeholder="全部" clearable class="w-32">
            <el-option label="未审核" :value="0" />
            <el-option label="本地过滤通过" :value="1" />
            <el-option label="人工审核通过" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <!-- 批量操作栏 -->
      <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center gap-4">
        <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-button 
          type="success" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchAudit(2)"
        >
          批量通过
        </el-button>
      </div>

      <el-table :data="commentList" v-loading="loading" stripe @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="50" />
        <el-table-column label="用户" width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-avatar :size="32" :src="getAvatarUrl(row.userAvatar)">
                {{ row.userName?.charAt(0) }}
              </el-avatar>
              <div>
                <p class="text-sm font-medium">{{ row.userName }}</p>
                <el-tag size="small" :type="getUserTypeTag(row.userType)">
                  {{ row.userTypeName }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评论内容" min-width="350">
          <template #default="{ row }">
            <div class="py-2 overflow-hidden">
              <p class="text-sm text-gray-700 dark:text-gray-300 line-clamp-3 break-words">{{ row.content }}</p>
              <div class="mt-1 text-xs text-gray-400">
                <span v-if="row.targetTypeName">{{ row.targetTypeName }}ID: {{ row.targetId }}</span>
                <span class="mx-2">|</span>
                <span>小说ID: {{ row.novelId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getAuditLevelTag(row.auditLevel)">
              {{ row.auditLevelName || '未审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replyCount" label="回复数" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleAudit(row, cmd)">
              <el-button size="small" link type="success">
                审核<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="2">通过</el-dropdown-item>
                  <el-dropdown-item command="0">标记未审核</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="p-4 flex justify-center">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="评论详情" width="600px">
      <div v-if="currentComment" class="space-y-4">
        <div class="flex items-center gap-4">
          <el-avatar :size="48" :src="getAvatarUrl(currentComment.userAvatar)">
            {{ currentComment.userName?.charAt(0) }}
          </el-avatar>
          <div>
            <p class="font-medium text-lg">{{ currentComment.userName }}</p>
            <p class="text-sm text-gray-500">ID: {{ currentComment.userId }} | {{ currentComment.userTypeName }}</p>
          </div>
        </div>
        <el-divider />
        <div>
          <p class="text-sm text-gray-500 mb-1">评论内容</p>
          <p class="text-gray-800 dark:text-gray-200 bg-gray-50 dark:bg-gray-700 p-3 rounded-lg">
            {{ currentComment.content }}
          </p>
        </div>
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p class="text-gray-500">评论对象</p>
            <p>{{ currentComment.targetTypeName }} (ID: {{ currentComment.targetId }})</p>
          </div>
          <div>
            <p class="text-gray-500">小说ID</p>
            <p>{{ currentComment.novelId }}</p>
          </div>
          <div>
            <p class="text-gray-500">审核状态</p>
            <el-tag :type="getAuditLevelTag(currentComment.auditLevel)">
              {{ currentComment.auditLevelName || '未审核' }}
            </el-tag>
          </div>
          <div>
            <p class="text-gray-500">回复数</p>
            <p>{{ currentComment.replyCount }}</p>
          </div>
          <div>
            <p class="text-gray-500">发布时间</p>
            <p>{{ currentComment.createTime }}</p>
          </div>
          <div>
            <p class="text-gray-500">更新时间</p>
            <p>{{ currentComment.updateTime || '-' }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getAvatarUrl } from '@/utils/file-url'
import { 
  getCommentManagePage, 
  managerDeleteComment, 
  managerAuditComment,
  managerBatchDeleteComment
} from '@/api/comment'
import type { CommentVO, CommentQueryDTO } from '@/types/comment'

const loading = ref(false)
const commentList = ref<CommentVO[]>([])
const total = ref(0)
const selectAll = ref(false)
const selectedIds = ref<number[]>([])

const queryParams = reactive<CommentQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  novelId: undefined,
  userName: undefined,
  content: undefined,
  userType: undefined,
  auditLevel: undefined
})

const showDetailDialog = ref(false)
const currentComment = ref<CommentVO | null>(null)

const getUserTypeTag = (type: number): 'primary' | 'success' | 'warning' | 'info' => {
  const types: Record<number, 'primary' | 'success' | 'warning' | 'info'> = {
    1: 'primary',
    2: 'success',
    3: 'warning'
  }
  return types[type] || 'info'
}

const getAuditLevelTag = (level: number): 'warning' | 'success' | 'info' => {
  const tags: Record<number, 'warning' | 'success' | 'info'> = {
    0: 'warning',
    1: 'info',
    2: 'success'
  }
  return tags[level] ?? 'warning'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCommentManagePage(queryParams)
    commentList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchList()
}

const handleReset = () => {
  queryParams.novelId = undefined
  queryParams.userName = undefined
  queryParams.content = undefined
  queryParams.userType = undefined
  queryParams.auditLevel = undefined
  queryParams.pageNum = 1
  fetchList()
}

const handleSelectionChange = (selection: CommentVO[]) => {
  selectedIds.value = selection.map(item => item.id)
  selectAll.value = selection.length === commentList.value.length && commentList.value.length > 0
}

const handleSelectAll = (val: boolean) => {
  selectedIds.value = val ? commentList.value.map(item => item.id) : []
}

const handleViewDetail = (row: CommentVO) => {
  currentComment.value = row
  showDetailDialog.value = true
}

const handleAudit = async (row: CommentVO, command: string) => {
  const auditLevel = parseInt(command)
  try {
    await managerAuditComment(row.id, auditLevel)
    ElMessage.success('审核成功')
    fetchList()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const handleDelete = async (row: CommentVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '确定删除',
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

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条评论吗？`, '批量删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await managerBatchDeleteComment(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchList()
  } catch (error) {
    // 用户取消
  }
}

const handleBatchAudit = async (auditLevel: number) => {
  try {
    const confirmText = auditLevel === 2 ? '批量通过' : '标记未审核'
    await ElMessageBox.confirm(`确定要${confirmText}选中的 ${selectedIds.value.length} 条评论吗？`, '批量审核确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    // 逐个审核（后端没有批量审核接口）
    for (const id of selectedIds.value) {
      await managerAuditComment(id, auditLevel)
    }
    ElMessage.success('批量审核成功')
    selectedIds.value = []
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
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

:deep(.el-table) {
  table-layout: fixed;
}

:deep(.el-table__body) {
  width: 100% !important;
}

:deep(.el-table__cell) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-table__cell .cell) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 评论内容列允许换行 */
:deep(.el-table__cell:nth-child(3) .cell) {
  white-space: normal;
  overflow: visible;
}
</style>