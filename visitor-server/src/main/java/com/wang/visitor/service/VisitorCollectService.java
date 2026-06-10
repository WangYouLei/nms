package com.wang.visitor.service;

import com.wang.common.result.Result;
import com.wang.pojo.vo.VisitorCollectVO;

import java.util.List;

/**
 * 小说收藏服务接口
 */
public interface VisitorCollectService {

    /**
     * 添加收藏
     * @param visitorId 用户ID
     * @param novelId 小说ID
     * @return 操作结果
     */
    Result addCollect(Long visitorId, Long novelId);

    /**
     * 取消收藏
     * @param visitorId 用户ID
     * @param novelId 小说ID
     * @return 操作结果
     */
    Result removeCollect(Long visitorId, Long novelId);

    /**
     * 获取用户收藏列表
     * @param visitorId 用户ID
     * @return 收藏列表
     */
    Result getCollectList(Long visitorId);

    /**
     * 检查是否已收藏
     * @param visitorId 用户ID
     * @param novelId 小说ID
     * @return 是否已收藏
     */
    Result checkCollect(Long visitorId, Long novelId);

    /**
     * 获取用户收藏数量
     * @param visitorId 用户ID
     * @return 收藏数量
     */
    Result getCollectCount(Long visitorId);
}