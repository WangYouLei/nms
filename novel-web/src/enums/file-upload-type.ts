/**
 * 文件上传类型枚举
 */
export enum FileUploadType {
  /** 作者头像 */
  AUTHOR_AVATAR = 0,
  /** 访客头像 */
  VISITOR_AVATAR = 1,
  /** 管理员头像 */
  MANAGER_AVATAR = 2,
  /** 小说封面 */
  NOVEL_COVER = 3,
  /** 小说章节 */
  NOVEL_CHAPTER = 4
}

/**
 * 文件上传类型描述
 */
export const FileUploadTypeLabels: Record<FileUploadType, string> = {
  [FileUploadType.AUTHOR_AVATAR]: '作者头像',
  [FileUploadType.VISITOR_AVATAR]: '访客头像',
  [FileUploadType.MANAGER_AVATAR]: '管理员头像',
  [FileUploadType.NOVEL_COVER]: '小说封面',
  [FileUploadType.NOVEL_CHAPTER]: '小说章节'
}