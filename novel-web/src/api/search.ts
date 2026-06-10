import request from '@/utils/request'
import type { PageResult, SearchDTO, NovelListVO, VisitorAuthorVO } from '@/types'

/**
 * ES搜索小说
 */
export function searchNovelsByES(data: SearchDTO) {
  return request.post<PageResult<NovelListVO>>('/search-server/common/novel/search', data)
}

/**
 * ES搜索作者
 */
export function searchAuthorsByES(data: SearchDTO) {
  return request.post<PageResult<VisitorAuthorVO>>('/search-server/common/author/search', data)
}

/**
 * 搜索建议（自动补全）
 */
export function getSearchSuggest(prefix: string) {
  return request.get<string[]>('/search-server/common/search/suggest', { prefix })
}

/**
 * 分类搜索（聚合统计）
 */
export function getSearchCategories(categoryType?: number) {
  return request.get<any>('/search-server/common/search/categories', { categoryType })
}
