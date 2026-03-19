package com.wang.manager.service;

import com.wang.common.result.Result;

/**
 * 数据概览Service
 */
public interface DashboardService {

    /**
     * 获取概览数据
     * @return 概览数据
     */
    Result getOverview();
}