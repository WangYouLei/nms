const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const ROLE_KEY = 'role'

/**
 * 获取Token
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo(): any {
  const info = localStorage.getItem(USER_INFO_KEY)
  return info ? JSON.parse(info) : null
}

/**
 * 设置用户信息
 */
export function setUserInfo(info: any): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
}

/**
 * 移除用户信息
 */
export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取角色
 */
export function getRole(): string | null {
  return localStorage.getItem(ROLE_KEY)
}

/**
 * 设置角色
 */
export function setRole(role: string): void {
  localStorage.setItem(ROLE_KEY, role)
}

/**
 * 移除角色
 */
export function removeRole(): void {
  localStorage.removeItem(ROLE_KEY)
}

/**
 * 清除所有认证信息
 */
export function clearAuth(): void {
  removeToken()
  removeUserInfo()
  removeRole()
}