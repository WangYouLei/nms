/**
 * 统一响应结果
 */
export interface Result<T = any> {
  /** 状态码：1成功，0及其他为失败 */
  code: number
  /** 消息 */
  msg: string
  /** 数据 */
  data: T
}

/**
 * 分页响应数据
 */
export interface PageResult<T> {
  /** 当前页码 */
  pageNum: number
  /** 每页数量 */
  pageSize: number
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
  /** 数据列表 */
  list: T[]
}

/**
 * 分页请求参数
 */
export interface PaginationParams {
  /** 当前页码 */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
}

/**
 * 登录响应数据
 */
export interface LoginResult {
  /** JWT Token */
  token: string
  /** 用户信息 */
  userInfo: UserInfo
}

/**
 * 用户基础信息
 */
export interface UserInfo {
  id: number
  name: string
  avatar: string
  account: string
  email?: string
}

/**
 * 验证码响应
 */
export interface CaptchaResult {
  /** 验证码token */
  token: string
  /** Base64编码的验证码图片 */
  image: string
}