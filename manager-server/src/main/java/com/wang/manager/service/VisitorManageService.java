package com.wang.manager.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.VisitorQueryDTO;

/**
 * 访客管理Service
 */
public interface VisitorManageService {

    /**
     * 分页查询访客信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Result getVisitorPage(Integer pageNum, Integer pageSize);

    /**
     * 多条件查询访客（支持id、姓名、账号、VIP等级，条件可为空）
     * @param queryDTO 查询条件
     * @return 访客信息列表
     */
    Result getVisitorList(VisitorQueryDTO queryDTO);

    /**
     * 获取访客详情
     * @param id 访客ID
     * @return 访客信息
     */
    Result getVisitorInfo(Integer id);
}