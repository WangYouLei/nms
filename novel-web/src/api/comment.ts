import request from '@/utils/request'
import type { PageResult } from '@/types'
import type { 
  CommentVO, 
  CommentDTO, 
  CommentQueryDTO,
  SensitiveWordVO,
  SensitiveWordDTO 
} from '@/types/comment'

// ==================== 评论接口 ====================

/**
 * 发表评论
 */
export function addComment(data: CommentDTO) {
  return request.post<number>('/comment-server/visitor/comment/add', data)
}

/**
 * 删除评论
 */
export function deleteComment(commentId: number, userId: number, userType: number) {
  return request.delete(`/comment-server/visitor/comment/delete/${commentId}`, {
    userId,
    userType
  })
}

/**
 * 点赞/取消点赞
 */
export function toggleLike(commentId: number, userId: number, userType: number) {
  return request.post<{ liked: boolean }>(`/comment-server/visitor/comment/like/${commentId}`, {}, {
    params: {
      userId,
      userType
    }
  })
}

/**
 * 获取评论详情
 */
export function getCommentDetail(commentId: number) {
  return request.get<CommentVO>(`/comment-server/common/comment/${commentId}`)
}

/**
 * 获取评论列表
 */
export function getCommentList(data: CommentQueryDTO) {
  return request.post<PageResult<CommentVO>>('/comment-server/common/comment/list', data)
}

/**
 * 获取某小说/章节的评论
 */
export function getCommentsByTarget(
  targetType: number, 
  targetId: number, 
  pageNum: number = 1, 
  pageSize: number = 10
) {
  return request.get<PageResult<CommentVO>>('/comment-server/common/comment/target', {
    targetType,
    targetId,
    pageNum,
    pageSize
  })
}

/**
 * 获取某条评论的回复列表
 */
export function getReplies(rootId: number, pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<CommentVO>>(`/comment-server/common/comment/replies/${rootId}`, {
    pageNum,
    pageSize
  })
}

/**
 * 获取我的评论
 */
export function getMyComments(userId: number, userType: number, pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<CommentVO>>('/comment-server/visitor/comment/my', {
    userId,
    userType,
    pageNum,
    pageSize
  })
}

// ==================== 管理员接口 ====================

/**
 * 管理员发表评论（官方评论）
 */
export function managerAddComment(data: CommentDTO) {
  return request.post<number>('/comment-server/manager/comment/add', data)
}

/**
 * 管理员删除评论
 */
export function managerDeleteComment(commentId: number) {
  return request.delete(`/comment-server/manager/comment/delete/${commentId}`)
}

/**
 * 获取待审核评论列表
 */
export function getPendingComments(pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<CommentVO>>('/comment-server/manager/comment/pending', {
    pageNum,
    pageSize
  })
}

/**
 * 审核评论
 */
export function auditComment(
  commentId: number, 
  approved: boolean, 
  reason: string, 
  auditorId: number, 
  auditorName: string
) {
  return request.post(`/comment-server/manager/comment/audit/${commentId}`, {}, {
    params: {
      approved,
      reason,
      auditorId,
      auditorName
    }
  })
}

// ==================== 敏感词接口 ====================

/**
 * 获取敏感词列表
 */
export function getSensitiveWordList(category?: number, level?: number) {
  return request.get<SensitiveWordVO[]>('/comment-server/manager/sensitive-word/list', {
    category,
    level
  })
}

/**
 * 添加敏感词
 */
export function addSensitiveWord(data: SensitiveWordDTO) {
  return request.post('/comment-server/manager/sensitive-word/add', data)
}

/**
 * 更新敏感词
 */
export function updateSensitiveWord(data: SensitiveWordDTO) {
  return request.put('/comment-server/manager/sensitive-word/update', data)
}

/**
 * 删除敏感词
 */
export function deleteSensitiveWord(id: number) {
  return request.delete(`/comment-server/manager/sensitive-word/delete/${id}`)
}

/**
 * 启用/禁用敏感词
 */
export function updateSensitiveWordStatus(id: number, status: number) {
  return request.put(`/comment-server/manager/sensitive-word/status/${id}`, {}, {
    params: {
      status
    }
  })
}