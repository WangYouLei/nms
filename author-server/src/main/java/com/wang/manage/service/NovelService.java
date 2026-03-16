package com.wang.manage.service;
import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelDTO;


/**
 * 小说服务接口
 */
public interface NovelService {
    
    /**
     * 新增小说
     * @param novel 小说信息
     * @return 操作结果
     */
    Result addNovel(NovelDTO novel);
    
    /**
     * 根据ID删除小说
     * @param id 小说ID
     * @return 操作结果
     */
    Result deleteNovel(Integer id);
    
    /**
     * 分页查询当前登录作者的小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    Result getNovelList(Integer pageNum, Integer pageSize);
    
    /**
     * 根据小说名称或副名称进行模糊查询
     * @param name 小说名称
     * @param subName 小说副名称
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    Result searchNovels(String name, String subName, Integer pageNum, Integer pageSize);
    
    /**
     * 修改小说信息
     * @param novelDTO 小说信息
     * @return 操作结果
     */
    Result updateNovel(NovelDTO novelDTO);
}
