package com.wang.manage.service;

import java.io.InputStream;

/**
 * 章节存储服务接口
 * 封装 MinIO 文件操作，职责单一
 */
public interface ChapterStorageService {

    /**
     * 上传章节内容到 MinIO
     *
     * @param novelId   小说ID
     * @param title     章节标题
     * @param content   章节内容
     * @return 存储路径（objectName）
     */
    String uploadChapterContent(Integer novelId, String title, String content);

    /**
     * 上传章节文件到 MinIO
     *
     * @param novelId 小说ID
     * @param title   章节标题
     * @param stream  文件流
     * @param size    文件大小
     * @return 存储路径（objectName）
     */
    String uploadChapterFile(Integer novelId, String title, InputStream stream, long size);

    /**
     * 获取章节内容
     *
     * @param objectName 存储路径
     * @return 章节内容字符串
     */
    String getChapterContent(String objectName);

    /**
     * 删除章节文件
     *
     * @param objectName 存储路径
     */
    void deleteChapterContent(String objectName);

    /**
     * 重命名章节文件
     *
     * @param oldObjectName 旧路径
     * @param novelId       小说ID
     * @param newTitle      新标题
     * @return 新存储路径
     */
    String renameChapterContent(String oldObjectName, Integer novelId, String newTitle);

    /**
     * 从内容URL提取存储路径
     *
     * @param contentUrl 内容URL
     * @return 存储路径（objectName）
     */
    String extractObjectName(String contentUrl);

    /**
     * 构建完整的访问URL
     *
     * @param objectName 存储路径
     * @return 完整URL
     */
    String buildContentUrl(String objectName);
}