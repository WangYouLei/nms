import type { NovelCategory } from './entity'

/**
 * 小说详情VO
 */
export interface NovelDetailVO {
  /** 小说ID */
  id: number
  /** 小说名称 */
  name: string
  /** 小说副名称 */
  subName?: string
  /** 小说标签 */
  tags?: string
  /** 小说简介 */
  introduction?: string
  /** 封面图片 */
  url?: string
  /** 作者ID */
  authorId?: number
  /** 作者名称 */
  authorName: string
  /** 作者头像 */
  authorAvatar?: string
  /** 小说章节数 */
  chapterCount: number
  /** 是否完结 */
  isFinished: boolean
  /** 是否热门 */
  isHot: boolean
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
  /** 分类信息列表 */
  categories?: NovelCategoryVO[]
}

/**
 * 小说列表VO
 */
export interface NovelListVO {
  /** 小说ID */
  id: number
  /** 小说名称 */
  name: string
  /** 小说副名称 */
  subName?: string
  /** 小说标签 */
  tags?: string
  /** 封面图片 */
  url?: string
  /** 作者名称 */
  authorName: string
  /** 作者头像 */
  authorAvatar?: string
  /** 小说章节数 */
  chapterCount: number
  /** 是否完结 */
  isFinished: boolean
  /** 是否热门 */
  isHot: boolean
  /** 更新时间 */
  updateTime: string
  /** 分类名称 */
  categoryName?: string
}

/**
 * 小说章节VO
 */
export interface NovelChapterVO {
  /** 主键ID */
  id: number
  /** 小说ID */
  novelId: number
  /** 章节标题 */
  title: string
  /** 章节内容URL */
  contentUrl: string
  /** 章节内容（仅在获取章节内容时返回） */
  content?: string
  /** 章节顺序 */
  chapterOrder: number
  /** 创建时间 */
  createTime: string
  /** 修改时间 */
  updateTime: string
}

/**
 * 小说分类VO
 */
export type NovelCategoryVO = NovelCategory

/**
 * 作者信息VO
 */
export interface AuthorVO {
  /** 作者ID */
  id: number
  /** 作者昵称 */
  name: string
  /** 账号 */
  account: string
  /** 邮箱 */
  email?: string
  /** 作者头像地址 */
  avatar?: string
  /** 等级 */
  rank: number
  /** 简介 */
  introduction?: string
  /** 作品数量 */
  novelCount: number
  /** 章节数量 */
  chapterCount?: number
  /** 创建时间 */
  createTime: string
}

/**
 * 作者公开信息VO（访客端）
 * 不包含敏感信息
 */
export interface VisitorAuthorVO {
  /** 作者ID */
  id: number
  /** 作者昵称 */
  name: string
  /** 作者头像 */
  avatar?: string
  /** 等级 */
  rank: number
  /** 作品简介 */
  introduction?: string
  /** 作品数量 */
  novelCount: number
}

/**
 * 作者排行榜VO
 */
export interface AuthorRankingVO {
  /** 作者ID */
  authorId: number
  /** 作者名称 */
  authorName: string
  /** 作品数量 */
  novelCount: number
  /** 作者等级 */
  rank: number
}

/**
 * 作者统计VO
 */
export interface AuthorStatisticsVO {
  /** 等级 */
  rank: number
  /** 等级名称 */
  rankName: string
  /** 数量 */
  count: number
}

/**
 * 访客信息VO
 */
export interface VisitorVO {
  /** 主键ID */
  id: number
  /** 访问者名称 */
  name: string
  /** 头像地址 */
  avatar?: string
  /** 账号 */
  account: string
  /** 邮箱 */
  email?: string
  /** VIP级别 */
  vipLevel: number
  /** 创建时间 */
  createTime: string
}

/**
 * 访客统计VO
 */
export interface VisitorStatisticsVO {
  /** VIP级别 */
  vipLevel: number
  /** 级别名称 */
  vipName: string
  /** 数量 */
  count: number
}

/**
 * 管理员信息VO
 */
export interface ManagerVO {
  /** 主键ID */
  id: number
  /** 昵称 */
  name?: string
  /** 账号 */
  account: string
  /** 头像 */
  avatar?: string
  /** 创建者ID */
  createId: number
  /** 创建时间 */
  createTime: string
}

/**
 * 数据概览VO
 */
export interface DashboardOverviewVO {
  /** 小说总数 */
  novelCount: number
  /** 作者总数 */
  authorCount: number
  /** 用户总数 */
  visitorCount: number
  /** 分类总数 */
  categoryCount: number
  /** 今日新增小说 */
  todayNewNovels: number
  /** 今日新增作者 */
  todayNewAuthors: number
  /** 今日新增用户 */
  todayNewVisitors: number
  /** 热门小说数 */
  hotNovelCount: number
  /** 完结小说数 */
  finishedNovelCount: number
}

/**
 * 趋势统计VO
 */
export interface TrendVO {
  /** 时间标签 */
  label: string
  /** 数量 */
  count: number
}

/**
 * 小说排行榜VO
 */
export interface NovelRankingVO {
  /** 小说ID */
  novelId: number
  /** 小说名称 */
  novelName: string
  /** 作者名称 */
  authorName: string
  /** 章节数量 */
  chapterCount: number
  /** 是否完结 */
  isFinished: boolean
}

/**
 * 小说统计VO
 */
export interface NovelStatisticsVO {
  /** 分类/状态名称 */
  name: string
  /** 数量 */
  count: number
}