/**
 * 业务状态码枚举
 */
export enum BizCode {
  // 通用状态码
  /** 成功 */
  SUCCESS = 10000,
  /** 失败 */
  FAIL = 10001,
  PARAM_INVALID = 10002,
  SYSTEM_ERROR = 10003,
  FILE_UPLOAD_FAIL = 10004,
  RESOURCE_NOT_FOUND = 10005,
  PERMISSION_DENIED = 10006,
  DATA_DUPLICATE = 10007,

  // 用户相关
  USER_NOT_FOUND = 20001,
  USER_EXIST = 20002,
  USER_ACCOUNT_ERROR = 20003,
  USER_NOT_LOGIN = 20004,
  USER_LOGIN_EXPIRED = 20005,
  USER_DISABLED = 20006,
  USER_EMAIL_ERROR = 20007,

  // 小说相关
  NOVEL_NOT_FOUND = 30001,
  NOVEL_TITLE_EXIST = 30002,
  NOVEL_CATEGORY_NOT_FOUND = 30003,
  NOVEL_CATEGORY_EXIST = 30004,
  NOVEL_CHAPTER_NOT_FOUND = 30005,
  NOVEL_CHAPTER_TITLE_EXIST = 30006,
  NOVEL_CHAPTER_SAVE_FAIL = 30007,

  // 访客相关
  VISITOR_NOT_FOUND = 40001,
  VISITOR_ACCOUNT_EXIST = 40002
}

/**
 * 业务状态码消息
 */
export const BizCodeMessages: Record<BizCode, string> = {
  [BizCode.SUCCESS]: '操作成功',
  [BizCode.FAIL]: '操作失败',
  [BizCode.PARAM_INVALID]: '参数错误',
  [BizCode.SYSTEM_ERROR]: '系统内部错误',
  [BizCode.FILE_UPLOAD_FAIL]: '文件上传失败',
  [BizCode.RESOURCE_NOT_FOUND]: '请求的资源不存在',
  [BizCode.PERMISSION_DENIED]: '权限不足',
  [BizCode.DATA_DUPLICATE]: '数据已存在',
  [BizCode.USER_NOT_FOUND]: '用户不存在',
  [BizCode.USER_EXIST]: '用户名称或账号已存在',
  [BizCode.USER_ACCOUNT_ERROR]: '账号或密码错误',
  [BizCode.USER_NOT_LOGIN]: '用户未登录',
  [BizCode.USER_LOGIN_EXPIRED]: '登录已过期，请重新登录',
  [BizCode.USER_DISABLED]: '用户已被禁用',
  [BizCode.USER_EMAIL_ERROR]: '用户账号与邮箱不匹配',
  [BizCode.NOVEL_NOT_FOUND]: '小说不存在',
  [BizCode.NOVEL_TITLE_EXIST]: '小说标题已存在',
  [BizCode.NOVEL_CATEGORY_NOT_FOUND]: '小说分类不存在',
  [BizCode.NOVEL_CATEGORY_EXIST]: '小说分类已存在',
  [BizCode.NOVEL_CHAPTER_NOT_FOUND]: '小说章节不存在',
  [BizCode.NOVEL_CHAPTER_TITLE_EXIST]: '小说章节标题已存在',
  [BizCode.NOVEL_CHAPTER_SAVE_FAIL]: '保存章节记录失败',
  [BizCode.VISITOR_NOT_FOUND]: '访客不存在',
  [BizCode.VISITOR_ACCOUNT_EXIST]: '访客账号已存在'
}