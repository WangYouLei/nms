import { ref, reactive } from 'vue'
import { getPresignedUrl } from '@/api/file'

/**
 * 文件 URL 工具函数
 * 使用预签名URL访问私有 MinIO bucket 中的文件
 */

/** URL 缓存，避免重复请求 */
const urlCache = reactive(new Map<string, { url: string; expireTime: number }>())

/** 响应式URL状态，用于触发组件重新渲染 */
const reactiveUrlMap = reactive(new Map<string, string>())

/** 正在获取预签名URL的请求，避免重复请求 */
const pendingRequests = new Map<string, Promise<string>>()

/** 默认过期时间（秒） */
const DEFAULT_EXPIRE_SECONDS = 3600

/** 缓存提前刷新时间（毫秒），提前5分钟刷新 */
const CACHE_REFRESH_AHEAD = 5 * 60 * 1000

/**
 * 检查是否为 MinIO URL
 */
function isMinioUrl(url: string): boolean {
  return url.includes('127.0.0.1:9000') || url.includes('minio')
}

/**
 * 获取预签名URL（带缓存）
 * @param url 原始文件URL
 * @param expireSeconds 过期时间（秒）
 * @returns 预签名URL
 */
async function fetchPresignedUrl(url: string, expireSeconds: number = DEFAULT_EXPIRE_SECONDS): Promise<string> {
  // 检查缓存
  const cached = urlCache.get(url)
  if (cached && cached.expireTime > Date.now() + CACHE_REFRESH_AHEAD) {
    return cached.url
  }

  // 检查是否有正在进行的请求
  const pending = pendingRequests.get(url)
  if (pending) {
    return pending
  }

  // 发起新请求
  const request = (async () => {
    try {
      const res = await getPresignedUrl(url, expireSeconds)
      const presignedUrl = res.data
      
      if (presignedUrl) {
        // 缓存结果
        urlCache.set(url, {
          url: presignedUrl,
          expireTime: Date.now() + expireSeconds * 1000
        })
        // 更新响应式状态
        reactiveUrlMap.set(url, presignedUrl)
        return presignedUrl
      }
    } catch (error) {
      console.error('Failed to get presigned URL:', error)
    }
    return url
  })()

  pendingRequests.set(url, request)
  
  try {
    return await request
  } finally {
    pendingRequests.delete(url)
  }
}

/**
 * 转换文件 URL（同步版本，返回原始URL）
 * 用于初始渲染，之后会被异步更新
 * @param url 原始 URL
 * @returns URL
 */
export function transformFileUrl(url: string | undefined | null): string {
  if (!url) {
    return ''
  }
  return url
}

/**
 * 获取图片 URL（带预签名和默认图）
 * 异步获取预签名URL
 * @param url 原始 URL
 * @param defaultUrl 默认图片地址
 * @returns 图片 URL（可能是原始URL或预签名URL）
 */
export function getImageUrl(url: string | undefined | null, defaultUrl: string = '/default-cover.jpg'): string {
  if (!url) {
    return defaultUrl
  }
  
  // 检查缓存
  const cached = urlCache.get(url)
  if (cached && cached.expireTime > Date.now()) {
    return cached.url
  }
  
  // 检查响应式状态（可能已经异步获取完成）
  const reactiveUrl = reactiveUrlMap.get(url)
  if (reactiveUrl) {
    return reactiveUrl
  }
  
  // 如果是 MinIO URL，触发异步获取预签名URL
  if (isMinioUrl(url)) {
    // 异步获取预签名URL，不阻塞渲染
    fetchPresignedUrl(url).catch(() => {})
    // 返回原始URL，等下次渲染时会使用缓存的预签名URL
    return url
  }
  
  return url
}

/**
 * 使用响应式图片URL（推荐用于Vue组件）
 * @param url 原始 URL
 * @param defaultUrl 默认图片地址
 * @returns 响应式的图片URL
 */
export function useImageUrl(url: string | undefined | null, defaultUrl: string = '/default-cover.jpg') {
  const imageUrl = ref(defaultUrl)
  
  const updateUrl = async () => {
    if (!url) {
      imageUrl.value = defaultUrl
      return
    }
    
    // 先检查缓存
    const cached = urlCache.get(url)
    if (cached && cached.expireTime > Date.now()) {
      imageUrl.value = cached.url
      return
    }
    
    // 检查响应式状态
    const reactiveUrl = reactiveUrlMap.get(url)
    if (reactiveUrl) {
      imageUrl.value = reactiveUrl
      return
    }
    
    // 如果是 MinIO URL，获取预签名
    if (isMinioUrl(url)) {
      imageUrl.value = url // 先显示原始URL
      try {
        const presignedUrl = await fetchPresignedUrl(url)
        imageUrl.value = presignedUrl
      } catch (error) {
        console.error('Failed to get presigned URL:', error)
      }
    } else {
      imageUrl.value = url
    }
  }
  
  updateUrl()
  
  return imageUrl
}

/**
 * 获取头像 URL
 * @param url 原始 URL
 * @returns 头像 URL
 */
export function getAvatarUrl(url: string | undefined | null): string {
  return getImageUrl(url, '/default-avatar.png')
}

/**
 * 异步获取预签名URL
 * 用于需要立即获取预签名URL的场景
 * @param url 原始 URL
 * @returns 预签名URL
 */
export async function getPresignedFileUrl(url: string | undefined | null): Promise<string> {
  if (!url) {
    return ''
  }
  
  if (!isMinioUrl(url)) {
    return url
  }
  
  return fetchPresignedUrl(url)
}

/**
 * 清除URL缓存
 */
export function clearUrlCache(): void {
  urlCache.clear()
  reactiveUrlMap.clear()
}