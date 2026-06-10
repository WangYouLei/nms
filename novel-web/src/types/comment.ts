/**
 * 评论VO
 */
export interface CommentVO {
  /** 评论ID */
  id: number
  /** 用户ID */
  userId: number
  /** 用户类型：1-访客，2-作者，3-管理员 */
  userType: number
  /** 用户类型名称 */
  userTypeName?: string
  /** 用户昵称 */
  userName: string
  /** 用户头像URL */
  userAvatar?: string
  /** 是否官方评论（管理员评论） */
  isOfficial: boolean
  /** 评论对象类型：1-小说，2-章节 */
  targetType: number
  /** 评论对象类型名称 */
  targetTypeName?: string
  /** 评论对象ID */
  targetId: number
  /** 小说ID */
  novelId: number
  /** 小说作者ID（用于判断评论者是否是该小说的作者） */
  novelAuthorId?: number
  /** 评论内容 */
  content: string
  /** 评论图片URL列表 */
  images: string[]
  /** 父评论ID */
  parentId?: number
  /** 回复的用户ID */
  replyUserId?: number
  /** 回复的用户昵称 */
  replyUserName?: string
  /** 根评论ID */
  rootId?: number
  /** 回复数 */
  replyCount: number
  /** 当前用户是否已点赞 */
  isLiked?: boolean
  /** 审核状态：0-待审核，1-审核通过，2-审核拒绝，3-人工审核中 */
  status: number
  /** 审核层级：0-未审核，1-本地过滤通过，2-人工审核通过 */
  auditLevel?: number
  /** 审核层级名称 */
  auditLevelName?: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime?: string
  /** 子评论列表 */
  replies?: CommentVO[]
}

/**
 * 评论DTO
 */
export interface CommentDTO {
  /** 评论ID（修改时需要） */
  id?: number
  /** 用户ID */
  userId: number
  /** 用户类型：1-访客，2-作者，3-管理员 */
  userType: number
  /** 用户昵称 */
  userName: string
  /** 用户头像URL */
  userAvatar?: string
  /** 评论对象类型：1-小说，2-章节 */
  targetType: number
  /** 评论对象ID */
  targetId: number
  /** 小说ID */
  novelId: number
  /** 评论内容 */
  content: string
  /** 评论图片URL列表 */
  images?: string[]
  /** 父评论ID（回复时需要） */
  parentId?: number
  /** 父评论的用户ID */
  replyUserId?: number
  /** 父评论的用户昵称 */
  replyUserName?: string
  /** 根评论ID */
  rootId?: number
}

/**
 * 评论查询DTO
 */
export interface CommentQueryDTO {
  /** 页码 */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
  /** 小说ID */
  novelId?: number
  /** 评论对象类型 */
  targetType?: number
  /** 评论对象ID */
  targetId?: number
  /** 用户ID */
  userId?: number
  /** 用户类型 */
  userType?: number
  /** 用户昵称（模糊查询） */
  userName?: string
  /** 评论内容（模糊查询） */
  content?: string
  /** 审核状态 */
  status?: number
  /** 审核层级：0-未审核，1-本地过滤通过，2-人工审核通过 */
  auditLevel?: number
  /** 父评论ID */
  parentId?: number
  /** 根评论ID */
  rootId?: number
  /** 是否只查询一级评论 */
  onlyRoot?: boolean
}

/**
 * 敏感词VO
 */
export interface SensitiveWordVO {
  /** 敏感词ID */
  id: number
  /** 敏感词 */
  word: string
  /** 敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他 */
  category: number
  /** 敏感词类别名称 */
  categoryName: string
  /** 敏感等级：1-低（需人工审核），2-高（直接拒绝） */
  level: number
  /** 敏感等级名称 */
  levelName: string
  /** 状态：0-禁用，1-启用 */
  status: number
  /** 状态名称 */
  statusName: string
  /** 来源：1-系统内置，2-管理员添加 */
  source: number
  /** 来源名称 */
  sourceName: string
  /** 创建人ID */
  creatorId?: number
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/**
 * 敏感词DTO
 */
export interface SensitiveWordDTO {
  /** 敏感词ID（修改时需要） */
  id?: number
  /** 敏感词 */
  word: string
  /** 敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他 */
  category: number
  /** 敏感等级：1-低（需人工审核），2-高（直接拒绝） */
  level: number
  /** 状态：0-禁用，1-启用 */
  status?: number
}

/**
 * 敏感词查询DTO
 */
export interface SensitiveWordQueryDTO {
  /** 页码 */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
  /** 敏感词（模糊查询） */
  word?: string
  /** 敏感词类别 */
  category?: number
  /** 敏感等级 */
  level?: number
  /** 状态 */
  status?: number
  /** 来源 */
  source?: number
}

/**
 * 人工审核VO
 */
export interface ManualAuditVO {
  /** 审核记录ID */
  id: number
  /** 审核目标对象ID */
  aimId: number
  /** 审核目标对象类型：1-评论，2-小说，3-章节 */
  aimType: number
  /** 审核目标对象类型名称 */
  aimTypeName: string
  /** 人工审核结果：0-待审核，1-通过，2-拒绝 */
  result: number
  /** 审核结果名称 */
  resultName: string
  /** 拒绝理由 */
  refusalReason?: string
  /** AI审核结果意见 */
  aiResult?: string
  /** 审核管理员ID */
  managerId?: number
  /** 审核管理员昵称 */
  managerName?: string
  /** 创建时间 */
  createTime: string
  /** 初审核时间 */
  firstAuditTime?: string
  /** 修改时间 */
  updateTime: string
  /** 审核目标内容 */
  aimContent?: string
  /** 提交者ID */
  submitterId?: number
  /** 提交者名称 */
  submitterName?: string
}

/**
 * 人工审核查询DTO
 */
export interface ManualAuditQueryDTO {
  /** 页码 */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
  /** 审核目标对象ID */
  aimId?: number
  /** 审核目标对象类型：1-评论，2-小说，3-章节 */
  aimType?: number
  /** 审核结果：0-待审核，1-通过，2-拒绝 */
  result?: number
  /** 审核管理员ID */
  managerId?: number
}

/**
 * AI审核结果
 */
export interface AiAuditResult {
  /** 审核结果 */
  auditResult: {
    /** 是否通过审核 */
    passed: boolean
    /** 审核结果：1-通过，2-需人工审核，3-拒绝 */
    result: number
    /** 审核结果描述 */
    resultDesc: string
    /** 检测到的敏感词列表 */
    sensitiveWords?: string[]
    /** 最高敏感等级 */
    maxLevel?: number
    /** 敏感词数量 */
    wordCount?: number
  }
  /** AI审核意见 */
  aiResult?: string
}

/**
 * 审核结果VO
 */
export interface AuditResultVO {
  passed: boolean
  result: number
  resultDesc: string
  sensitiveWords?: string[]
  maxLevel?: number
  wordCount?: number
}

/**
 * 用户类型枚举
 */
export enum CommentUserType {
  VISITOR = 1,
  AUTHOR = 2,
  MANAGER = 3
}

/**
 * 评论对象类型枚举
 */
export enum CommentTargetType {
  NOVEL = 1,
  CHAPTER = 2
}

/**
 * 审核状态枚举
 */
export enum CommentStatus {
  PENDING = 0,
  APPROVED = 1,
  REJECTED = 2,
  MANUAL_REVIEW = 3
}

/**
 * 审核层级枚举
 */
export enum AuditLevel {
  /** 未审核 */
  UNAUDITED = 0,
  /** 本地过滤通过 */
  LOCAL_FILTER_PASSED = 1,
  /** 人工审核通过 */
  MANUAL_AUDIT_PASSED = 2
}

/**
 * 敏感词类别枚举
 */
export enum SensitiveCategory {
  POLITICS = 1,
  PORNOGRAPHY = 2,
  VIOLENCE = 3,
  ADVERTISEMENT = 4,
  OTHER = 5
}

/**
 * 敏感词等级枚举（更新为2级）
 */
export enum SensitiveLevel {
  /** 低（需人工审核） */
  LOW = 1,
  /** 高（直接拒绝） */
  HIGH = 2
}

/**
 * 审核目标对象类型枚举
 */
export enum AuditAimType {
  COMMENT = 1,
  NOVEL = 2,
  CHAPTER = 3
}

/**
 * 审核结果枚举
 */
export enum AuditResult {
  /** 待审核 */
  PENDING = 0,
  /** 通过 */
  APPROVED = 1,
  /** 拒绝 */
  REJECTED = 2
}