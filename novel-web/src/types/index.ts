// API类型
export type { Result, PageResult, PaginationParams, CaptchaResult, LoginResult, UserInfo } from './api'

// 实体类型
export type { Novel, NovelChapter, NovelCategory, NovelCategoryRelation, Author, Visitor, Manager } from './entity'

// DTO类型
export type { NovelDTO, NovelSearchDTO, NovelChapterDTO, NovelCategoryDTO, NovelCategoryRelationDTO, AuthorDTO, AuthorRegisterDTO, VisitorDTO, VisitorRegisterDTO, VisitorDeleteDTO, ManagerDTO, ManagerQueryDTO, PasswordUpdateEmailDTO, VisitorFollowDTO } from './dto'

// VO类型
export type { NovelDetailVO, NovelListVO, NovelChapterVO, NovelCategoryVO, AuthorVO, VisitorAuthorVO, AuthorDetailVO, AuthorRankingVO, AuthorStatisticsVO, VisitorVO, VisitorStatisticsVO, ManagerVO, DashboardOverviewVO, TrendVO, NovelRankingVO, NovelStatisticsVO, VisitorCollectVO, VisitorFollowVO } from './vo'

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
  AiAuditResult
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