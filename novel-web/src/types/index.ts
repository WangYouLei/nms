// API类型
export type { Result, PageResult, PaginationParams, CaptchaResult, LoginResult, UserInfo } from './api'

// 实体类型
export type { Novel, NovelChapter, NovelCategory, NovelCategoryRelation, Author, Visitor, Manager } from './entity'

// DTO类型
export type { NovelDTO, NovelSearchDTO, SearchDTO, NovelChapterDTO, NovelCategoryDTO, NovelCategoryRelationDTO, AuthorDTO, AuthorRegisterDTO, VisitorDTO, VisitorRegisterDTO, VisitorDeleteDTO, ManagerDTO, ManagerQueryDTO, PasswordUpdateEmailDTO, ManualAuditDTO, VisitorFollowDTO } from './dto'

// VO类型
export type { NovelDetailVO, NovelListVO, NovelChapterVO, NovelCategoryVO, AuthorVO, VisitorAuthorVO, AuthorDetailVO, AuthorRankingVO, AuthorRankingItem, AuthorStatisticsVO, VisitorVO, VisitorStatisticsVO, ManagerVO, DashboardOverviewVO, TrendVO, NovelRankingVO, NovelRankingItem, NovelStatisticsVO, VisitorCollectVO, VisitorFollowVO, VisitorReadingProgressVO } from './vo'

// 评论类型
export type { 
  CommentVO, 
  CommentDTO, 
  CommentQueryDTO, 
  SensitiveWordVO, 
  SensitiveWordDTO, 
  SensitiveWordQueryDTO,
  ManualAuditVO,
  ManualAuditQueryDTO,
  AiAuditResult,
  AuditResultVO
} from './comment'
export { 
  CommentUserType, 
  CommentTargetType, 
  CommentStatus, 
  SensitiveCategory, 
  SensitiveLevel,
  AuditAimType,
  AuditResult
} from './comment'

// AI写作助手类型
export type { AiWritingDTO, AiWritingVO, StyleSummaryVO, ContinuationPlanDTO, ContinuationPlanVO, ContinuationGenerateDTO, ContinuationGenerateVO, KnowledgeItemRef, PolishRequestDTO, PolishResultVO, PolishChangeItem, KnowledgeItemVO, KnowledgeExtractDTO, KnowledgeExtractVO, KnowledgeExtractItemSummary, KnowledgeExtractBatchDTO } from './ai-writing'
export { AiWritingType, KnowledgeItemStatus, PolishAspect } from './ai-writing'