package com.wang.search.service;

import com.wang.common.result.Result;

/**
 * 数据同步服务接口
 */
public interface DataSyncService {

    /**
     * 全量同步所有数据（MySQL -> ES）
     * @return 同步结果
     */
    Result syncAll();

    /**
     * 单条小说同步（MySQL -> ES）
     * @param novelId 小说ID
     * @return 同步结果
     */
    Result syncNovel(Long novelId);

    /**
     * 单条作者同步（MySQL -> ES）
     * @param authorId 作者ID
     * @return 同步结果
     */
    Result syncAuthor(Long authorId);
}
