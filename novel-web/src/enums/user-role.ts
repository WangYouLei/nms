/**
 * 用户角色枚举
 */
export enum UserRole {
  /** 管理员 */
  MANAGER = 'MANAGER',
  /** 作者 */
  AUTHOR = 'AUTHOR',
  /** 访客 */
  VISITOR = 'VISITOR'
}

/**
 * 用户角色描述
 */
export const UserRoleLabels: Record<UserRole, string> = {
  [UserRole.MANAGER]: '管理员',
  [UserRole.AUTHOR]: '作者',
  [UserRole.VISITOR]: '访客'
}

/**
 * 判断是否为管理员
 */
export function isManager(role: string): boolean {
  return role?.toUpperCase() === UserRole.MANAGER
}

/**
 * 判断是否为作者
 */
export function isAuthor(role: string): boolean {
  return role?.toUpperCase() === UserRole.AUTHOR
}

/**
 * 判断是否为访客
 */
export function isVisitor(role: string): boolean {
  return role?.toUpperCase() === UserRole.VISITOR
}