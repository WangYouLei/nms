import request from '@/utils/request'
import type { VisitorReadingProgressVO } from '@/types'

/**
 * 更新阅读进度
 */
export function updateReadingProgress(novelId: number, chapterId: number, chapterOrder: number) {
  return request.post('/visitor-server/visitor/reading-progress/update', {}, {
    params: { novelId, chapterId, chapterOrder }
  })
}

/**
 * 获取某小说的阅读进度
 */
export function getReadingProgress(novelId: number) {
  return request.get<VisitorReadingProgressVO>(`/visitor-server/visitor/reading-progress/${novelId}`)
}

/**
 * 获取最近阅读列表
 */
export function getRecentReading() {
  return request.get<VisitorReadingProgressVO[]>('/visitor-server/visitor/reading-progress/recent')
}

/**
 * 删除阅读进度
 */
export function deleteReadingProgress(novelId: number) {
  return request.delete(`/visitor-server/visitor/reading-progress/${novelId}`)
}

/**
 * 获取小说在读人数
 */
export function getReadingCount(novelId: number) {
  return request.get<number>(`/visitor-server/visitor/reading-progress/count/${novelId}`)
}
