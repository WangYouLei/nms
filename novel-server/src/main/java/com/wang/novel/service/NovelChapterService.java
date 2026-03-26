package com.wang.novel.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelChapterDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小说章节服务接口
 * 提供 author、manager、visitor 三个端口共用的章节功能
 */
public interface NovelChapterService {

    // ==================== Common - 公共方法 ====================

    /**
     * 查询小说的所有章节列表
     * @param novelId 小说ID
     * @return 章节列表
     */
    Result getChapterList(Integer novelId);

    /**
     * 获取章节详情
     * @param chapterId 章节ID
     * @return 章节详情
     */
    Result getChapterDetail(Integer chapterId);

    /**
     * 获取章节内容
     * @param chapterId 章节ID
     * @return 章节内容
     */
    Result getChapterContent(Integer chapterId);

    // ==================== Author/Manager - 作者/管理端方法 ====================

    /**
     * 上传新章节
     * @param novelId 小说ID
     * @param title 新章节标题
     * @param wordCount 章节字数
     * @param file 新章节文件
     * @return 操作结果
     */
    Result uploadChapter(Integer novelId, String title, Integer wordCount, MultipartFile file);

    /**
     * 删除章节
     * @param id 章节ID
     * @return 操作结果
     */
    Result deleteChapter(Integer id);

    /**
     * 更新章节信息（包括章节内容）
     * @param id 章节id
     * @param title 章节标题
     * @param chapterOrder 章节顺序
     * @param wordCount 章节字数
     * @param oldFileUrl 旧文件路径
     * @param file 新文件
     * @return
     */
    Result updateChapter(Integer id, String title, Integer chapterOrder, Integer wordCount, String oldFileUrl, MultipartFile file);
}