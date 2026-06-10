import request from '@/utils/request'
import type { NovelRankingVO, AuthorRankingVO } from '@/types'

/**
 * 小说收藏榜
 */
export function getNovelCollectRanking(limit?: number) {
  return request.get<NovelRankingVO>('/novel-server/visitor/ranking/novel/collect', { limit })
}

/**
 * 连载榜
 */
export function getNovelOngoingRanking(limit?: number) {
  return request.get<NovelRankingVO>('/novel-server/visitor/ranking/novel/ongoing', { limit })
}

/**
 * 最新更新榜
 */
export function getNovelLatestRanking(limit?: number) {
  return request.get<NovelRankingVO>('/novel-server/visitor/ranking/novel/latest', { limit })
}

/**
 * 新书榜
 */
export function getNovelNewRanking(limit?: number) {
  return request.get<NovelRankingVO>('/novel-server/visitor/ranking/novel/new', { limit })
}

/**
 * 作者高产榜
 */
export function getAuthorProductiveRanking(limit?: number) {
  return request.get<AuthorRankingVO>('/novel-server/visitor/ranking/author/productive', { limit })
}
