package com.wang.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manager.mapper.VisitorMapper;
import com.wang.manager.service.VisitorManageService;
import com.wang.pojo.dto.VisitorQueryDTO;
import com.wang.pojo.entity.Visitor;
import com.wang.pojo.vo.VisitorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VisitorManageServiceImpl implements VisitorManageService {

    private final VisitorMapper visitorMapper;

    public VisitorManageServiceImpl(VisitorMapper visitorMapper) {
        this.visitorMapper = visitorMapper;
    }

    @Override
    public Result getVisitorPage(Integer pageNum, Integer pageSize) {
        log.info("分页查询访客：页码={}, 每页数量={}", pageNum, pageSize);

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 创建分页对象
        Page<Visitor> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Visitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Visitor::getCreateTime);

        // 执行分页查询
        Page<Visitor> result = visitorMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<VisitorVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<VisitorVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                voList
        );

        log.info("分页查询访客成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    @Override
    public Result getVisitorList(VisitorQueryDTO queryDTO) {
        log.info("多条件查询访客：queryDTO={}", queryDTO);

        LambdaQueryWrapper<Visitor> queryWrapper = new LambdaQueryWrapper<>();

        // 根据ID精确查询
        if (queryDTO.getId() != null) {
            queryWrapper.eq(Visitor::getId, queryDTO.getId());
        }

        // 根据姓名模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(Visitor::getName, queryDTO.getName());
        }

        // 根据账号模糊查询
        if (StringUtils.hasText(queryDTO.getAccount())) {
            queryWrapper.like(Visitor::getAccount, queryDTO.getAccount());
        }

        // 根据VIP等级查询
        if (queryDTO.getVipLevel() != null) {
            queryWrapper.eq(Visitor::getVipLevel, queryDTO.getVipLevel());
        }

        queryWrapper.orderByDesc(Visitor::getCreateTime);

        List<Visitor> visitors = visitorMapper.selectList(queryWrapper);

        List<VisitorVO> voList = visitors.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result getVisitorInfo(Long id) {
        log.info("获取访客详情：ID={}", id);

        Visitor visitor = visitorMapper.selectById(id);
        if (visitor == null) {
            log.warn("访客不存在：ID={}", id);
            return Result.error("访客不存在");
        }

        return Result.success(convertToVO(visitor));
    }

    /**
     * 转换为VO
     */
    private VisitorVO convertToVO(Visitor visitor) {
        VisitorVO vo = new VisitorVO();
        BeanUtils.copyProperties(visitor, vo);
        return vo;
    }
}