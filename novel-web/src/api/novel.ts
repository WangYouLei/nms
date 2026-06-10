import request from '@/utils/request'
import type { PageResult } from '@/types'
import type { NovelDetailVO, NovelListVO, NovelChapterVO, NovelCategoryVO, NovelDTO, NovelSearchDTO, NovelCategoryRelationDTO, NovelCategoryDTO, AuthorDetailVO } from '@/types'

// ==================== 公共接口 ====================

/**
 * 获取小说详情
 */
export function getNovelDetail(novelId: number) {
  return request.get<NovelDetailVO>(`/novel-server/common/novel/${novelId}`)
}

/**
 * 搜索小说列表
 */
export function searchNovels(data: NovelSearchDTO) {
  return request.post<PageResult<NovelListVO>>('/novel-server/common/novel/search', data)
}

/**
 * 查询章节列表
 */
export function getChapterList(novelId: number) {
  return request.get<NovelChapterVO[]>('/novel-server/common/chapter/list', { novelId })
}

/**
 * 获取章节详情
 */
export function getChapterDetail(id: number) {
  return request.get<NovelChapterVO>(`/novel-server/common/chapter/detail/${id}`)
}

/**
 * 获取章节内容
 * 注意：后端返回的是 NovelChapterVO 对象，其中包含 content 字段
 */
export function getChapterContent(id: number) {
  return request.get<NovelChapterVO>(`/novel-server/common/chapter/content/${id}`)
}

/**
 * 获取所有分类
 */
export function getAllCategories() {
  return request.get<NovelCategoryVO[]>('/novel-server/common/category/list')
}

/**
 * 根据频道获取分类
 */
export function getCategoriesByChannel(category: number) {
  return request.get<NovelCategoryVO[]>(`/novel-server/common/category/channel/${category}`)
}

/**
 * 获取热门分类
 */
export function getHotCategories() {
  return request.get<NovelCategoryVO[]>('/novel-server/common/category/hot')
}

/**
 * 根据ID查询分类
 */
export function getCategoryById(id: number) {
  return request.get<NovelCategoryVO>(`/novel-server/common/category/${id}`)
}

/**
 * 获取小说的分类
 */
export function getNovelCategory(novelId: number) {
  return request.get<NovelCategoryVO[]>(`/novel-server/common/category/relation/${novelId}`)
}

// ==================== 访客端接口 ====================

/**
 * 分页查询热门小说
 */
export function getHotNovels(params: { pageNum?: number; pageSize?: number; categoryId?: number }) {
  return request.get<PageResult<NovelListVO>>('/novel-server/visitor/novel/hot', params)
}

/**
 * 按分类查询小说
 */
export function getNovelsByCategory(categoryId: number, params: { 
  pageNum?: number
  pageSize?: number
  sortBy?: string
  isFinished?: boolean
}) {
  return request.get<PageResult<NovelListVO>>(`/novel-server/visitor/novel/category/${categoryId}`, params)
}

/**
 * 按作者查询小说（使用通用搜索接口）
 */
export function getNovelsByAuthor(authorId: number, params?: { pageNum?: number; pageSize?: number }) {
  return request.post<PageResult<NovelListVO>>('/novel-server/common/novel/search', {
    authorId,
    pageNum: params?.pageNum ?? 1,
    pageSize: params?.pageSize ?? 10
  })
}

/**
 * 获取作者详情（访客端）
 * 包含作者信息和作品列表
 */
export function getVisitorAuthorDetail(authorId: number, params?: { pageNum?: number; pageSize?: number }) {
  return request.get<AuthorDetailVO>(`/novel-server/visitor/author/${authorId}`, params)
}

// ==================== 作者端接口 ====================

/**
 * 新增小说
 */
export function addNovel(data: NovelDTO) {
  return request.post('/novel-server/author/novel/add', data)
}

/**
 * 删除小说（作者端，逻辑删除）
 */
export function deleteNovelAuthor(id: number) {
  return request.delete(`/novel-server/author/novel/delete/${id}`)
}

/**
 * 修改小说信息
 */
export function updateNovel(data: NovelDTO) {
  return request.put('/novel-server/author/novel/update', data)
}

/**
 * 上传新章节
 */
export function uploadChapter(
  novelId: number,
  title: string,
  wordCount: number,
  file: File,
  onProgress?: (progress: number) => void
) {
  const formData = new FormData()
  formData.append('novelId', String(novelId))
  formData.append('title', title)
  formData.append('wordCount', String(wordCount))
  formData.append('file', file)
  
  return request.post('/novel-server/author/chapter/upload', formData, {
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
 * 删除章节
 */
export function deleteChapter(id: number) {
  return request.delete(`/novel-server/author/chapter/delete/${id}`)
}

/**
 * 更新章节
 */
export function updateChapter(params: {
  id: number
  title: string
  chapterOrder?: number
  wordCount?: number
  oldFileUrl?: string
  file?: File
}) {
  const formData = new FormData()
  formData.append('id', String(params.id))
  formData.append('title', params.title)
  if (params.chapterOrder) {
    formData.append('chapterOrder', String(params.chapterOrder))
  }
  if (params.wordCount !== undefined) {
    formData.append('wordCount', String(params.wordCount))
  }
  if (params.oldFileUrl) {
    formData.append('oldFileUrl', params.oldFileUrl)
  }
  if (params.file) {
    formData.append('file', params.file)
  }
  
  return request.post('/novel-server/author/chapter/update', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 设置小说分类
 */
export function setNovelCategory(data: NovelCategoryRelationDTO) {
  return request.post('/novel-server/author/category/relation/set', data)
}

// ==================== 管理端接口 ====================

/**
 * 删除小说（管理端，物理删除）
 */
export function deleteNovelManager(id: number) {
  return request.delete(`/novel-server/manager/novel/delete/${id}`)
}

/**
 * 添加分类
 */
export function addCategory(data: NovelCategoryDTO) {
  return request.post('/novel-server/manager/category/add', data)
}

/**
 * 修改分类
 */
export function updateCategory(data: NovelCategoryDTO) {
  return request.put('/novel-server/manager/category/update', data)
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return request.delete(`/novel-server/manager/category/delete/${id}`)
}

/**
 * 分页查询分类
 */
export function getCategoryList(params: { pageNum?: number; pageSize?: number; type?: string; category?: number }) {
  return request.get<PageResult<NovelCategoryVO>>('/novel-server/manager/category/page', params)
}