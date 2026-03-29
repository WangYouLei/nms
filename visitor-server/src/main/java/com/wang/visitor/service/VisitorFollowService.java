package com.wang.visitor.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.VisitorFollowDTO;
import com.wang.pojo.vo.VisitorFollowVO;

/**
 * 访客关注作者服务接口
 */
public interface VisitorFollowService {

    /**
     * 关注作者
     * @param dto 关注信息（包含访客ID、作者ID和作者冗余信息）
     * @return 关注结果
     */
    Result follow(VisitorFollowDTO dto);

    /**
     * 取消关注
     * @param visitorId 访客ID
     * @param authorId 作者ID
     * @return 取消结果
     */
    Result unfollow(Integer visitorId, Integer authorId);

    /**
     * 检查是否已关注
     * @param visitorId 访客ID
     * @param authorId 作者ID
     * @return 是否已关注
     */
    Result checkFollow(Integer visitorId, Integer authorId);

    /**
     * 获取我的关注列表
     * @param visitorId 访客ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    Result getMyFollows(Integer visitorId, Integer pageNum, Integer pageSize);

    /**
     * 获取作者的粉丝列表
     * @param authorId 作者ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    Result getFollowers(Integer authorId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的关注数量
     * @param visitorId 访客ID
     * @return 关注数量
     */
    Result getMyFollowCount(Integer visitorId);

    /**
     * 获取作者的粉丝数量
     * @param authorId 作者ID
     * @return 粉丝数量
     */
    Result getFollowerCount(Integer authorId);
}