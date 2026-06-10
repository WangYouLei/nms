import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiWritingAssist, continuationGenerate, polishText } from '@/api'
import { AiWritingType, PolishAspect } from '@/types'
import type { ContinuationGenerateVO, PolishResultVO } from '@/types'

export function useAiWriting() {
  const aiLoading = ref(false)
  const aiDialogVisible = ref(false)
  const aiDialogTitle = ref('')
  const aiResult = ref('')
  const aiResultType = ref(0)

  // 续写引擎
  const continuationLoading = ref(false)
  const continuationResult = ref<ContinuationGenerateVO | null>(null)
  const continuationDialogVisible = ref(false)

  // 润色
  const polishLoading = ref(false)
  const polishResult = ref<PolishResultVO | null>(null)
  const polishDialogVisible = ref(false)

  const handleAiContinue = async (content: string, context?: string) => {
    if (!content.trim()) return
    aiLoading.value = true
    aiDialogVisible.value = true
    aiDialogTitle.value = '续写灵感建议'
    aiResult.value = ''
    aiResultType.value = AiWritingType.CONTINUE
    try {
      const res = await aiWritingAssist({
        type: AiWritingType.CONTINUE,
        content,
        context: context || ''
      })
      aiResult.value = res.data?.result || '暂无建议'
    } catch {
      aiResult.value = '获取续写建议失败，请稍后重试'
    } finally {
      aiLoading.value = false
    }
  }

  const handleAiSummary = async (content: string) => {
    if (!content.trim()) return
    aiLoading.value = true
    aiDialogVisible.value = true
    aiDialogTitle.value = 'AI章节摘要'
    aiResult.value = ''
    aiResultType.value = AiWritingType.SUMMARY
    try {
      const res = await aiWritingAssist({
        type: AiWritingType.SUMMARY,
        content
      })
      aiResult.value = res.data?.result || '暂无摘要'
    } catch {
      aiResult.value = '生成摘要失败，请稍后重试'
    } finally {
      aiLoading.value = false
    }
  }

  /**
   * 续写引擎 - 完整流水线（检索→规划→生成→审查→合规审查）
   */
  const handleContinuation = async (novelId: number, currentContent: string, options?: {
    chapterSummaries?: string[]
    authorInstructions?: string
    selectedOutline?: string
    temperature?: number
  }) => {
    if (!currentContent.trim()) {
      ElMessage.warning('请先输入章节内容')
      return
    }

    continuationLoading.value = true
    continuationDialogVisible.value = true
    continuationResult.value = null

    try {
      const res = await continuationGenerate({
        novelId,
        currentContent,
        chapterSummaries: options?.chapterSummaries,
        authorInstructions: options?.authorInstructions,
        selectedOutline: options?.selectedOutline,
        temperature: options?.temperature ?? 0.7
      })
      continuationResult.value = res.data

      if (!res.data?.contentSafe) {
        ElMessage.warning('AI生成内容可能存在合规风险，请仔细审查')
      }
    } catch {
      ElMessage.error('续写生成失败，请稍后重试')
    } finally {
      continuationLoading.value = false
    }
  }

  /**
   * 文本润色
   */
  const handlePolish = async (text: string, options?: {
    aspects?: string[]
    customInstruction?: string
    preserveLength?: boolean
    generateLonger?: boolean
    novelId?: number
  }) => {
    if (!text.trim()) {
      ElMessage.warning('请先输入待润色文本')
      return
    }

    polishLoading.value = true
    polishDialogVisible.value = true
    polishResult.value = null

    try {
      const res = await polishText({
        text,
        aspects: options?.aspects || [PolishAspect.GRAMMAR, PolishAspect.STYLE, PolishAspect.COHERENCE, PolishAspect.DESCRIPTION, PolishAspect.DIALOGUE],
        customInstruction: options?.customInstruction,
        preserveLength: options?.preserveLength ?? true,
        generateLonger: options?.generateLonger,
        novelId: options?.novelId
      })
      polishResult.value = res.data

      if (!res.data?.contentSafe) {
        ElMessage.warning('润色内容可能存在合规风险，请仔细审查')
      }
    } catch {
      ElMessage.error('文本润色失败，请稍后重试')
    } finally {
      polishLoading.value = false
    }
  }

  const applySummary = (applyFn: (text: string) => void) => {
    if (aiResult.value) {
      const text = aiResult.value.replace(/\n/g, ' ').substring(0, 100)
      applyFn(text)
      aiDialogVisible.value = false
      ElMessage.success('已应用到章节标题')
    }
  }

  const copyAiResult = () => {
    if (aiResult.value) {
      navigator.clipboard.writeText(aiResult.value).then(() => {
        ElMessage.success('已复制到剪贴板')
      }).catch(() => {
        ElMessage.error('复制失败')
      })
    }
  }

  return {
    aiLoading,
    aiDialogVisible,
    aiDialogTitle,
    aiResult,
    aiResultType,
    handleAiContinue,
    handleAiSummary,
    applySummary,
    copyAiResult,

    // 续写引擎
    continuationLoading,
    continuationResult,
    continuationDialogVisible,
    handleContinuation,

    // 润色
    polishLoading,
    polishResult,
    polishDialogVisible,
    handlePolish
  }
}
