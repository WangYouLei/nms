package com.wang.manager.service.impl;

import com.wang.common.result.Result;
import com.wang.manager.mapper.StatisticsMapper;
import com.wang.manager.service.DashboardService;
import com.wang.pojo.vo.DashboardOverviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 数据概览Service实现
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private final StatisticsMapper statisticsMapper;

    public DashboardServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    public Result getOverview() {
        log.info("获取数据概览");

        LocalDate today = LocalDate.now();

        DashboardOverviewVO vo = new DashboardOverviewVO();
        
        // 基础统计
        vo.setNovelCount(statisticsMapper.countAllNovels());
        vo.setAuthorCount(statisticsMapper.countAllAuthors());
        vo.setVisitorCount(statisticsMapper.countAllVisitors());
        vo.setCategoryCount(statisticsMapper.countAllCategories());
        
        // 今日新增
        vo.setTodayNewNovels(statisticsMapper.countTodayNewNovels(today));
        vo.setTodayNewAuthors(statisticsMapper.countTodayNewAuthors(today));
        vo.setTodayNewVisitors(statisticsMapper.countTodayNewVisitors(today));
        
        // 小说状态统计
        vo.setHotNovelCount(statisticsMapper.countHotNovels());
        vo.setFinishedNovelCount(statisticsMapper.countFinishedNovels());

        log.info("数据概览获取成功：小说={}, 作者={}, 用户={}", 
                vo.getNovelCount(), vo.getAuthorCount(), vo.getVisitorCount());

        return Result.success(vo);
    }
}