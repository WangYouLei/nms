import request from '@/utils/request'
import type { CaptchaResult } from '@/types'

// ==================== 验证码服务 ====================

/**
 * 生成验证码
 */
export function generateCaptcha() {
  return request.get<CaptchaResult>('/common-server/captcha/generate')
}

/**
 * 校验验证码
 */
export function verifyCaptcha(token: string, code: string) {
  return request.get<boolean>('/common-server/captcha/verify', { token, code })
}

// ==================== 邮件服务 ====================

/**
 * 发送邮箱验证码 - 后端使用@RequestParam
 */
export function sendEmailCode(email: string) {
  return request.postWithParams('/common-server/email/sendCode', { email })
}

/**
 * 验证邮箱验证码 - 后端使用@RequestParam
 */
export function verifyEmailCode(email: string, code: string) {
  return request.postWithParams('/common-server/email/verifyCode', { email, code })
}

// ==================== 文件服务 ====================

/**
 * 上传文件 - 后端使用@RequestParam + MultipartFile
 */
export function uploadFile(
  file: File,
  code: number,
  options?: {
    novelId?: number
    oldFileUrl?: string
  },
  onProgress?: (progress: number) => void
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('code', String(code))
  if (options?.novelId) {
    formData.append('novelId', String(options.novelId))
  }
  if (options?.oldFileUrl) {
    formData.append('oldFileUrl', options.oldFileUrl)
  }
  
  return request.post<string>('/common-server/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (progressEvent.total && onProgress) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(progress)
      }
    }
  })
}

/**
 * 删除文件
 */
export function deleteFile(fileUrl: string) {
  return request.delete('/common-server/file/delete', { fileUrl })
}

/**
 * 获取文件内容
 */
export function getFileContent(fileUrl: string) {
  return request.get<string>('/common-server/file/content', { fileUrl })
}