// 公共服务API
export * from './common'

// 小说相关API
export * from './novel'

// 访客相关API
export * from './visitor'

// 作者相关API
export * from './author'

// 管理员相关API
export * from './manager'

// 关注相关API
export * from './follow'

// 收藏相关API
export * from './collect'

// 评论相关API
export * from './comment'

// 文件相关API
export * from './file'

// 排行榜API（不通过barrel export，直接从 @/api/ranking 导入，避免与manager同名冲突）
// export * from './ranking'

// 搜索API
export * from './search'

// 阅读进度API
export * from './reading-progress'

// AI写作助手API
export * from './ai-writing'