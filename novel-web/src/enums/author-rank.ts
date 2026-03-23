/**
 * 作者等级枚举
 */
export enum AuthorRank {
  /** 执笔者 */
  SCRIBE = 1,
  /** 织梦师 */
  DREAMWEAVER = 2,
  /** 造界者 */
  WORLDMAKER = 3,
  /** 渡舟人 */
  FERRYMAN = 4,
  /** 燃灯者 */
  LAMPKEEPER = 5
}

/**
 * 作者等级名称
 */
export const AuthorRankLabels: Record<AuthorRank, string> = {
  [AuthorRank.SCRIBE]: '执笔者',
  [AuthorRank.DREAMWEAVER]: '织梦师',
  [AuthorRank.WORLDMAKER]: '造界者',
  [AuthorRank.FERRYMAN]: '渡舟人',
  [AuthorRank.LAMPKEEPER]: '燃灯者'
}

/**
 * 获取作者等级名称
 */
export function getAuthorRankName(rank: number): string {
  return AuthorRankLabels[rank as AuthorRank] || '未知'
}