<template>
  <div class="chapter-edit-page">
    <!-- 顶部导航 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <el-button link @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">编辑章节</h1>
      </div>
      <div class="flex items-center gap-3">
        <span v-if="lastSavedAt" class="text-sm text-gray-400">
          上次保存: {{ lastSavedAt }}
        </span>
        <el-button @click="saveDraft" :loading="saving">
          <el-icon class="mr-1"><Document /></el-icon>
          保存草稿
        </el-button>
        <el-button type="primary" @click="publishChapter" :loading="publishing">
          <el-icon class="mr-1"><Check /></el-icon>
          保存章节
        </el-button>
      </div>
    </div>

    <!-- 小说信息 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl p-4 mb-4 flex items-center gap-4">
      <img :src="getImageUrl(novel?.url)" class="w-16 h-20 object-cover rounded" />
      <div>
        <h2 class="font-medium text-gray-800 dark:text-gray-200">{{ novel?.name || '加载中...' }}</h2>
        <p class="text-sm text-gray-500">章节序号：{{ chapterForm.chapterOrder }}</p>
      </div>
    </div>

    <!-- 编辑区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <!-- 章节标题 -->
      <div class="lg:col-span-1">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4">
          <h3 class="font-medium text-gray-800 dark:text-gray-200 mb-3">章节信息</h3>
          <el-form :model="chapterForm" label-position="top">
            <el-form-item label="章节标题" required>
              <el-input 
                v-model="chapterForm.title" 
                placeholder="请输入章节标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="章节序号">
              <el-input-number 
                v-model="chapterForm.chapterOrder" 
                :min="1"
                :max="9999"
                class="w-full"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 字数统计 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4 mt-4">
          <h3 class="font-medium text-gray-800 dark:text-gray-200 mb-3">统计信息</h3>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-500">总字数</span>
              <span class="font-medium text-gray-800 dark:text-gray-200">{{ wordCount }} 字</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">段落</span>
              <span class="font-medium text-gray-800 dark:text-gray-200">{{ paragraphCount }} 段</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容编辑 -->
      <div class="lg:col-span-2">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4 h-full">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-medium text-gray-800 dark:text-gray-200">章节内容</h3>
            <el-button size="small" @click="clearContent">
              <el-icon class="mr-1"><Delete /></el-icon>
              清空
            </el-button>
          </div>
          <el-input
            v-model="chapterForm.content"
            type="textarea"
            :rows="20"
            placeholder="请输入章节内容..."
            class="chapter-editor"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Check, Document, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNovelDetail, getChapterDetail, getChapterContent, updateChapter } from '@/api'
import { getImageUrl } from '@/utils/file-url'
import type { NovelDetailVO } from '@/types'

const router = useRouter()
const route = useRoute()

const novel = ref<NovelDetailVO | null>(null)
const saving = ref(false)
const publishing = ref(false)
const lastSavedAt = ref('')
const oldFileUrl = ref('')

// 自动保存定时器
let autoSaveTimer: ReturnType<typeof setInterval> | null = null
const AUTO_SAVE_INTERVAL = 60 * 1000 // 1分钟

const chapterForm = reactive({
  id: 0,
  title: '',
  content: '',
  chapterOrder: 1
})

// 计算属性
const novelId = computed(() => Number(route.params.novelId))
const chapterId = computed(() => Number(route.params.chapterId))
const wordCount = computed(() => {
  return chapterForm.content.replace(/\s/g, '').length
})
const paragraphCount = computed(() => {
  const paragraphs = chapterForm.content.split(/\n+/).filter(p => p.trim())
  return paragraphs.length
})

// 获取小说信息
const fetchNovel = async () => {
  try {
    const res = await getNovelDetail(novelId.value)
    novel.value = res.data
  } catch (error) {
    console.error('Failed to fetch novel:', error)
    ElMessage.error('获取小说信息失败')
  }
}

// 获取章节详情
const fetchChapter = async () => {
  try {
    const [detailRes, contentRes] = await Promise.all([
      getChapterDetail(chapterId.value),
      getChapterContent(chapterId.value)
    ])
    
    chapterForm.id = detailRes.data?.id || chapterId.value
    chapterForm.title = detailRes.data?.title || ''
    chapterForm.chapterOrder = detailRes.data?.chapterOrder || 1
    chapterForm.content = contentRes.data?.content || ''
    oldFileUrl.value = detailRes.data?.url || ''
    
    // 从草稿加载（如果有更新的草稿）
    loadDraft()
  } catch (error) {
    console.error('Failed to fetch chapter:', error)
    ElMessage.error('获取章节信息失败')
  }
}

// 清空内容
const clearContent = () => {
  ElMessageBox.confirm('确定要清空所有内容吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    chapterForm.content = ''
    ElMessage.success('已清空')
  }).catch(() => {})
}

// 保存草稿到本地
const saveDraft = (showMessage: boolean = true) => {
  if (!chapterForm.title.trim() && !chapterForm.content.trim()) {
    return
  }
  
  const draft = {
    novelId: novelId.value,
    chapterId: chapterId.value,
    title: chapterForm.title,
    content: chapterForm.content,
    chapterOrder: chapterForm.chapterOrder,
    savedAt: new Date().toISOString()
  }
  
  localStorage.setItem(`chapter_edit_${chapterId.value}`, JSON.stringify(draft))
  
  const now = new Date()
  lastSavedAt.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  
  if (showMessage) {
    ElMessage.success('草稿已保存')
  }
}

// 加载草稿
const loadDraft = () => {
  const draftStr = localStorage.getItem(`chapter_edit_${chapterId.value}`)
  if (draftStr) {
    try {
      const draft = JSON.parse(draftStr)
      // 只有当草稿比当前内容更新时才加载
      if (draft.title || draft.content) {
        chapterForm.title = draft.title || chapterForm.title
        chapterForm.content = draft.content || chapterForm.content
        chapterForm.chapterOrder = draft.chapterOrder || chapterForm.chapterOrder
        
        if (draft.savedAt) {
          const savedDate = new Date(draft.savedAt)
          lastSavedAt.value = `${savedDate.getHours().toString().padStart(2, '0')}:${savedDate.getMinutes().toString().padStart(2, '0')}`
          ElMessage.info('已加载未保存的草稿')
        }
      }
    } catch (e) {
      console.error('Failed to load draft:', e)
    }
  }
}

// 启动自动保存
const startAutoSave = () => {
  if (autoSaveTimer) return
  autoSaveTimer = setInterval(() => {
    saveDraft(false)
  }, AUTO_SAVE_INTERVAL)
}

// 停止自动保存
const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

// 保存章节
const publishChapter = async () => {
  if (!chapterForm.title.trim()) {
    ElMessage.warning('请输入章节标题')
    return
  }
  if (!chapterForm.content.trim()) {
    ElMessage.warning('请输入章节内容')
    return
  }

  try {
    publishing.value = true

    // 将文本内容转换为文件
    const blob = new Blob([chapterForm.content], { type: 'text/plain;charset=utf-8' })
    const file = new File([blob], `${chapterForm.title}.txt`, { type: 'text/plain' })

    // 调用更新接口
    await updateChapter({
      id: chapterForm.id,
      title: chapterForm.title,
      chapterOrder: chapterForm.chapterOrder,
      oldFileUrl: oldFileUrl.value,
      file: file
    })

    // 清除草稿
    localStorage.removeItem(`chapter_edit_${chapterId.value}`)

    ElMessage.success('章节保存成功！')
    router.back()
  } catch (error: any) {
    console.error('Failed to save chapter:', error)
    ElMessage.error(error?.message || '保存失败，请重试')
  } finally {
    publishing.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchNovel(), fetchChapter()])
  startAutoSave()
})

onUnmounted(() => {
  stopAutoSave()
})
</script>

<style scoped>
.chapter-editor :deep(.el-textarea__inner) {
  font-size: 16px;
  line-height: 1.8;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.chapter-editor :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
</style>