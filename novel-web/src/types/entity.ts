/**
 * 小说实体
 */
export interface Novel {
  /** 主键ID */
  id: number
  /** 小说名称 */
  name: string
  /** 小说副名称 */
  subName?: string
  /** 小说标签 */
  tags?: string
  /** 小说简介 */
  introduction?: string
  /** 作者ID */
  authorId: number
  /** 作者名称 */
  authorName?: string
  /** 封面图片路径 */
  url?: string
  /** 小说章节数 */
  chapterCount?: number
  /** 是否完结 */
  isFinished: boolean
  /** 是否热门 */
  isHot: boolean
  /** 是否删除 */
  isDel: boolean
  /** 创建时间 */
  createTime: string
  /** 修改时间 */
  updateTime: string
}

/**
 * 小说章节实体
 */
export interface NovelChapter {
  /** 主键ID */
  id: number
  /** 小说ID */
  novelId: number
  /** 章节标题 */
  title: string
  /** 章节内容URL */
  contentUrl: string
  /** 章节顺序 */
  chapterOrder: number
  /** 创建时间 */
  createTime: string
  /** 修改时间 */
  updateTime: string
}

/**
 * 小说分类实体
 */
export interface NovelCategory {
  /** 主键ID */
  id: number
  /** 分类类型/名称 */
  type: string
  /** 频道：1-男频，2-女频 */
  category: number
  /** 是否热门 */
  isHot: number
  /** 创建时间 */
  createTime: string
  /** 修改时间 */
  updateTime: string
}

/**
 * 小说-分类关联实体
 */
export interface NovelCategoryRelation {
  /** 主键ID */
  id?: number
  /** 小说ID */
  novelId: number
  /** 分类ID */
  categoryId: number
}

/**
 * 作者实体
 */
export interface Author {
  /** 作者ID */
  id: number
  /** 作者昵称 */
  name: string
  /** 账号 */
  account: string
  /** 密码 */
  password?: string
  /** 邮箱 */
  email?: string
  /** 作者头像地址 */
  avatar?: string
  /** 等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者 */
  rank: number
  /** 作品数量 */
  novelCount?: number
  /** 是否删除 */
  isDel: boolean
  /** 创建时间 */
  createTime: string
  /** 修改时间 */
  updateTime: string
}

/**
 * 访客实体
 */
export interface Visitor {
  /** 主键ID */
  id: number
  /** 访问者名称 */
  name: string
  /** 头像地址 */
  avatar?: string
  /** 账号 */
  account: string
  /** 密码 */
  password?: string
  /** 邮箱 */
  email?: string
  /** VIP级别 */
  vipLevel: number
  /** 是否删除 */
  isDel: boolean
  /** 创建时间 */
  createTime: string
  /** 修改时间 */
  updateTime: string
}

/**
 * 管理员实体
 */
export interface Manager {
  /** 主键ID */
  id: number
  /** 昵称 */
  name?: string
  /** 账号 */
  account: string
  /** 密码 */
  password?: string
  /** 头像 */
  avatar?: string
  /** 创建者ID */
  createId: number
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}