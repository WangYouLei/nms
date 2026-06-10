/**
 * 写作风格总结响应
 */
export interface StyleSummaryVO {
  novel_id: number
  style_text: string
  last_summarized_chapter: number
  version: number
  exists: boolean
}

/**
 * AI写作助手请求DTO
 */
export interface AiWritingDTO {
  /** 功能类型：1-续写建议，2-章节摘要，3-角色一致性检查，4-标题/简介优化 */
  type: AiWritingType
  /** 当前章节内容 */
  content?: string
  /** 前文上下文 */
  context?: string
  /** 小说标题（用于标题优化） */
  title?: string
  /** 小说简介（用于简介优化） */
  introduction?: string
}

/**
 * AI写作助手响应VO
 */
export interface AiWritingVO {
  /** 功能类型 */
  type: AiWritingType
  /** AI生成结果 */
  result: string
  /** 备选标题列表（仅type=4时返回） */
  titleOptions?: string[]
  /** 优化后的简介（仅type=4时返回） */
  optimizedIntroduction?: string
}

/**
 * AI写作助手功能类型枚举
 */
export enum AiWritingType {
  /** 续写建议 */
  CONTINUE = 1,
  /** 章节摘要 */
  SUMMARY = 2,
  /** 角色一致性检查 */
  CHARACTER_CHECK = 3,
  /** 标题/简介优化 */
  TITLE_OPTIMIZE = 4
}

// ========== 续写引擎 ==========

/**
 * 续写大纲规划请求
 */
export interface ContinuationPlanDTO {
  novelId: number
  currentContent: string
  chapterSummaries?: string[]
  authorInstructions?: string
}

/**
 * 续写大纲规划响应
 */
export interface ContinuationPlanVO {
  outlines: string[]
}

/**
 * 续写生成请求
 */
export interface ContinuationGenerateDTO {
  novelId: number
  currentContent: string
  chapterSummaries?: string[]
  authorInstructions?: string
  selectedOutlineIndex?: number
  selectedOutline?: string
  temperature?: number
  maxTokens?: number
}

/**
 * 续写生成响应
 */
export interface ContinuationGenerateVO {
  continuationText: string
  outlines: string[]
  usedKnowledge: KnowledgeItemRef[]
  qualityScore: number
  warnings: string[]
  contentSafe: boolean
  contentRiskScore: number
  contentRiskLevel: string
  contentIssues: string[]
}

/**
 * 续写引用的知识项
 */
export interface KnowledgeItemRef {
  id: number
  itemType: string
  name: string
  summary: string
  relevanceScore?: number
}

// ========== 文本润色 ==========

/**
 * 文本润色请求
 */
export interface PolishRequestDTO {
  text: string
  aspects?: string[]
  customInstruction?: string
  preserveLength?: boolean
  generateLonger?: boolean
  temperature?: number
  novelId?: number
}

/**
 * 文本润色响应
 */
export interface PolishResultVO {
  polishedText: string
  changes: PolishChangeItem[]
  summary: string
  contentSafe: boolean
  contentRiskScore: number
  contentRiskLevel: string
  contentIssues: string[]
}

/**
 * 润色变更项
 */
export interface PolishChangeItem {
  type: string
  original: string
  polished: string
  description: string
}

// ========== 知识库 ==========

/**
 * 知识项
 */
export interface KnowledgeItemVO {
  id: number
  novelId: number
  itemType: string
  name: string
  content: string
  summary: string
  sourceChapterId?: number
  sourceChapterOrder?: number
  confidence: number
  version: number
  status: number
  createdAt: string
  updatedAt: string
}

/**
 * 知识提取请求
 */
export interface KnowledgeExtractDTO {
  novelId: number
  chapterId: number
  chapterOrder: number
  chapterText: string
  novelInfo?: string
}

/**
 * 知识提取响应
 */
export interface KnowledgeExtractVO {
  extractedCount: number
  newItems: KnowledgeExtractItemSummary[]
  updatedItems: KnowledgeExtractItemSummary[]
}

/**
 * 知识提取项摘要
 */
export interface KnowledgeExtractItemSummary {
  id: number
  itemType: string
  name: string
  summary: string
}

/**
 * 批量知识提取请求
 */
export interface KnowledgeExtractBatchDTO {
  novelId: number
  chapters: {
    chapter_id: number
    chapter_order: number
    text: string
    novel_info?: string
  }[]
}

/**
 * 知识项状态
 */
export enum KnowledgeItemStatus {
  /** 拒绝 */
  REJECTED = -1,
  /** 待确认 */
  PENDING = 0,
  /** 已确认 */
  CONFIRMED = 1,
  /** 已修改 */
  MODIFIED = 2
}

/**
 * 润色维度
 */
export enum PolishAspect {
  /** 语法修正 */
  GRAMMAR = 'grammar',
  /** 风格润色 */
  STYLE = 'style',
  /** 连贯性改善 */
  COHERENCE = 'coherence',
  /** 描写增强 */
  DESCRIPTION = 'description',
  /** 对话优化 */
  DIALOGUE = 'dialogue',
  /** 自定义 */
  CUSTOM = 'custom'
}
