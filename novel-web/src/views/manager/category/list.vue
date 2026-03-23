<template>
  <div class="category-manage-page">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">分类管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon class="mr-1"><Plus /></el-icon>
        新增分类
      </el-button>
    </div>
    
    <!-- 表格 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
      <el-table :data="categories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="分类名称" />
        <el-table-column label="频道" width="100">
          <template #default="{ row }">
            <el-tag :type="row.category === 1 ? 'primary' : 'danger'">
              {{ row.category === 1 ? '男频' : '女频' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热门" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.isHot" :active-value="1" :inactive-value="0" @change="handleHotChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleEdit(row)">编辑</el-button>
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
          @current-change="fetchCategories"
        />
      </div>
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="editForm.type" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="频道">
          <el-radio-group v-model="editForm.category">
            <el-radio :value="1">男频</el-radio>
            <el-radio :value="2">女频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="热门">
          <el-switch v-model="editForm.isHot" :active-value="1" :inactive-value="0" />
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
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api'
import { formatDateTime } from '@/utils/format'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { NovelCategoryVO } from '@/types'

const loading = ref(true)
const categories = ref<NovelCategoryVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)

const editForm = reactive({
  id: undefined as number | undefined,
  type: '',
  category: 1,
  isHot: 0
})

const formatTime = formatDateTime

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
    total.value = categories.value.length
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editForm.id = undefined
  editForm.type = ''
  editForm.category = 1
  editForm.isHot = 0
  dialogVisible.value = true
}

const handleEdit = (row: NovelCategoryVO) => {
  editForm.id = row.id
  editForm.type = row.type
  editForm.category = row.category
  editForm.isHot = row.isHot
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (error) {
    // 用户取消
  }
}

const handleHotChange = async (row: NovelCategoryVO) => {
  try {
    await updateCategory({ id: row.id, type: row.type, category: row.category, isHot: row.isHot })
    ElMessage.success('更新成功')
  } catch (error) {
    fetchCategories()
  }
}

const handleSubmit = async () => {
  if (!editForm.type.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  try {
    if (editForm.id) {
      await updateCategory(editForm as any)
      ElMessage.success('更新成功')
    } else {
      await addCategory(editForm as any)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchCategories()
  } catch (error) {
    console.error('Failed to save category:', error)
  }
}

onMounted(() => {
  fetchCategories()
})
</script>