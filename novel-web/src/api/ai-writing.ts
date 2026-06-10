import request from '@/utils/request'
import type {
  AiWritingDTO,
  AiWritingVO,
  StyleSummaryVO,
  ContinuationPlanDTO,
  ContinuationPlanVO,
  ContinuationGenerateDTO,
  ContinuationGenerateVO,
  PolishRequestDTO,
  PolishResultVO,
  KnowledgeItemVO,
  KnowledgeExtractDTO,
  KnowledgeExtractVO,
  KnowledgeExtractBatchDTO
} from '@/types'

const AI_BASE = '/ai-server'

// ========== AI写作助手 ==========

export function aiWritingAssist(data: AiWritingDTO) {
  return request.post<AiWritingVO>(`${AI_BASE}/aiWriting/assist`, data)
}

// ========== 续写引擎 ==========

export function continuationPlan(data: ContinuationPlanDTO) {
  return request.post<ContinuationPlanVO>(`${AI_BASE}/continuation/plan`, data)
}

export function continuationGenerate(data: ContinuationGenerateDTO) {
  return request.post<ContinuationGenerateVO>(`${AI_BASE}/continuation/generate`, data)
}

// ========== 文本润色 ==========

export function polishText(data: PolishRequestDTO) {
  return request.post<PolishResultVO>(`${AI_BASE}/polishing/polish`, data)
}

// ========== 写作风格总结 ==========

export function getStyleSummary(novelId: number) {
  return request.get<StyleSummaryVO>(`${AI_BASE}/knowledge/style-summary`, { novelId })
}

export function refreshStyleSummary(novelId: number, chapterSamples?: string) {
  return request.post<StyleSummaryVO>(`${AI_BASE}/knowledge/style-summary/refresh`, { novelId, chapterSamples })
}

export function updateStyleSummary(novelId: number, styleText: string) {
  return request.put<StyleSummaryVO>(`${AI_BASE}/knowledge/style-summary`, { novelId, styleText })
}

// ========== 知识库 ==========

export function getKnowledgeItems(novelId: number, itemType?: string, minStatus?: number) {
  return request.get<KnowledgeItemVO[]>(`${AI_BASE}/knowledge/items`, {
    novelId,
    itemType,
    minStatus
  })
}

export function createKnowledgeItem(data: Partial<KnowledgeItemVO>) {
  return request.post<KnowledgeItemVO>(`${AI_BASE}/knowledge/item`, data)
}

export function updateKnowledgeItem(id: number, data: Partial<KnowledgeItemVO>) {
  return request.put<KnowledgeItemVO>(`${AI_BASE}/knowledge/item/${id}`, data)
}

export function updateKnowledgeItemStatus(id: number, status: number) {
  return request.patch<KnowledgeItemVO>(`${AI_BASE}/knowledge/item/${id}/status`, { status })
}

export function deleteKnowledgeItem(id: number) {
  return request.delete(`${AI_BASE}/knowledge/item/${id}`)
}

// ========== 知识提取 ==========

export function extractKnowledge(data: KnowledgeExtractDTO) {
  return request.post<KnowledgeExtractVO>(`${AI_BASE}/knowledge/extract`, data)
}

export function extractKnowledgeBatch(data: KnowledgeExtractBatchDTO) {
  return request.post(`${AI_BASE}/knowledge/extract-batch`, data)
}
