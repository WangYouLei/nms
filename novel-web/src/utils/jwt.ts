import type { UserInfo } from '@/types'

/**
 * JWT Token 前缀
 */
const JWT_PREFIX = 'NovelManagementSystem'

/**
 * Base64 URL 解码（支持 UTF-8）
 */
function base64UrlDecode(str: string): string {
  // 替换 URL 安全字符
  let base64 = str.replace(/-/g, '+').replace(/_/g, '/')
  // 补齐 Base64 填充
  const padding = base64.length % 4
  if (padding) {
    base64 += '='.repeat(4 - padding)
  }
  
  // 使用 TextDecoder 正确解码 UTF-8 字符
  const binaryString = atob(base64)
  const bytes = new Uint8Array(binaryString.length)
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  return new TextDecoder('utf-8').decode(bytes)
}

/**
 * 解析 JWT Token 获取用户信息
 * @param token JWT Token
 * @returns 用户信息或 null
 */
export function parseJwtToken(token: string): UserInfo | null {
  if (!token) {
    return null
  }

  try {
    // 移除前缀
    const pureToken = token.replace(JWT_PREFIX, '')
    
    // 分割 token
    const parts = pureToken.split('.')
    if (parts.length !== 3) {
      console.error('Invalid JWT token format')
      return null
    }

    // 解码 payload (第二部分)
    const payload = JSON.parse(base64UrlDecode(parts[1]))
    
    return {
      id: payload.id,
      name: payload.name,
      avatar: payload.avatar,
      account: payload.account
    }
  } catch (error) {
    console.error('Failed to parse JWT token:', error)
    return null
  }
}

/**
 * 从 JWT Token 获取用户角色
 * @param token JWT Token
 * @returns 角色字符串或 null
 */
export function getRoleFromToken(token: string): string | null {
  if (!token) {
    return null
  }

  try {
    // 移除前缀
    const pureToken = token.replace(JWT_PREFIX, '')
    
    // 分割 token
    const parts = pureToken.split('.')
    if (parts.length !== 3) {
      return null
    }

    // 解码 payload
    const payload = JSON.parse(base64UrlDecode(parts[1]))
    return payload.role || null
  } catch (error) {
    console.error('Failed to get role from JWT token:', error)
    return null
  }
}