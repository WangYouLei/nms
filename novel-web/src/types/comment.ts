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
  /** 用户昵称 */
  userName: string
  /** 是否官方评论（管理员评论） */
  isOfficial: boolean
  /** 评论对象类型：1-小说，2-章节 */
  targetType: number
  /** 评论对象ID */
  targetId: number
  /** 小说ID */
  novelId: number
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
  /** 点赞数 */
  likeCount: number
  /** 回复数 */
  replyCount: number
  /** 当前用户是否已点赞 */
  isLiked?: boolean
  /** 审核状态：0-待审核，1-审核通过，2-审核拒绝，3-人工审核中 */
  status: number
  /** 创建时间 */
  createTime: string
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
  /** 审核状态 */
  status?: number
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
  /** 敏感词类别描述 */
  categoryDesc: string
  /** 敏感等级：1-低，2-中，3-高 */
  level: number
  /** 敏感等级描述 */
  levelDesc: string
  /** 替换字符 */
  replacement?: string
  /** 状态：0-禁用，1-启用 */
  status: number
  /** 来源：1-系统内置，2-管理员添加 */
  source: number
  /** 来源描述 */
  sourceDesc: string
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
  /** 敏感词类别 */
  category: number
  /** 敏感等级 */
  level: number
  /** 替换字符 */
  replacement?: string
  /** 状态 */
  status?: number
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
 * 敏感词等级枚举
 */
export enum SensitiveLevel {
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3
}