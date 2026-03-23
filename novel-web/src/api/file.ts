import request from '@/utils/request'

/**
 * 获取预签名URL
 * 用于前端直接访问私有 MinIO bucket 中的文件
 * @param fileUrl 原始文件URL
 * @param expireSeconds 过期时间（秒），默认1小时
 * @returns 预签名URL
 */
export function getPresignedUrl(fileUrl: string, expireSeconds: number = 3600) {
  return request.get<string>('/common-server/file/presigned-url', { 
    fileUrl, 
    expireSeconds 
  })
}