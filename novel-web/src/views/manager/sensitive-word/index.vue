<template>
  <div class="sensitive-word-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">敏感词管理</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">管理评论审核的敏感词库</p>
      </div>
      <div class="flex gap-2">
        <el-button @click="handleRefreshCache">
          <el-icon class="mr-2"><Refresh /></el-icon>
          刷新缓存
        </el-button>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon class="mr-2"><Plus /></el-icon>
          添加敏感词
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4 mb-6">
      <div class="flex flex-wrap gap-4">
        <el-input 
          v-model="filterWord" 
          placeholder="敏感词搜索" 
          clearable 
          class="w-48"
          @keyup.enter="fetchList"
        />
        <el-select v-model="filterCategory" placeholder="敏感词类别" clearable class="w-40">
          <el-option label="涉政" :value="1" />
          <el-option label="涉黄" :value="2" />
          <el-option label="涉暴" :value="3" />
          <el-option label="广告" :value="4" />
          <el-option label="其他" :value="5" />
        </el-select>
        <el-select v-model="filterLevel" placeholder="敏感等级" clearable class="w-40">
          <el-option label="低（需人工审核）" :value="1" />
          <el-option label="高（直接拒绝）" :value="2" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable class="w-32">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <el-table :data="wordList" v-loading="loading" stripe>
        <el-table-column prop="word" label="敏感词" min-width="150" />
        <el-table-column prop="categoryName" label="类别" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">
              {{ row.categoryName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="levelName" label="等级" width="140">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ row.levelName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceName" label="来源" width="120" />
        <el-table-column prop="statusName" label="状态" width="80">
          <template #default="{ row }">
            <el-switch 
              :model-value="row.status === 1"
              @change="(val: string | number | boolean) => handleStatusChange(row, Boolean(val))"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">
              编辑
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
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="showAddDialog" :title="isEdit ? '编辑敏感词' : '添加敏感词'" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="formData.word" placeholder="请输入敏感词" maxlength="100" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="formData.category" placeholder="请选择类别" class="w-full">
            <el-option label="涉政" :value="1" />
            <el-option label="涉黄" :value="2" />
            <el-option label="涉暴" :value="3" />
            <el-option label="广告" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感等级" prop="level">
          <el-select v-model="formData.level" placeholder="请选择等级" class="w-full">
            <el-option label="低（需人工审核）" :value="1" />
            <el-option label="高（直接拒绝）" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formStatus" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  getSensitiveWordList, 
  addSensitiveWord, 
  updateSensitiveWord,
  deleteSensitiveWord, 
  updateSensitiveWordStatus,
  refreshSensitiveWordCache
} from '@/api/comment'
import type { SensitiveWordVO, SensitiveWordDTO } from '@/types/comment'

const loading = ref(false)
const submitting = ref(false)
const wordList = ref<SensitiveWordVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选条件
const filterWord = ref('')
const filterCategory = ref<number>()
const filterLevel = ref<number>()
const filterStatus = ref<number>()

const showAddDialog = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<SensitiveWordDTO>({
  word: '',
  category: 1,
  level: 1,
  status: 1
})

const formStatus = computed({
  get: () => formData.status === 1,
  set: (val) => { formData.status = val ? 1 : 0 }
})

const rules: FormRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSensitiveWordList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      word: filterWord.value || undefined,
      category: filterCategory.value,
      level: filterLevel.value,
      status: filterStatus.value
    })
    wordList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch list:', error)
  } finally {
    loading.value = false
  }
}

const getCategoryType = (category: number): 'danger' | 'warning' | 'info' | 'primary' | undefined => {
  const types: Record<number, 'danger' | 'warning' | 'info' | 'primary' | undefined> = {
    1: 'danger',
    2: 'warning',
    3: 'danger',
    4: 'info',
    5: undefined
  }
  return types[category] || undefined
}

const getLevelType = (level: number): 'info' | 'danger' => {
  return level === 2 ? 'danger' : 'info'
}

const handleEdit = (row: SensitiveWordVO) => {
  isEdit.value = true
  formData.id = row.id
  formData.word = row.word
  formData.category = row.category
  formData.level = row.level
  formData.status = row.status
  showAddDialog.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSensitiveWord(formData)
      ElMessage.success('更新成功')
    } else {
      await addSensitiveWord(formData)
      ElMessage.success('添加成功')
    }
    showAddDialog.value = false
    resetForm()
    fetchList()
  } catch (error) {
    console.error('Failed to submit:', error)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  isEdit.value = false
  formData.id = undefined
  formData.word = ''
  formData.category = 1
  formData.level = 1
  formData.status = 1
  formRef.value?.resetFields()
}

const handleStatusChange = async (row: SensitiveWordVO, status: boolean) => {
  try {
    await updateSensitiveWordStatus(row.id, status ? 1 : 0)
    row.status = status ? 1 : 0
    ElMessage.success(status ? '已启用' : '已禁用')
  } catch (error) {
    console.error('Failed to update status:', error)
  }
}

const handleDelete = async (row: SensitiveWordVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这个敏感词吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteSensitiveWord(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    // 用户取消
  }
}

const handleRefreshCache = async () => {
  try {
    await refreshSensitiveWordCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    console.error('Failed to refresh cache:', error)
  }
}

onMounted(() => {
  fetchList()
})
</script>