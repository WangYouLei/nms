<template>
  <div class="administrator-manage-page">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">管理员管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon class="mr-1"><Plus /></el-icon>
        新增管理员
      </el-button>
    </div>
    
    <!-- 表格 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
      <el-table :data="administrators" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="getAvatarUrl(row.avatar)">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="昵称" width="120" />
        <el-table-column prop="account" label="账号" />
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="p-4 flex justify-end">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchAdministrators"
        />
      </div>
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑管理员' : '新增管理员'" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.name" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="editForm.account" placeholder="请输入账号" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item v-if="!editForm.id" label="密码">
          <el-input v-model="editForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getManagerPage, addManager, updateManager, deleteManager, updateManagerPassword } from '@/api'
import { formatDateTime } from '@/utils/format'
import { getAvatarUrl } from '@/utils/file-url'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { ManagerVO } from '@/types'

const loading = ref(true)
const administrators = ref<ManagerVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)

const editForm = reactive({
  id: undefined as number | undefined,
  name: '',
  account: '',
  password: ''
})

const formatTime = formatDateTime

const fetchAdministrators = async () => {
  loading.value = true
  try {
    const res = await getManagerPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    administrators.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch administrators:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editForm.id = undefined
  editForm.name = ''
  editForm.account = ''
  editForm.password = ''
  dialogVisible.value = true
}

const handleEdit = (row: ManagerVO) => {
  editForm.id = row.id
  editForm.name = row.name || ''
  editForm.account = row.account
  editForm.password = ''
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个管理员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteManager(id)
    ElMessage.success('删除成功')
    fetchAdministrators()
  } catch (error) {
    // 用户取消
  }
}

const handleResetPassword = async (row: ManagerVO) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^.{6,20}$/,
      inputErrorMessage: '密码长度在6-20位之间'
    })
    await updateManagerPassword(row.id, value)
    ElMessage.success('密码重置成功')
  } catch (error) {
    // 用户取消
  }
}

const handleSubmit = async () => {
  if (!editForm.account.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  
  if (!editForm.id && !editForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  
  try {
    if (editForm.id) {
      await updateManager({ id: editForm.id, name: editForm.name, account: editForm.account })
      ElMessage.success('更新成功')
    } else {
      await addManager({ name: editForm.name, account: editForm.account, password: editForm.password })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchAdministrators()
  } catch (error) {
    console.error('Failed to save administrator:', error)
  }
}

onMounted(() => {
  fetchAdministrators()
})
</script>