package com.wang.manager.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.AuthorQueryDTO;

/**
 * 作者管理Service
 */
public interface AuthorManageService {

    /**
     * 分页查询作者信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Result getAuthorPage(Integer pageNum, Integer pageSize);

    /**
     * 多条件查询作者（支持id、姓名、账号、等级，条件可为空）
     * @param queryDTO 查询条件
     * @return 作者信息列表
     */
    Result getAuthorList(AuthorQueryDTO queryDTO);

    /**
     * 获取作者详情
     * @param id 作者ID
     * @return 作者信息
     */
    Result getAuthorInfo(Integer id);
}