import request from '@/utils/request'
import type { PageResult, ManagerVO, ManagerDTO, ManagerQueryDTO, DashboardOverviewVO, TrendVO, NovelRankingVO, AuthorRankingVO, NovelStatisticsVO, AuthorStatisticsVO, VisitorStatisticsVO } from '@/types'

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
  return request.get<NovelRankingVO[]>('/manager-server/manager/dashboard/ranking/novel/ongoing', { limit })
}

/**
 * 作者高产榜
 */
export function getAuthorProductiveRanking(limit?: number) {
  return request.get<AuthorRankingVO[]>('/manager-server/manager/dashboard/ranking/author/productive', { limit })
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