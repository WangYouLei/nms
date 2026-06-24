import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import router from '@/router'
import { BizCode } from '@/enums'
import type { Result } from '@/types'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

// 登录过期弹窗标志，防止多次弹出
let isShowingLoginExpired = false

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      // 后端期望 token 放在请求头的 "token" 字段中
      config.headers.token = userStore.token
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const { code, msg } = response.data
    
    // 成功
    if (code === BizCode.SUCCESS) {
      return response.data as any
    }
    
    // 用户未登录或登录过期
    if (code === BizCode.USER_NOT_LOGIN || code === BizCode.USER_LOGIN_EXPIRED) {
      // 防止多次弹出
      if (!isShowingLoginExpired) {
        isShowingLoginExpired = true
        ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
        }).catch(() => {}).finally(() => {
          isShowingLoginExpired = false
        })
      }
      return Promise.reject(new Error(msg))
    }
    
    // 其他错误
    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  (error) => {
    let message = '网络错误'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data?.msg || '请求失败'
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装请求方法
const request = {
  get<T = any>(url: string, params?: object, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.get(url, { params, ...config })
  },
  
  post<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.post(url, data, config)
  },
  
  postWithParams<T = any>(url: string, params?: object, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.post(url, null, { params, ...config })
  },
  
  put<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.put(url, data, config)
  },

  patch<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.patch(url, data, config)
  },

  delete<T = any>(url: string, params?: object, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.delete(url, { ...config, params })
  },
  
  upload<T = any>(url: string, file: File, onProgress?: (progress: number) => void): Promise<Result<T>> {
    const formData = new FormData()
    formData.append('file', file)
    // 注意：不要手动设置 Content-Type！
    // axios 检测到 data 是 FormData 时，会自动设置正确的 Content-Type（含 boundary）
    return service.post(url, formData, {
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && onProgress) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  }
}

export default request
export { service as axiosInstance }