<template>
  <div class="novel-manage-page">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">小说管理</h1>
    </div>
    
    <!-- 搜索 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg p-4 mb-4">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="小说名称/作者" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isFinished" placeholder="全部" clearable>
            <el-option label="连载中" :value="false" />
            <el-option label="已完结" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 表格 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
      <el-table :data="novels" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img :src="getImageUrl(row.url)" class="w-16 h-20 object-cover rounded" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="小说名称" min-width="150" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="chapterCount" label="章节" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isFinished" type="success" size="small">完结</el-tag>
            <el-tag v-else type="primary" size="small">连载</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热门" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isHot" type="danger" size="small">热门</el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleView(row)">查看</el-button>
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchNovels"
          @current-change="fetchNovels"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchNovels, deleteNovelManager } from '@/api'
import { formatDateTime } from '@/utils/format'
import { getImageUrl } from '@/utils/file-url'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { NovelListVO } from '@/types'

const router = useRouter()

const loading = ref(true)
const novels = ref<NovelListVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  isFinished: undefined as boolean | undefined
})

const formatTime = formatDateTime

const fetchNovels = async () => {
  loading.value = true
  try {
    const res = await searchNovels({
      keyword: searchForm.keyword || undefined,
      isFinished: searchForm.isFinished,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    novels.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchNovels()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.isFinished = undefined
  handleSearch()
}

const handleView = (novel: NovelListVO) => {
  router.push(`/novel/${novel.id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这部小说吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNovelManager(id)
    ElMessage.success('删除成功')
    fetchNovels()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchNovels()
})
</script>