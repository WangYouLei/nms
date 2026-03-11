package com.wang.manage.service;

import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelChapterDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小说章节服务接口
 */
public interface NovelChapterService {

    /**
     * 上传章节
     * @param novelId 小说ID
     * @param title 章节标题
     * @param file 章节文件
     * @return 操作结果
     */
    Result uploadChapter(Integer novelId, String title, MultipartFile file);

    /**
     * 删除章节
     * @param id 章节ID
     * @return 操作结果
     */
    Result deleteChapter(Integer id);

    /**
     * 更新章节信息
     * @param chapterDTO 章节信息
     * @return 操作结果
     */
    Result updateChapter(NovelChapterDTO chapterDTO);

    /**
     * 分页查询章节列表
     * @param novelId 小说ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Result getChapterList(Integer novelId, Integer pageNum, Integer pageSize);

    /**
     * 获取章节详情
     * @param id 章节ID
     * @return 章节详情
     */
    Result getChapterDetail(Integer id);
}