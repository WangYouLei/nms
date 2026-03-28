<template>
  <div class="novel-list-page">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">我的小说</h1>
      <el-button type="primary" @click="router.push('/author/novel/create')">
        <el-icon class="mr-1"><Plus /></el-icon>
        新建小说
      </el-button>
    </div>
    
    <!-- 筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg p-4 mb-4">
      <el-radio-group v-model="filterStatus" @change="fetchNovels">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="false">连载中</el-radio-button>
        <el-radio-button label="true">已完结</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 列表 -->
    <div v-if="loading" class="flex justify-center py-12">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>
    
    <div v-else-if="novels.length === 0" class="bg-white dark:bg-gray-800 rounded-lg p-12 text-center">
      <el-empty description="暂无小说">
        <el-button type="primary" @click="router.push('/author/novel/create')">创建小说</el-button>
      </el-empty>
    </div>
    
    <div v-else class="space-y-4">
      <div 
        v-for="novel in novels" 
        :key="novel.id"
        class="bg-white dark:bg-gray-800 rounded-lg p-4 flex gap-4"
      >
        <img :src="getImageUrl(novel.url)" class="w-24 h-32 object-cover rounded" />
        <div class="flex-1">
          <div class="flex items-center gap-2">
            <h3 class="text-lg font-medium text-gray-800 dark:text-gray-200">{{ novel.name }}</h3>
            <el-tag v-if="novel.isFinished" type="success" size="small">完结</el-tag>
            <el-tag v-else type="primary" size="small">连载中</el-tag>
            <el-tag v-if="novel.isHot" type="danger" size="small">热门</el-tag>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1 ellipsis-2">{{ novel.tags || '暂无简介' }}</p>
          <div class="flex items-center gap-4 mt-2 text-sm text-gray-400">
            <span>{{ novel.chapterCount }}章</span>
            <span>更新于 {{ formatTime(novel.updateTime) }}</span>
          </div>
        </div>
        <div class="flex flex-col gap-2 items-end">
          <el-button size="small" style="width: 88px" @click="router.push(`/author/novel/edit/${novel.id}`)">
            编辑
          </el-button>
          <el-button size="small" style="width: 88px" @click="router.push(`/author/novel/${novel.id}/chapters`)">
            章节管理
          </el-button>
          <el-button size="small" type="primary" style="width: 88px" @click="router.push(`/author/novel/${novel.id}/chapter/create`)">
            添加章节
          </el-button>
          <el-button size="small" type="danger" style="width: 88px" @click="handleDelete(novel)">
            删除
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Loading } from '@element-plus/icons-vue'
import { searchNovels, deleteNovelAuthor } from '@/api'
import { formatRelativeTime } from '@/utils/format'
import { getImageUrl } from '@/utils/file-url'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { NovelListVO } from '@/types'

const router = useRouter()

const loading = ref(true)
const novels = ref<NovelListVO[]>([])
const filterStatus = ref('')

const formatTime = formatRelativeTime

const fetchNovels = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filterStatus.value) {
      params.isFinished = filterStatus.value === 'true'
    }
    const res = await searchNovels(params)
    novels.value = res.data?.list || []
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (novel: NovelListVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除小说《${novel.name}》吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await deleteNovelAuthor(novel.id)
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