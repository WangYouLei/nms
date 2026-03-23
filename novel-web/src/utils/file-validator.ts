/**
 * 文件上传相关工具函数
 */
import { ElMessage } from 'element-plus'

/** 默认最大文件大小 10MB */
export const DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024

/** 图片类型 */
export const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

/** 图片扩展名 */
export const IMAGE_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.webp']

/**
 * 校验文件大小
 * @param file 文件对象
 * @param maxSize 最大大小（字节），默认10MB
 * @param _maxSizeText 最大大小文本描述，用于错误提示（暂未使用）
 * @returns 是否通过校验
 */
export function validateFileSize(
  file: File,
  maxSize: number = DEFAULT_MAX_FILE_SIZE,
  _maxSizeText: string = '10MB'
): boolean {
  if (file.size > maxSize) {
    return false
  }
  return true
}

/**
 * 校验文件类型
 * @param file 文件对象
 * @param allowedTypes 允许的MIME类型列表
 * @returns 是否通过校验
 */
export function validateFileType(file: File, allowedTypes: string[]): boolean {
  return allowedTypes.includes(file.type)
}

/**
 * 校验图片文件
 * @param file 文件对象
 * @param maxSize 最大大小（字节），默认10MB
 * @param maxSizeText 最大大小文本描述
 * @returns 错误信息，如果校验通过则返回null
 */
export function validateImageFile(
  file: File,
  maxSize: number = DEFAULT_MAX_FILE_SIZE,
  maxSizeText: string = '10MB'
): string | null {
  // 校验文件类型
  if (!validateFileType(file, IMAGE_TYPES)) {
    return '只能上传 JPG、PNG、GIF、WEBP 格式的图片'
  }

  // 校验文件大小
  if (!validateFileSize(file, maxSize, maxSizeText)) {
    return `图片大小不能超过 ${maxSizeText}`
  }

  return null
}

/**
 * 通用的上传前校验函数
 * @param file 文件对象
 * @param options 校验选项
 * @returns 是否通过校验
 */
export function beforeUploadValidate(
  file: File,
  options: {
    /** 允许的文件类型 */
    allowedTypes?: string[]
    /** 最大文件大小（字节） */
    maxSize?: number
    /** 最大大小文本描述 */
    maxSizeText?: string
  } = {}
): boolean {
  const {
    allowedTypes = IMAGE_TYPES,
    maxSize = DEFAULT_MAX_FILE_SIZE,
    maxSizeText = '10MB'
  } = options

  // 校验文件类型
  if (allowedTypes.length > 0 && !validateFileType(file, allowedTypes)) {
    ElMessage.error(`不支持的文件格式`)
    return false
  }

  // 校验文件大小
  if (!validateFileSize(file, maxSize, maxSizeText)) {
    ElMessage.error(`文件大小不能超过 ${maxSizeText}`)
    return false
  }

  return true
}