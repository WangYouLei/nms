<template>
  <div class="chapter-create-page">
    <!-- 顶部导航 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <el-button link @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">新建章节</h1>
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
          发布章节
        </el-button>
      </div>
    </div>

    <!-- 小说信息 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl p-4 mb-4 flex items-center gap-4">
      <img :src="getImageUrl(novel?.url)" class="w-16 h-20 object-cover rounded" />
      <div>
        <h2 class="font-medium text-gray-800 dark:text-gray-200">{{ novel?.name || '加载中...' }}</h2>
        <p class="text-sm text-gray-500">当前章节数：{{ chapterCount }}</p>
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
              <p class="text-xs text-gray-400 mt-1">留空则自动添加到末尾</p>
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
            <div class="flex items-center gap-2">
              <el-button size="small" @click="insertExample">
                <el-icon class="mr-1"><MagicStick /></el-icon>
                插入示例
              </el-button>
              <el-button size="small" @click="clearContent">
                <el-icon class="mr-1"><Delete /></el-icon>
                清空
              </el-button>
            </div>
          </div>
          <el-input
            v-model="chapterForm.content"
            type="textarea"
            :rows="20"
            placeholder="请输入章节内容...&#10;&#10;提示：&#10;- 使用换行分隔段落&#10;- 支持中文标点&#10;- 建议每章 2000-5000 字"
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
import { ArrowLeft, Check, Document, Delete, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNovelDetail, getChapterList, uploadChapter } from '@/api'
import { getImageUrl } from '@/utils/file-url'
import type { NovelDetailVO, NovelChapterVO } from '@/types'

const router = useRouter()
const route = useRoute()

const novel = ref<NovelDetailVO | null>(null)
const chapters = ref<NovelChapterVO[]>([])
const saving = ref(false)
const publishing = ref(false)
const lastSavedAt = ref('')

// 自动保存定时器
let autoSaveTimer: ReturnType<typeof setInterval> | null = null
const AUTO_SAVE_INTERVAL = 60 * 1000 // 1分钟

const chapterForm = reactive({
  title: '',
  content: '',
  chapterOrder: undefined as number | undefined
})

// 计算属性
const novelId = computed(() => Number(route.params.novelId))
const chapterCount = computed(() => chapters.value.length)
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

// 获取章节列表
const fetchChapters = async () => {
  try {
    const res = await getChapterList(novelId.value)
    chapters.value = res.data || []
    // 默认章节序号为当前章节数+1
    if (!chapterForm.chapterOrder) {
      chapterForm.chapterOrder = chapters.value.length + 1
    }
  } catch (error) {
    console.error('Failed to fetch chapters:', error)
  }
}

// 插入示例内容
const insertExample = () => {
  if (chapterForm.content) {
    ElMessageBox.confirm('当前内容将被替换，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      chapterForm.content = getExampleContent()
    }).catch(() => {})
  } else {
    chapterForm.content = getExampleContent()
  }
}

const getExampleContent = () => {
  return `第一章 神秘的开端

这是一个风和日丽的早晨，阳光透过窗帘洒落在书桌上。

李明推开窗户，深吸一口气，空气中弥漫着淡淡的花香。这座城市总是给他一种安心的感觉。

"今天又是新的一天。"他自言自语道。

街道上人来人往，每个人都在为自己的生活奔波。李明看着这一切，心中不禁感慨万千。

就在这时，一个神秘的电话打破了他的宁静……

（请在此继续创作您的小说内容）`
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
    return // 空内容不保存
  }
  
  const draft = {
    novelId: novelId.value,
    title: chapterForm.title,
    content: chapterForm.content,
    chapterOrder: chapterForm.chapterOrder,
    savedAt: new Date().toISOString()
  }
  
  localStorage.setItem(`chapter_draft_${novelId.value}`, JSON.stringify(draft))
  
  // 更新保存时间显示
  const now = new Date()
  lastSavedAt.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  
  if (showMessage) {
    ElMessage.success('草稿已保存')
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

// 加载草稿
const loadDraft = () => {
  const draftStr = localStorage.getItem(`chapter_draft_${novelId.value}`)
  if (draftStr) {
    try {
      const draft = JSON.parse(draftStr)
      chapterForm.title = draft.title || ''
      chapterForm.content = draft.content || ''
      chapterForm.chapterOrder = draft.chapterOrder
      
      // 显示上次保存时间
      if (draft.savedAt) {
        const savedDate = new Date(draft.savedAt)
        lastSavedAt.value = `${savedDate.getHours().toString().padStart(2, '0')}:${savedDate.getMinutes().toString().padStart(2, '0')}`
        ElMessage.info('已加载上次保存的草稿')
      }
    } catch (e) {
      console.error('Failed to load draft:', e)
    }
  }
}

// 发布章节
const publishChapter = async () => {
  // 验证
  if (!chapterForm.title.trim()) {
    ElMessage.warning('请输入章节标题')
    return
  }
  if (!chapterForm.content.trim()) {
    ElMessage.warning('请输入章节内容')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要发布章节"${chapterForm.title}"吗？发布后将立即对读者可见。`,
      '发布确认',
      {
        confirmButtonText: '发布',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    publishing.value = true

    // 将文本内容转换为文件
    const blob = new Blob([chapterForm.content], { type: 'text/plain;charset=utf-8' })
    const file = new File([blob], `${chapterForm.title}.txt`, { type: 'text/plain' })

    // 调用上传接口
    await uploadChapter(novelId.value, chapterForm.title, file)

    // 清除草稿
    localStorage.removeItem(`chapter_draft_${novelId.value}`)

    ElMessage.success('章节发布成功！')
    
    // 跳转到章节列表
    router.push(`/author/novel/${novelId.value}/chapters`)
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to publish chapter:', error)
      ElMessage.error(error?.message || '发布失败，请重试')
    }
  } finally {
    publishing.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchNovel(), fetchChapters()])
  loadDraft()
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