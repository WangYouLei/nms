<template>
  <div class="chapter-create-page flex -m-6 h-[calc(100vh-4rem)]">
    <!-- 左侧：可滚动内容区 -->
    <div class="flex-1 overflow-y-auto p-6">
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

    <!-- 抽屉式信息面板 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl mb-4 overflow-hidden">
      <!-- 抽屉头部（可点击展开/收起） -->
      <div 
        class="flex items-center justify-between px-4 py-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        @click="infoDrawerExpanded = !infoDrawerExpanded"
      >
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-2">
            <el-icon :class="{ 'rotate-90': infoDrawerExpanded }" class="transition-transform">
              <ArrowRight />
            </el-icon>
            <span class="font-medium text-gray-800 dark:text-gray-200">
              {{ infoDrawerExpanded ? '收起' : '展开' }}章节信息
            </span>
          </div>
          
          <!-- 收起状态显示简要信息 -->
          <template v-if="!infoDrawerExpanded">
            <div class="flex items-center gap-6 text-sm">
              <div class="flex items-center gap-2">
                <span class="text-gray-500">标题：</span>
                <span class="text-gray-800 dark:text-gray-200">{{ chapterForm.title || '未填写' }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-gray-500">字数：</span>
                <span class="text-primary font-medium">{{ wordCount }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-gray-500">序号：</span>
                <span class="text-gray-800 dark:text-gray-200">第 {{ chapterForm.chapterOrder || chapterCount + 1 }} 章</span>
              </div>
            </div>
          </template>
        </div>
        
        <!-- 统计信息（始终显示） -->
        <div class="flex items-center gap-4 text-sm">
          <div class="flex items-center gap-1 px-3 py-1 bg-primary/10 text-primary rounded-full">
            <el-icon><EditPen /></el-icon>
            <span>{{ wordCount }} 字</span>
          </div>
          <div class="flex items-center gap-1 px-3 py-1 bg-success/10 text-green-600 rounded-full">
            <el-icon><Document /></el-icon>
            <span>{{ paragraphCount }} 段</span>
          </div>
        </div>
      </div>
      
      <!-- 抽屉内容 -->
      <el-collapse-transition>
        <div v-show="infoDrawerExpanded" class="border-t border-gray-100 dark:border-gray-700">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 p-4">
            <!-- 左侧：章节信息 -->
            <div>
              <h4 class="text-sm font-medium text-gray-500 mb-3">章节信息</h4>
              <el-form :model="chapterForm" label-position="top" size="default">
                <el-form-item label="章节标题" required class="mb-3">
                  <el-input 
                    v-model="chapterForm.title" 
                    placeholder="请输入章节标题"
                    maxlength="100"
                    show-word-limit
                    clearable
                  />
                </el-form-item>
                <el-form-item label="章节序号" class="mb-0">
                  <el-input-number 
                    v-model="chapterForm.chapterOrder" 
                    :min="1"
                    :max="9999"
                    class="w-full"
                  />
                  <p class="text-xs text-gray-400 mt-1">留空则自动添加到末尾（第 {{ chapterCount + 1 }} 章）</p>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 右侧：统计信息 -->
            <div>
              <h4 class="text-sm font-medium text-gray-500 mb-3">统计信息</h4>
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4 text-center">
                  <p class="text-3xl font-bold text-primary">{{ wordCount }}</p>
                  <p class="text-sm text-gray-500 mt-1">总字数</p>
                </div>
                <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4 text-center">
                  <p class="text-3xl font-bold text-green-600">{{ paragraphCount }}</p>
                  <p class="text-sm text-gray-500 mt-1">段落数</p>
                </div>
                <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4 text-center">
                  <p class="text-xl font-bold" :class="chapterForm.title ? 'text-green-600' : 'text-gray-400'">
                    {{ chapterForm.title ? '有' : '无' }}
                  </p>
                  <p class="text-sm text-gray-500 mt-1">标题状态</p>
                </div>
                <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4 text-center">
                  <p class="text-3xl font-bold" :class="wordCount >= 2000 ? 'text-green-600' : 'text-red-500'">
                    {{ wordCount >= 2000 ? '达标' : '不足' }}
                  </p>
                  <p class="text-sm text-gray-500 mt-1">建议 2000+ 字</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-transition>
    </div>

    <!-- 内容编辑区域 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl p-4">
        <h3 class="font-medium text-gray-800 dark:text-gray-200">章节内容</h3>
        <el-input
          ref="contentTextarea"
          v-model="chapterForm.content"
          type="textarea"
          :rows="24"
          placeholder="请输入章节内容...&#10;&#10;提示：&#10;- 使用换行分隔段落&#10;- 支持中文标点&#10;- 建议每章 2000-5000 字"
          class="chapter-editor"
        />
      </div>
    </div>

    <!-- 右侧：固定工具栏（不随滚动，占满高度） -->
    <div class="w-40 flex-shrink-0 bg-white dark:bg-gray-800 border-l border-gray-200 dark:border-gray-700 p-4 flex flex-col">
      <h3 class="font-medium text-gray-800 dark:text-gray-200 mb-3">AI工具</h3>
      <div class="flex flex-col gap-3">
        <el-button size="small" type="warning" style="width:100%" @click="onAiContinue" :loading="aiLoading" :disabled="!chapterForm.content.trim()">
          <el-icon class="mr-1"><MagicStick /></el-icon>
          续写灵感
        </el-button>
        <el-button size="small" type="success" style="width:100%" @click="onAiSummary" :loading="aiLoading" :disabled="!chapterForm.content.trim()">
          <el-icon class="mr-1"><Notebook /></el-icon>
          生成摘要
        </el-button>
        <el-button size="small" type="primary" style="width:100%" @click="onContinuation" :loading="continuationLoading" :disabled="!chapterForm.content.trim()">
          <el-icon class="mr-1"><Promotion /></el-icon>
          AI续写
        </el-button>
        <el-button size="small" type="info" style="width:100%" @click="onPolish" :loading="polishLoading" :disabled="!chapterForm.content.trim()">
          <el-icon class="mr-1"><Edit /></el-icon>
          文本润色
        </el-button>
        <el-button size="small" style="width:100%" @click="insertExample">
          <el-icon class="mr-1"><MagicStick /></el-icon>
          插入示例
        </el-button>
        <el-button size="small" style="width:100%" @click="clearContent">
          <el-icon class="mr-1"><Delete /></el-icon>
          清空
        </el-button>
      </div>
    </div>

    <!-- AI写作助手结果弹窗 -->
    <el-dialog
      v-model="aiDialogVisible"
      :title="aiDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="ai-result-content">
        <div v-if="aiLoading" class="flex items-center justify-center py-8">
          <el-icon class="is-loading mr-2" :size="20"><Loading /></el-icon>
          <span class="text-gray-500">AI正在思考中...</span>
        </div>
        <div v-else>
          <pre class="whitespace-pre-wrap text-sm text-gray-700 dark:text-gray-300 leading-relaxed bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">{{ aiResult }}</pre>
          <div v-if="aiResultType === 2" class="mt-4">
            <el-button type="primary" size="small" @click="onApplySummary">应用到标题</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false">关闭</el-button>
        <el-button v-if="aiResult && !aiLoading" type="primary" @click="copyAiResult">复制结果</el-button>
      </template>
    </el-dialog>

    <!-- AI续写结果弹窗 -->
    <el-dialog
      v-model="continuationDialogVisible"
      title="续写灵感引擎"
      width="700px"
      :close-on-click-modal="false"
      @closed="continuationStep = 0"
    >
      <!-- 阶段0：指令输入 -->
      <div v-if="continuationStep === 0">
        <p class="text-sm text-gray-500 mb-3">
          <el-icon class="mr-1"><Cpu /></el-icon>
          可选：输入您的续写要求或提示词，AI将根据您的指令生成更精准的内容
        </p>
        <el-input
          v-model="continuationInstruction"
          type="textarea"
          :rows="4"
          placeholder="例如：主角应该遇到一位神秘老者、剧情需要加入悬疑元素、保持轻松幽默的风格..."
          maxlength="500"
          show-word-limit
        />
        <div class="flex justify-end gap-3 mt-4">
          <el-button @click="continuationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="startContinuation" :loading="continuationLoading">
            <el-icon class="mr-1"><Cpu /></el-icon>
            开始生成
          </el-button>
        </div>
      </div>

      <!-- 阶段1：加载中/结果 -->
      <div v-else>
      <div v-if="continuationLoading" class="flex items-center justify-center py-8">
        <el-icon class="is-loading mr-2" :size="20"><Loading /></el-icon>
        <span class="text-gray-500">AI正在生成续写内容（检索→规划→生成→审查→合规审查）...</span>
      </div>
      <div v-else-if="continuationResult">
        <el-alert
          v-if="!continuationResult.contentSafe"
          title="内容合规提醒"
          type="warning"
          :description="'风险等级：' + continuationResult.contentRiskLevel + '，请仔细审查生成内容'"
          show-icon
          class="mb-4"
        />
        <div class="flex items-center gap-4 mb-4">
          <div class="text-sm text-gray-500">质量评分：</div>
          <el-progress
            :percentage="Math.round(continuationResult.qualityScore)"
            :color="continuationResult.qualityScore >= 80 ? '#67c23a' : continuationResult.qualityScore >= 60 ? '#e6a23c' : '#f56c6c'"
            :stroke-width="18"
            :text-inside="true"
            style="width: 200px"
          />
        </div>
        <el-alert
          v-if="continuationResult.warnings?.length"
          type="warning"
          class="mb-4"
        >
          <template #title>审查警告</template>
          <ul class="list-disc pl-4 text-sm">
            <li v-for="(w, i) in continuationResult.warnings" :key="i">{{ w }}</li>
          </ul>
        </el-alert>
        <div class="mb-4">
          <div class="text-sm font-medium text-gray-600 mb-2">续写内容</div>
          <pre class="whitespace-pre-wrap text-sm text-gray-700 dark:text-gray-300 leading-relaxed bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4 max-h-96 overflow-y-auto">{{ continuationResult.continuationText }}</pre>
        </div>
        <div v-if="continuationResult.usedKnowledge?.length" class="mb-4">
          <div class="text-sm font-medium text-gray-600 mb-2">引用的知识项</div>
          <div class="flex flex-wrap gap-2">
            <el-tag
              v-for="k in continuationResult.usedKnowledge"
              :key="k.id"
              size="small"
              :type="k.itemType === 'character' ? 'primary' : k.itemType === 'setting' ? 'success' : k.itemType === 'plot' ? 'warning' : 'info'"
            >
              {{ k.name }}
            </el-tag>
          </div>
        </div>
      </div>
      </div>
      <template #footer>
        <template v-if="continuationStep === 0">
          <el-button @click="continuationDialogVisible = false">关闭</el-button>
        </template>
        <template v-else>
          <el-button @click="continuationDialogVisible = false">关闭</el-button>
          <el-button v-if="continuationResult?.continuationText" type="primary" @click="applyContinuation">追加到内容</el-button>
          <el-button v-if="continuationResult?.continuationText" @click="copyText(continuationResult.continuationText)">复制续写</el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 文本润色结果弹窗 -->
    <el-dialog
      v-model="polishDialogVisible"
      title="AI文本润色"
      width="700px"
      :close-on-click-modal="false"
      @closed="polishStep = 0"
    >
      <!-- 阶段0：指令输入 -->
      <div v-if="polishStep === 0">
        <!-- 选中文本预览（可编辑） -->
        <div class="mb-4">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-1">
            <el-icon class="mr-1"><Edit /></el-icon>
            待润色文本（{{ polishSelectedText.length }} 字）
          </div>
          <el-input
            v-model="polishSelectedText"
            type="textarea"
            :rows="6"
            maxlength="10000"
            show-word-limit
            class="polish-selected-text"
          />
        </div>

        <p class="text-sm text-gray-500 mb-3">
          <el-icon class="mr-1"><Cpu /></el-icon>
          可选：输入您的润色要求或提示词，AI将根据您的指令进行精准润色
        </p>
        <el-input
          v-model="polishInstruction"
          type="textarea"
          :rows="4"
          placeholder="例如：让对话更自然口语化、增强环境描写、简化冗余表达、偏向古风文风..."
          maxlength="500"
          show-word-limit
        />
        <div class="flex justify-end gap-3 mt-4">
          <el-button @click="polishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="startPolish" :loading="polishLoading">
            <el-icon class="mr-1"><Cpu /></el-icon>
            开始润色
          </el-button>
        </div>
      </div>

      <!-- 阶段1：加载中/结果 -->
      <div v-else>
      <div v-if="polishLoading" class="flex items-center justify-center py-8">
        <el-icon class="is-loading mr-2" :size="20"><Loading /></el-icon>
        <span class="text-gray-500">AI正在润色文本...</span>
      </div>
      <div v-else-if="polishResult">
        <el-alert
          v-if="!polishResult.contentSafe"
          title="内容合规提醒"
          type="warning"
          :description="'风险等级：' + polishResult.contentRiskLevel"
          show-icon
          class="mb-4"
        />
        <div v-if="polishResult.changes?.length" class="mb-4">
          <div class="text-sm font-medium text-gray-600 mb-2">修改说明</div>
          <div class="space-y-1">
            <div v-for="(c, i) in polishResult.changes" :key="i" class="text-sm text-gray-600">
              <el-tag size="small" type="info" class="mr-1">{{ c.type }}</el-tag>
              {{ c.description }}
            </div>
          </div>
        </div>
        <div class="mb-4">
          <div class="text-sm font-medium text-gray-600 mb-2">润色后文本</div>
          <pre class="whitespace-pre-wrap text-sm text-gray-700 dark:text-gray-300 leading-relaxed bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4 max-h-96 overflow-y-auto">{{ polishResult.polishedText }}</pre>
        </div>
      </div>
      </div>
      <template #footer>
        <template v-if="polishStep === 0">
          <el-button @click="polishDialogVisible = false">关闭</el-button>
        </template>
        <template v-else>
          <el-button @click="polishDialogVisible = false">关闭</el-button>
          <el-button v-if="polishResult?.polishedText" type="primary" @click="applyPolish">替换选中文本</el-button>
          <el-button v-if="polishResult?.polishedText" @click="copyText(polishResult.polishedText)">复制润色</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, ArrowRight, Check, Document, Delete, MagicStick, EditPen, Notebook, Loading, Promotion, Edit, Cpu } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNovelDetail, getChapterList, uploadChapter } from '@/api'
import { getImageUrl } from '@/utils/file-url'
import { useAiWriting } from '@/utils/use-ai-writing'
import type { NovelDetailVO, NovelChapterVO } from '@/types'

const router = useRouter()
const route = useRoute()

const novel = ref<NovelDetailVO | null>(null)
const chapters = ref<NovelChapterVO[]>([])
const saving = ref(false)
const publishing = ref(false)
const lastSavedAt = ref('')
const infoDrawerExpanded = ref(true) // 抽屉默认展开

// 续写/润色指令
const continuationInstruction = ref('')
const polishInstruction = ref('')
const continuationStep = ref(0) // 0=输入指令, 1=生成中/结果
const polishStep = ref(0)

// 润色-选中文本模式
const contentTextarea = ref()
const polishSelectedText = ref('')
const polishSelectionStart = ref(0)
const polishSelectionEnd = ref(0)

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

// AI写作助手
const {
  aiLoading,
  aiDialogVisible,
  aiDialogTitle,
  aiResult,
  aiResultType,
  handleAiContinue,
  handleAiSummary,
  applySummary,
  copyAiResult,
  continuationLoading,
  continuationResult,
  continuationDialogVisible,
  handleContinuation,
  polishLoading,
  polishResult,
  polishDialogVisible,
  handlePolish
} = useAiWriting()

const onAiContinue = () => handleAiContinue(chapterForm.content, novel.value?.introduction || '')
const onAiSummary = () => handleAiSummary(chapterForm.content)
const onApplySummary = () => applySummary((text) => { chapterForm.title = text })

const onContinuation = () => {
  if (!chapterForm.content.trim()) {
    ElMessage.warning('请先输入章节内容')
    return
  }
  continuationStep.value = 0
  continuationInstruction.value = ''
  continuationResult.value = null
  continuationDialogVisible.value = true
}

const startContinuation = () => {
  continuationStep.value = 1
  handleContinuation(novelId.value, chapterForm.content, {
    authorInstructions: continuationInstruction.value || undefined
  })
}

const getSelectedText = (): { text: string; start: number; end: number } | null => {
  const textarea = contentTextarea.value?.textarea as HTMLTextAreaElement | undefined
  if (!textarea) return null
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  if (start === end) return null // 未选中任何内容
  return {
    text: textarea.value.substring(start, end),
    start,
    end
  }
}

const onPolish = () => {
  if (!chapterForm.content.trim()) {
    ElMessage.warning('请先输入待润色文本')
    return
  }
  // 获取选中文本
  const selected = getSelectedText()
  if (!selected || !selected.text.trim()) {
    // 提示用户选中文本
    ElMessageBox.confirm(
      '请先在编辑器中选中需要润色的文本段落，选中后点击确定继续。',
      '选择润色文本',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      // 用户点击确定后，再次检查是否已选中文本
      const reSelected = getSelectedText()
      if (!reSelected || !reSelected.text.trim()) {
        ElMessage.warning('未检测到选中文本，请先选中需要润色的文本后再点击按钮')
        return
      }
      polishSelectedText.value = reSelected.text
      polishSelectionStart.value = reSelected.start
      polishSelectionEnd.value = reSelected.end
      polishStep.value = 0
      polishInstruction.value = ''
      polishResult.value = null
      polishDialogVisible.value = true
    }).catch(() => {})
    return
  }
  polishSelectedText.value = selected.text
  polishSelectionStart.value = selected.start
  polishSelectionEnd.value = selected.end
  polishStep.value = 0
  polishInstruction.value = ''
  polishResult.value = null
  polishDialogVisible.value = true
}

const startPolish = () => {
  polishStep.value = 1
  handlePolish(polishSelectedText.value, {
    novelId: novelId.value,
    customInstruction: polishInstruction.value || undefined
  })
}

const applyContinuation = () => {
  if (continuationResult.value?.continuationText) {
    chapterForm.content += '\n\n' + continuationResult.value.continuationText
    continuationDialogVisible.value = false
    ElMessage.success('续写内容已追加')
  }
}

const applyPolish = () => {
  if (polishResult.value?.polishedText) {
    const before = chapterForm.content.substring(0, polishSelectionStart.value)
    const after = chapterForm.content.substring(polishSelectionEnd.value)
    chapterForm.content = before + polishResult.value.polishedText + after
    polishDialogVisible.value = false
    ElMessage.success('已将选中文本替换为润色后内容')
  }
}

const copyText = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
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

    // 调用上传接口，传递字数
    await uploadChapter(novelId.value, chapterForm.title, wordCount.value, file)

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

/* 润色弹窗-待润色文本编辑区 */
.polish-selected-text :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.8;
  background-color: rgb(239, 246, 255);
  border-color: rgb(191, 219, 254);
}

:root.dark .polish-selected-text :deep(.el-textarea__inner) {
  background-color: rgba(30, 58, 138, 0.2);
  border-color: rgba(30, 64, 175, 0.5);
}
</style>