import request from '@/utils/request'
import type { VisitorCollectVO } from '@/types'

/**
 * 添加收藏
 */
export function addCollect(novelId: number) {
  return request.post(`/visitor-server/visitor/collect/add/${novelId}`)
}

/**
 * 取消收藏
 */
export function removeCollect(novelId: number) {
  return request.delete(`/visitor-server/visitor/collect/remove/${novelId}`)
}

/**
 * 获取收藏列表
 */
export function getCollectList() {
  return request.get<VisitorCollectVO[]>('/visitor-server/visitor/collect/list')
}

/**
 * 检查是否已收藏
 */
export function checkCollect(novelId: number) {
  return request.get<boolean>(`/visitor-server/visitor/collect/check/${novelId}`)
}

/**
 * 获取收藏数量
 */
export function getCollectCount() {
  return request.get<number>('/visitor-server/visitor/collect/count')
}