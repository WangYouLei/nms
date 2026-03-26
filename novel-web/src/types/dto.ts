import type { PaginationParams } from './api'

/**
 * 小说DTO - 用于新增/修改小说
 */
export interface NovelDTO {
  /** 主键ID（修改时需要） */
  id?: number
  /** 小说名称 */
  name: string
  /** 小说副名称 */
  subName?: string
  /** 小说标签 */
  tags?: string
  /** 小说简介 */
  introduction?: string
  /** 作者名称 */
  authorName?: string
  /** 作者ID */
  authorId?: number
  /** 封面图片路径 */
  url?: string
  /** 小说章节数 */
  chapterCount?: number
  /** 是否完结 */
  isFinished?: boolean
}

/**
 * 小说搜索DTO
 */
export interface NovelSearchDTO extends PaginationParams {
  /** 搜索关键词 */
  keyword?: string
  /** 小说名称 */
  name?: string
  /** 小说副名称 */
  subName?: string
  /** 作者ID */
  authorId?: number
  /** 是否删除 */
  isDel?: boolean
  /** 是否热门 */
  isHot?: boolean
  /** 是否完结 */
  isFinished?: boolean
  /** 排序方式：update-最新更新，collect-收藏最多，word-字数最多 */
  sortBy?: string
}

/**
 * 小说章节DTO
 */
export interface NovelChapterDTO {
  /** 主键ID */
  id?: number
  /** 小说ID */
  novelId: number
  /** 章节标题 */
  title: string
  /** 章节内容URL */
  contentUrl?: string
  /** 章节顺序 */
  chapterOrder?: number
}

/**
 * 小说分类DTO
 */
export interface NovelCategoryDTO {
  /** 主键ID */
  id?: number
  /** 分类类型/名称 */
  type: string
  /** 频道：1-男频，2-女频 */
  category: number
  /** 是否热门 */
  isHot?: number
}

/**
 * 小说分类关联DTO
 */
export interface NovelCategoryRelationDTO {
  /** 小说ID */
  novelId: number
  /** 分类ID列表 */
  categoryIds: number[]
}

/**
 * 作者DTO
 */
export interface AuthorDTO {
  /** 作者ID */
  id: number
  /** 作者昵称 */
  name?: string
  /** 邮箱 */
  email?: string
  /** 作者头像地址 */
  avatar?: string
}

/**
 * 作者注册DTO
 */
export interface AuthorRegisterDTO {
  /** 作者昵称 */
  name: string
  /** 账号 */
  account: string
  /** 密码 */
  password: string
  /** 邮箱 */
  email: string
  /** 验证码 */
  code: string
  /** 验证码token */
  token: string
}

/**
 * 访客DTO
 */
export interface VisitorDTO {
  /** 访客ID */
  id: number
  /** 访问者名称 */
  name?: string
  /** 头像地址 */
  avatar?: string
  /** 邮箱 */
  email?: string
}

/**
 * 访客注册DTO
 */
export interface VisitorRegisterDTO {
  /** 访问者名称 */
  name: string
  /** 账号 */
  account: string
  /** 密码 */
  password: string
  /** 邮箱 */
  email: string
  /** 验证码 */
  code: string
  /** 验证码token */
  token: string
}

/**
 * 访客删除DTO
 */
export interface VisitorDeleteDTO {
  /** 访客ID */
  id: number
  /** 邮箱 */
  email: string
  /** 验证码 */
  code: string
}

/**
 * 管理员DTO
 */
export interface ManagerDTO {
  /** 主键ID */
  id?: number
  /** 昵称 */
  name?: string
  /** 账号 */
  account: string
  /** 密码 */
  password?: string
  /** 头像 */
  avatar?: string
}

/**
 * 管理员查询DTO
 */
export interface ManagerQueryDTO {
  /** 管理员ID */
  id?: number
  /** 姓名 */
  name?: string
  /** 账号 */
  account?: string
}

/**
 * 密码修改DTO
 */
export interface PasswordUpdateEmailDTO {
  /** 用户ID（已登录用户修改密码时使用） */
  id?: number
  /** 账号（忘记密码场景使用，与id二选一） */
  account?: string
  /** 邮箱 */
  email: string
  /** 验证码 */
  code: string
  /** 新密码 */
  newPassword: string
}

/**
 * 访客关注作者DTO
 */
export interface VisitorFollowDTO {
  /** 访客ID */
  visitorId: number
  /** 作者ID */
  authorId: number
  /** 作者名称 */
  authorName: string
  /** 作者头像URL */
  authorAvatar?: string
  /** 作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者） */
  authorRank?: number
}