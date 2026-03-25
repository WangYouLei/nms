import request from '@/utils/request'
import type { AuthorVO, AuthorDTO, AuthorRegisterDTO, PasswordUpdateEmailDTO, VisitorAuthorVO } from '@/types'

// ==================== 作者认证 ====================

/**
 * 作者登录 - 后端使用@RequestParam，参数在URL中
 * @returns Result<string> - data 为 JWT token 字符串
 */
export function authorLogin(account: string, password: string) {
  return request.postWithParams<string>('/author-server/author/login', { account, password })
}

/**
 * 作者退出登录 - 后端使用@RequestParam
 */
export function authorLogout(id: number) {
  return request.postWithParams('/author-server/author/logout', { id })
}

/**
 * 作者注册 - 后端使用@RequestBody
 */
export function authorRegister(data: AuthorRegisterDTO) {
  return request.post('/author-server/author/register', data)
}

// ==================== 作者信息 ====================

/**
 * 获取作者信息
 */
export function getAuthorInfo(id: number) {
  return request.get<AuthorVO>(`/author-server/author/info/${id}`)
}

/**
 * 获取作者公开信息（访客端使用）
 * 不包含敏感信息（账号、邮箱等）
 */
export function getAuthorPublicInfo(id: number) {
  return request.get<VisitorAuthorVO>(`/author-server/author/getNameAndAvatar/${id}`)
}

/**
 * 修改作者信息 - 后端使用@RequestBody
 */
export function updateAuthor(data: AuthorDTO) {
  return request.put('/author-server/author/update', data)
}

/**
 * 作者注销
 */
export function deleteAuthor(id: number) {
  return request.delete(`/author-server/author/delete/${id}`)
}

/**
 * 修改密码 - 后端使用@RequestParam
 */
export function updateAuthorPassword(id: number, oldPassword: string, newPassword: string) {
  return request.postWithParams('/author-server/author/updatePassword', { id, oldPassword, newPassword })
}

/**
 * 通过邮箱修改密码 - 后端使用@RequestBody
 */
export function updateAuthorPasswordByEmail(data: PasswordUpdateEmailDTO) {
  return request.post('/author-server/author/updatePasswordByEmail', data)
}