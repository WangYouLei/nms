import request from '@/utils/request'
import type { VisitorVO, VisitorDTO, VisitorRegisterDTO, VisitorDeleteDTO, PasswordUpdateEmailDTO } from '@/types'

// ==================== 访客认证 ====================

/**
 * 访客登录 - 后端使用@RequestParam，参数在URL中
 * @returns Result<string> - data 为 JWT token 字符串
 */
export function visitorLogin(account: string, password: string) {
  return request.postWithParams<string>('/visitor-server/visitor/login', { account, password })
}

/**
 * 访客退出登录
 */
export function visitorLogout(id: number) {
  return request.postWithParams('/visitor-server/visitor/logout', { id })
}

/**
 * 访客注册 - 后端使用@RequestBody
 */
export function visitorRegister(data: VisitorRegisterDTO) {
  return request.post('/visitor-server/visitor/register', data)
}

// ==================== 访客信息 ====================

/**
 * 获取访客信息
 */
export function getVisitorInfo(visitorId: number) {
  return request.get<VisitorVO>(`/visitor-server/visitor/info/${visitorId}`)
}

/**
 * 修改访客信息
 */
export function updateVisitor(data: VisitorDTO) {
  return request.put('/visitor-server/visitor/update', data)
}

/**
 * 修改密码 - 后端使用@RequestParam
 */
export function updateVisitorPassword(visitorId: number, oldPassword: string, newPassword: string) {
  return request.postWithParams('/visitor-server/visitor/password', { visitorId, oldPassword, newPassword })
}

/**
 * 通过邮箱修改密码 - 后端使用@RequestBody
 */
export function updateVisitorPasswordByEmail(data: PasswordUpdateEmailDTO) {
  return request.post('/visitor-server/visitor/updatePasswordByEmail', data)
}

/**
 * 删除访客账号 - 后端使用@RequestBody
 */
export function deleteVisitor(data: VisitorDeleteDTO) {
  return request.post('/visitor-server/visitor/delete', data)
}

/**
 * 获取访客名称和头像
 */
export function getVisitorNameAndAvatar(visitorId: number) {
  return request.get<{ name: string; avatar: string }>(`/visitor-server/visitor/getNameAndAvatar/${visitorId}`)
}