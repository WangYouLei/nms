<template>
  <div class="comment-manage-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">人工审核</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">审核AI审核后的内容</p>
      </div>
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2 text-sm">
          <span class="text-gray-500">待审核:</span>
          <el-tag type="warning">{{ statistics.pending }}</el-tag>
        </div>
        <div class="flex items-center gap-2 text-sm">
          <span class="text-gray-500">已通过:</span>
          <el-tag type="success">{{ statistics.approved }}</el-tag>
        </div>
        <div class="flex items-center gap-2 text-sm">
          <span class="text-gray-500">已拒绝:</span>
          <el-tag type="danger">{{ statistics.rejected }}</el-tag>
        </div>
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
          <span v-if="statistics.pending > 0" class="ml-1 px-1.5 py-0.5 bg-white/20 rounded-full text-xs">
            {{ statistics.pending }}
          </span>
        </button>
        <button 
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="activeTab === 'approved' ? 'bg-primary text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
          @click="activeTab = 'approved'; fetchList()"
        >
          已通过
        </button>
        <button 
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="activeTab === 'rejected' ? 'bg-primary text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
          @click="activeTab = 'rejected'; fetchList()"
        >
          已拒绝
        </button>
        <button 
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="activeTab === 'all' ? 'bg-primary text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
          @click="activeTab = 'all'; fetchList()"
        >
          全部记录
        </button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <el-table :data="auditList" v-loading="loading" stripe>
        <el-table-column label="审核对象" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderTypeTag(row.aimType)">
              {{ row.aimTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aimId" label="对象ID" width="100" />
        <el-table-column label="AI审核意见" min-width="300">
          <template #default="{ row }">
            <div v-if="row.aiResult" class="text-sm">
              <p class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ row.aiResult }}</p>
            </div>
            <span v-else class="text-gray-400">无AI审核记录</span>
          </template>
        </el-table-column>
        <el-table-column prop="resultName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.result)">
              {{ row.resultName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refusalReason" label="拒绝理由" width="150">
          <template #default="{ row }">
            <span v-if="row.refusalReason">{{ row.refusalReason }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="审核人" width="120">
          <template #default="{ row }">
            <span v-if="row.managerName">{{ row.managerName }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="firstAuditTime" label="审核时间" width="180">
          <template #default="{ row }">
            <span v-if="row.firstAuditTime">{{ row.firstAuditTime }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.result === 0">
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
    <el-dialog v-model="showAuditDialog" title="审核" width="500px">
      <div class="space-y-4">
        <div>
          <p class="text-sm text-gray-500">审核对象</p>
          <p class="mt-1 text-gray-800 dark:text-gray-200">
            {{ currentAudit?.aimTypeName }} (ID: {{ currentAudit?.aimId }})
          </p>
        </div>
        <div v-if="currentAudit?.aiResult">
          <p class="text-sm text-gray-500">AI审核意见</p>
          <p class="mt-1 text-gray-800 dark:text-gray-200 whitespace-pre-wrap bg-gray-50 dark:bg-gray-700 p-3 rounded-lg">
            {{ currentAudit?.aiResult }}
          </p>
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
          :loading="submitting"
          @click="submitAudit"
        >
          确认拒绝
        </el-button>
        <el-button 
          v-else
          type="success"
          :loading="submitting"
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
import { 
  getPendingAuditList, 
  getManualAuditList, 
  approveAudit, 
  rejectAudit, 
  deleteAuditRecord,
  getAuditStatistics
} from '@/api/manager'
import type { ManualAuditVO } from '@/types/comment'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const auditList = ref<ManualAuditVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('pending')

const statistics = ref({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

const showAuditDialog = ref(false)
const currentAudit = ref<ManualAuditVO | null>(null)
const auditApproved = ref(false)
const auditReason = ref('')

const getOrderTypeTag = (type: number): 'primary' | 'success' | 'warning' => {
  const types: Record<number, 'primary' | 'success' | 'warning'> = {
    1: 'primary',
    2: 'success',
    3: 'warning'
  }
  return types[type] || 'primary'
}

const getResultType = (result: number): 'warning' | 'success' | 'danger' | 'info' => {
  const types: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[result] || 'info'
}

const fetchStatistics = async () => {
  try {
    const res = await getAuditStatistics()
    statistics.value = res.data || { total: 0, pending: 0, approved: 0, rejected: 0 }
  } catch (error) {
    console.error('Failed to fetch statistics:', error)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    let result
    if (activeTab.value === 'pending') {
      result = await getPendingAuditList(currentPage.value, pageSize.value)
    } else {
      const resultValue = activeTab.value === 'approved' ? 1 : activeTab.value === 'rejected' ? 2 : undefined
      result = await getManualAuditList({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        result: resultValue
      })
    }
    auditList.value = result.data?.list || []
    total.value = result.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch list:', error)
  } finally {
    loading.value = false
  }
}

const handleAudit = (audit: ManualAuditVO, approved: boolean) => {
  currentAudit.value = audit
  auditApproved.value = approved
  auditReason.value = ''
  showAuditDialog.value = true
}

const submitAudit = async () => {
  if (!currentAudit.value) return

  submitting.value = true
  try {
    if (auditApproved.value) {
      await approveAudit(
        currentAudit.value.id,
        userStore.userId!,
        userStore.userName!
      )
    } else {
      await rejectAudit(
        currentAudit.value.id,
        auditReason.value,
        userStore.userId!,
        userStore.userName!
      )
    }
    
    ElMessage.success(auditApproved.value ? '审核通过' : '审核拒绝')
    showAuditDialog.value = false
    fetchList()
    fetchStatistics()
  } catch (error) {
    console.error('Failed to audit:', error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: ManualAuditVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条审核记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteAuditRecord(row.id)
    ElMessage.success('删除成功')
    fetchList()
    fetchStatistics()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchList()
  fetchStatistics()
})
</script>

<style scoped>
.whitespace-pre-wrap {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>