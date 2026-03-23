<template>
  <div class="chapter-list-page">
    <div class="flex items-center justify-between mb-6">
      <div>
        <el-button link @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200 ml-4 inline">章节管理</h1>
      </div>
      <el-button type="primary" @click="router.push('/author/chapter/upload')">
        <el-icon class="mr-1"><Upload /></el-icon>
        上传章节
      </el-button>
    </div>
    
    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
      <el-table :data="chapters" v-loading="loading" stripe>
        <el-table-column prop="chapterOrder" label="章节序号" width="100" />
        <el-table-column prop="title" label="章节标题" />
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Upload } from '@element-plus/icons-vue'
import { getChapterList, deleteChapter } from '@/api'
import { formatDateTime } from '@/utils/format'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { NovelChapterVO } from '@/types'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const chapters = ref<NovelChapterVO[]>([])

const formatTime = formatDateTime

const fetchChapters = async () => {
  const novelId = Number(route.params.novelId)
  loading.value = true
  try {
    const res = await getChapterList(novelId)
    chapters.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch chapters:', error)
  } finally {
    loading.value = false
  }
}

const handleEdit = (_chapter: NovelChapterVO) => {
  ElMessage.info('编辑功能开发中')
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个章节吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteChapter(id)
    ElMessage.success('删除成功')
    fetchChapters()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  fetchChapters()
})
</script>