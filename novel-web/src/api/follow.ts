import request from '@/utils/request'
import type { PageResult } from '@/types'
import type { VisitorFollowDTO, VisitorFollowVO } from '@/types'

/**
 * 关注作者
 */
export function addFollow(data: VisitorFollowDTO) {
  return request.post('/visitor-server/visitor/follow/add', data)
}

/**
 * 取消关注
 */
export function removeFollow(authorId: number, visitorId: number) {
  return request.delete(`/visitor-server/visitor/follow/remove/${authorId}`, { visitorId })
}

/**
 * 检查是否已关注
 */
export function checkFollow(authorId: number, visitorId: number) {
  return request.get<boolean>('/visitor-server/visitor/follow/check/' + authorId, { visitorId })
}

/**
 * 获取我的关注列表
 */
export function getMyFollows(visitorId: number, pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<VisitorFollowVO>>('/visitor-server/visitor/follow/list', {
    visitorId,
    pageNum,
    pageSize
  })
}

/**
 * 获取我的关注数量
 */
export function getMyFollowCount(visitorId: number) {
  return request.get<number>('/visitor-server/visitor/follow/count', { visitorId })
}

/**
 * 获取作者的粉丝数量
 */
export function getFollowerCount(authorId: number) {
  return request.get<number>(`/visitor-server/visitor/follow/followerCount/${authorId}`)
}