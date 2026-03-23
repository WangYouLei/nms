// API类型
export type { Result, PageResult, PaginationParams, CaptchaResult, LoginResult, UserInfo } from './api'

// 实体类型
export type { Novel, NovelChapter, NovelCategory, NovelCategoryRelation, Author, Visitor, Manager } from './entity'

// DTO类型
export type { NovelDTO, NovelSearchDTO, NovelChapterDTO, NovelCategoryDTO, NovelCategoryRelationDTO, AuthorDTO, AuthorRegisterDTO, VisitorDTO, VisitorRegisterDTO, VisitorDeleteDTO, ManagerDTO, ManagerQueryDTO, PasswordUpdateEmailDTO } from './dto'

// VO类型
export type { NovelDetailVO, NovelListVO, NovelChapterVO, NovelCategoryVO, AuthorVO, AuthorRankingVO, AuthorStatisticsVO, VisitorVO, VisitorStatisticsVO, ManagerVO, DashboardOverviewVO, TrendVO, NovelRankingVO, NovelStatisticsVO } from './vo'

// 评论类型
export type { CommentVO, CommentDTO, CommentQueryDTO, SensitiveWordVO, SensitiveWordDTO } from './comment'
export { CommentUserType, CommentTargetType, CommentStatus, SensitiveCategory, SensitiveLevel } from './comment'