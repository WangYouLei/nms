<template>
  <div class="sensitive-word-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">敏感词管理</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">管理评论审核的敏感词库</p>
      </div>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon class="mr-2"><Plus /></el-icon>
        添加敏感词
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4 mb-6">
      <div class="flex flex-wrap gap-4">
        <el-select v-model="filterCategory" placeholder="敏感词类别" clearable class="w-40">
          <el-option label="涉政" :value="1" />
          <el-option label="涉黄" :value="2" />
          <el-option label="涉暴" :value="3" />
          <el-option label="广告" :value="4" />
          <el-option label="其他" :value="5" />
        </el-select>
        <el-select v-model="filterLevel" placeholder="敏感等级" clearable class="w-40">
          <el-option label="低（需人工审核）" :value="1" />
          <el-option label="中（自动拒绝）" :value="2" />
          <el-option label="高（直接拒绝）" :value="3" />
        </el-select>
        <el-button @click="fetchList">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden">
      <el-table :data="wordList" v-loading="loading" stripe>
        <el-table-column prop="word" label="敏感词" min-width="150" />
        <el-table-column prop="categoryDesc" label="类别" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">
              {{ row.categoryDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="levelDesc" label="等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ row.levelDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replacement" label="替换字符" width="100" />
        <el-table-column prop="sourceDesc" label="来源" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch 
              :model-value="row.status === 1"
              @change="(val: string | number | boolean) => handleStatusChange(row, Boolean(val))"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" text @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加对话框 -->
    <el-dialog v-model="showAddDialog" title="添加敏感词" width="500px">
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
            <el-option label="中（自动拒绝）" :value="2" />
            <el-option label="高（直接拒绝）" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="替换字符">
          <el-input v-model="formData.replacement" placeholder="默认为 ***" maxlength="50" />
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
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  getSensitiveWordList, 
  addSensitiveWord, 
  deleteSensitiveWord, 
  updateSensitiveWordStatus 
} from '@/api/comment'
import type { SensitiveWordVO, SensitiveWordDTO } from '@/types/comment'

const loading = ref(false)
const submitting = ref(false)
const wordList = ref<SensitiveWordVO[]>([])
const filterCategory = ref<number>()
const filterLevel = ref<number>()
const showAddDialog = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<SensitiveWordDTO>({
  word: '',
  category: 1,
  level: 1,
  replacement: '***',
  status: 1
})

const rules: FormRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSensitiveWordList(filterCategory.value, filterLevel.value)
    wordList.value = res.data || []
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

const getLevelType = (level: number): 'info' | 'warning' | 'danger' => {
  const types: Record<number, 'info' | 'warning' | 'danger'> = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return types[level] || 'info'
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await addSensitiveWord(formData)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    formRef.value?.resetFields()
    fetchList()
  } catch (error) {
    console.error('Failed to add:', error)
  } finally {
    submitting.value = false
  }
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

onMounted(() => {
  fetchList()
})
</script>