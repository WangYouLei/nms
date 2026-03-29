import request from '@/utils/request'
import type { PageResult } from '@/types'
import type { 
  CommentVO, 
  CommentDTO, 
  CommentQueryDTO,
  SensitiveWordVO,
  SensitiveWordDTO,
  SensitiveWordQueryDTO
} from '@/types/comment'

// ==================== 评论接口 ====================

/**
 * 发表评论
 */
export function addComment(data: CommentDTO) {
  return request.post<number>('/comment-server/comment/add', data)
}

/**
 * 删除评论
 */
export function deleteComment(commentId: number, userId: number, userType: number) {
  return request.delete(`/comment-server/comment/delete/${commentId}`, {
    userId,
    userType
  })
}

/**
 * 获取评论详情
 */
export function getCommentDetail(commentId: number) {
  return request.get<CommentVO>(`/comment-server/comment/detail/${commentId}`)
}

/**
 * 获取评论列表
 */
export function getCommentList(data: CommentQueryDTO) {
  return request.post<PageResult<CommentVO>>('/comment-server/comment/list', data)
}

/**
 * 获取某小说的评论
 */
export function getNovelComments(
  novelId: number, 
  pageNum: number = 1, 
  pageSize: number = 10
) {
  return request.get<PageResult<CommentVO>>(`/comment-server/comment/novel/${novelId}`, {
    pageNum,
    pageSize
  })
}

/**
 * 获取某小说/章节的评论（通用接口）
 */
export function getCommentsByTarget(
  targetType: number, 
  targetId: number, 
  pageNum: number = 1, 
  pageSize: number = 10
) {
  // 根据 targetType 选择不同的接口
  // targetType: 1-小说，2-章节
  if (targetType === 1) {
    // 小说评论
    return request.get<PageResult<CommentVO>>(`/comment-server/comment/novel/${targetId}`, {
      pageNum,
      pageSize
    })
  } else {
    // 章节评论（使用 list 接口）
    return request.post<PageResult<CommentVO>>('/comment-server/comment/list', {
      targetId,
      targetType,
      pageNum,
      pageSize,
      parentId: null  // 只查询一级评论
    })
  }
}

/**
 * 获取某条评论的回复列表
 */
export function getReplies(rootId: number, pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<CommentVO>>(`/comment-server/comment/replies/${rootId}`, {
    pageNum,
    pageSize
  })
}

/**
 * 获取我的评论
 */
export function getMyComments(userId: number, userType: number, pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<CommentVO>>('/comment-server/comment/my', {
    userId,
    userType,
    pageNum,
    pageSize
  })
}

/**
 * 获取小说的评论树（包含所有回复）
 */
export function getNovelCommentTree(
  novelId: number, 
  targetType?: number,
  pageNum: number = 1, 
  pageSize: number = 10
) {
  return request.get<PageResult<CommentVO>>(`/comment-server/comment/tree/${novelId}`, {
    targetType,
    pageNum,
    pageSize
  })
}

/**
 * 更新评论
 */
export function updateComment(data: CommentDTO) {
  return request.put('/comment-server/comment/update', data)
}

// ==================== 管理员接口 ====================

/**
 * 管理员分页查询评论列表
 */
export function getCommentManagePage(data: CommentQueryDTO) {
  return request.post<PageResult<CommentVO>>('/comment-server/manager/comment/page', data)
}

/**
 * 管理员获取评论详情
 */
export function getCommentManageDetail(id: number) {
  return request.get<CommentVO>(`/comment-server/manager/comment/detail/${id}`)
}

/**
 * 管理员删除评论
 */
export function managerDeleteComment(id: number) {
  return request.delete(`/comment-server/manager/comment/delete/${id}`)
}

/**
 * 管理员审核评论
 */
export function managerAuditComment(id: number, auditLevel: number) {
  return request.put(`/comment-server/manager/comment/audit/${id}`, {}, {
    params: { auditLevel }
  })
}

/**
 * 管理员批量删除评论
 */
export function managerBatchDeleteComment(ids: number[]) {
  return request.delete('/comment-server/manager/comment/batch-delete', ids as any)
}

/**
 * 管理员批量审核评论
 */
export function managerBatchAuditComment(ids: number[], auditLevel: number) {
  return request.put('/comment-server/manager/comment/batch-audit', ids as any, {
    params: { auditLevel }
  })
}

/**
 * 审核评论（普通接口）
 */
export function auditComment(commentId: number, auditLevel: number) {
  return request.put('/comment-server/comment/audit', {}, {
    params: {
      commentId,
      auditLevel
    }
  })
}

// ==================== 敏感词接口（common-server模块）====================

/**
 * 获取敏感词列表（分页）
 */
export function getSensitiveWordList(data: SensitiveWordQueryDTO) {
  return request.post<PageResult<SensitiveWordVO>>('/common-server/sensitive-word/list', data)
}

/**
 * 添加敏感词
 */
export function addSensitiveWord(data: SensitiveWordDTO) {
  return request.post('/common-server/sensitive-word/add', data)
}

/**
 * 更新敏感词
 */
export function updateSensitiveWord(data: SensitiveWordDTO) {
  return request.put('/common-server/sensitive-word/update', data)
}

/**
 * 删除敏感词
 */
export function deleteSensitiveWord(id: number) {
  return request.delete(`/common-server/sensitive-word/delete/${id}`)
}

/**
 * 启用/禁用敏感词
 */
export function updateSensitiveWordStatus(id: number, status: number) {
  return request.put(`/common-server/sensitive-word/status/${id}`, {}, {
    params: {
      status
    }
  })
}

/**
 * 检测文本中的敏感词
 */
export function detectSensitiveWords(content: string) {
  return request.post<Set<string>>('/common-server/sensitive-word/detect', { content })
}

/**
 * 过滤文本中的敏感词
 */
export function filterSensitiveWords(content: string, replacement: string = '*') {
  return request.post<string>('/common-server/sensitive-word/filter', { content }, {
    params: { replacement }
  })
}

/**
 * 刷新敏感词缓存
 */
export function refreshSensitiveWordCache() {
  return request.post('/common-server/sensitive-word/refresh-cache')
}