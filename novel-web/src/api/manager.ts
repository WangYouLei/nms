import request from '@/utils/request'
import type { PageResult, ManagerVO, ManagerDTO, ManagerQueryDTO, DashboardOverviewVO, TrendVO, NovelRankingVO, AuthorRankingVO, NovelStatisticsVO, AuthorStatisticsVO, VisitorStatisticsVO } from '@/types'
import type { ManualAuditVO, ManualAuditQueryDTO, AiAuditResult } from '@/types/comment'
import type { ManualAuditDTO } from '@/types'

// ==================== 管理员认证 ====================

/**
 * 管理员登录 - 后端使用@RequestParam，参数在URL中
 * @returns Result<string> - data 为 JWT token 字符串
 */
export function managerLogin(account: string, password: string) {
  return request.postWithParams<string>('/manager-server/manager/login', { account, password })
}

/**
 * 管理员登出
 */
export function managerLogout() {
  return request.post('/manager-server/manager/logout')
}

// ==================== 管理员管理 ====================

/**
 * 添加管理员 - 后端使用@RequestBody
 */
export function addManager(data: ManagerDTO) {
  return request.post('/manager-server/manager/add', data)
}

/**
 * 删除管理员
 */
export function deleteManager(id: number) {
  return request.delete(`/manager-server/manager/delete/${id}`)
}

/**
 * 修改管理员信息 - 后端使用@RequestBody
 */
export function updateManager(data: ManagerDTO) {
  return request.put('/manager-server/manager/update', data)
}

/**
 * 多条件查询管理员 - 后端使用@RequestBody
 */
export function getManagerList(data: ManagerQueryDTO) {
  return request.post<ManagerVO[]>('/manager-server/manager/list', data)
}

/**
 * 分页查询管理员
 */
export function getManagerPage(params: { pageNum?: number; pageSize?: number }) {
  return request.get<PageResult<ManagerVO>>('/manager-server/manager/page', params)
}

/**
 * 修改管理员密码 - 后端使用@RequestParam
 */
export function updateManagerPassword(id: number, newPassword: string) {
  return request.postWithParams('/manager-server/manager/updatePassword', { id, newPassword })
}

/**
 * 获取管理员名称和头像
 */
export function getManagerNameAndAvatar(id: number) {
  return request.get<{ name: string; avatar: string }>(`/manager-server/manager/getNameAndAvatar/${id}`)
}

// ==================== 数据统计 ====================

/**
 * 获取概览数据
 */
export function getDashboardOverview() {
  return request.get<DashboardOverviewVO>('/manager-server/manager/dashboard/overview')
}

/**
 * 小说数量统计
 */
export function getNovelCountStatistics(groupBy: string) {
  return request.get<NovelStatisticsVO[]>('/manager-server/manager/dashboard/statistics/novel/count', { groupBy })
}

/**
 * 作者数量统计
 */
export function getAuthorCountStatistics() {
  return request.get<AuthorStatisticsVO[]>('/manager-server/manager/dashboard/statistics/author/count')
}

/**
 * 用户数量统计
 */
export function getVisitorCountStatistics() {
  return request.get<VisitorStatisticsVO[]>('/manager-server/manager/dashboard/statistics/visitor/count')
}

/**
 * 连载榜
 */
export function getNovelOngoingRanking(limit?: number) {
  return request.get<NovelRankingVO>('/manager-server/manager/dashboard/ranking/novel/ongoing', { limit })
}

/**
 * 作者高产榜
 */
export function getAuthorProductiveRanking(limit?: number) {
  return request.get<AuthorRankingVO>('/manager-server/manager/dashboard/ranking/author/productive', { limit })
}

/**
 * 小说收藏榜
 */
export function getNovelCollectRanking(limit?: number) {
  return request.get<NovelRankingVO>('/manager-server/manager/dashboard/ranking/novel/collect', { limit })
}

/**
 * 最新更新榜
 */
export function getNovelLatestRanking(limit?: number) {
  return request.get<NovelRankingVO>('/manager-server/manager/dashboard/ranking/novel/latest', { limit })
}

/**
 * 新书榜
 */
export function getNovelNewRanking(limit?: number) {
  return request.get<NovelRankingVO>('/manager-server/manager/dashboard/ranking/novel/new', { limit })
}

/**
 * 小说趋势统计
 */
export function getNovelTrend(params: { startDate: string; endDate: string; type?: string }) {
  return request.get<TrendVO[]>('/manager-server/manager/dashboard/statistics/novel/trend', params)
}

/**
 * 作者注册趋势
 */
export function getAuthorTrend(params: { startDate: string; endDate: string; type?: string }) {
  return request.get<TrendVO[]>('/manager-server/manager/dashboard/statistics/author/register', params)
}

/**
 * 用户注册趋势
 */
export function getVisitorTrend(params: { startDate: string; endDate: string; type?: string }) {
  return request.get<TrendVO[]>('/manager-server/manager/dashboard/statistics/visitor/register', params)
}

// ==================== 人工审核 ====================

/**
 * 获取待审核记录列表
 */
export function getPendingAuditList(pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<ManualAuditVO>>('/manager-server/manual-audit/pending', {
    pageNum,
    pageSize
  })
}

/**
 * 获取审核记录列表（分页查询）
 */
export function getManualAuditList(data: ManualAuditQueryDTO) {
  return request.post<PageResult<ManualAuditVO>>('/manager-server/manual-audit/list', data)
}

/**
 * 获取审核记录详情
 */
export function getManualAuditDetail(id: number) {
  return request.get<ManualAuditVO>(`/manager-server/manual-audit/detail/${id}`)
}

/**
 * 审核通过
 */
export function approveAudit(id: number, managerId: number, managerName: string) {
  return request.put(`/manager-server/manual-audit/approve/${id}`, {}, {
    params: {
      managerId,
      managerName
    }
  })
}

/**
 * 审核拒绝
 */
export function rejectAudit(id: number, refusalReason: string, managerId: number, managerName: string) {
  return request.put(`/manager-server/manual-audit/reject/${id}`, {}, {
    params: {
      refusalReason,
      managerId,
      managerName
    }
  })
}

/**
 * 执行审核（通过/拒绝）
 */
export function executeAudit(
  id: number, 
  result: number, 
  refusalReason: string | null, 
  managerId: number, 
  managerName: string
) {
  return request.put('/manager-server/manual-audit/execute', {}, {
    params: {
      id,
      result,
      refusalReason,
      managerId,
      managerName
    }
  })
}

/**
 * 批量审核通过
 */
export function batchApproveAudit(ids: number[], managerId: number, managerName: string) {
  return request.put('/manager-server/manual-audit/batch-approve', ids, {
    params: {
      managerId,
      managerName
    }
  })
}

/**
 * 批量审核拒绝
 */
export function batchRejectAudit(ids: number[], refusalReason: string, managerId: number, managerName: string) {
  return request.put('/manager-server/manual-audit/batch-reject', ids, {
    params: {
      refusalReason,
      managerId,
      managerName
    }
  })
}

/**
 * 获取审核统计信息
 */
export function getAuditStatistics() {
  return request.get<{
    total: number
    pending: number
    approved: number
    rejected: number
  }>('/manager-server/manual-audit/statistics')
}

/**
 * 删除审核记录
 */
export function deleteAuditRecord(id: number) {
  return request.delete(`/manager-server/manual-audit/delete/${id}`)
}

/**
 * 创建审核记录
 */
export function createManualAudit(data: ManualAuditDTO) {
  return request.post('/manager-server/manual-audit/create', data)
}

/**
 * 获取指定管理员的审核记录
 */
export function getManagerAuditList(managerId: number, pageNum: number = 1, pageSize: number = 10) {
  return request.get<PageResult<ManualAuditVO>>(`/manager-server/manual-audit/manager/${managerId}`, {
    pageNum,
    pageSize
  })
}

/**
 * 检查是否存在待审核记录
 */
export function checkPendingAudit(aimId: number, aimType: number) {
  return request.get<boolean>('/manager-server/manual-audit/check-pending', {
    aimId,
    aimType
  })
}

// ==================== AI审核 ====================

/**
 * AI审核文本内容
 */
export function aiAuditContent(content: string, aimId: number, aimType: number) {
  return request.post<AiAuditResult>('/ai-server/aiAudit/audit', {
    content,
    aimId,
    aimType
  })
}