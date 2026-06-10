package com.wang.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manager.mapper.AuthorMapper;
import com.wang.manager.service.AuthorManageService;
import com.wang.pojo.dto.AuthorQueryDTO;
import com.wang.pojo.entity.Author;
import com.wang.pojo.vo.AuthorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthorManageServiceImpl implements AuthorManageService {

    private final AuthorMapper authorMapper;

    public AuthorManageServiceImpl(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    @Override
    public Result getAuthorPage(Integer pageNum, Integer pageSize) {
        log.info("分页查询作者：页码={}, 每页数量={}", pageNum, pageSize);

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 创建分页对象
        Page<Author> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Author> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Author::getCreateTime);

        // 执行分页查询
        Page<Author> result = authorMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<AuthorVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<AuthorVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                voList
        );

        log.info("分页查询作者成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    @Override
    public Result getAuthorList(AuthorQueryDTO queryDTO) {
        log.info("多条件查询作者：queryDTO={}", queryDTO);

        LambdaQueryWrapper<Author> queryWrapper = new LambdaQueryWrapper<>();

        // 根据ID精确查询
        if (queryDTO.getId() != null) {
            queryWrapper.eq(Author::getId, queryDTO.getId());
        }

        // 根据姓名模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(Author::getName, queryDTO.getName());
        }

        // 根据账号模糊查询
        if (StringUtils.hasText(queryDTO.getAccount())) {
            queryWrapper.like(Author::getAccount, queryDTO.getAccount());
        }

        // 根据等级查询
        if (queryDTO.getRank() != null) {
            queryWrapper.eq(Author::getRank, queryDTO.getRank());
        }

        queryWrapper.orderByDesc(Author::getCreateTime);

        List<Author> authors = authorMapper.selectList(queryWrapper);

        List<AuthorVO> voList = authors.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result getAuthorInfo(Long id) {
        log.info("获取作者详情：ID={}", id);

        Author author = authorMapper.selectById(id);
        if (author == null) {
            log.warn("作者不存在：ID={}", id);
            return Result.error("作者不存在");
        }

        return Result.success(convertToVO(author));
    }

    /**
     * 转换为VO
     */
    private AuthorVO convertToVO(Author author) {
        AuthorVO vo = new AuthorVO();
        BeanUtils.copyProperties(author, vo);
        return vo;
    }
}