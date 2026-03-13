package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manage.mapper.NovelMapper;
import com.wang.manage.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 小说服务实现类
 */
@Service
@Slf4j
public class NovelServiceImpl implements NovelService {


    private final NovelMapper novelMapper;

    @Autowired
    public NovelServiceImpl(NovelMapper novelMapper) {
        this.novelMapper = novelMapper;
    }

    /**
     * 新增小说
     *
     * @param novelDTO 小说信息
     * @return 操作结果
     */
    @Override
    public Result addNovel(NovelDTO novelDTO) {

        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        log.info("新增小说：名称={}, 作者ID={}", novelDTO.getName(), loginUser.getId());

        Novel novel = new Novel();
        BeanUtils.copyProperties(novelDTO, novel);
        // 设置作者ID为当前登录用户的ID
        novel.setAuthorId(loginUser.getId());
        // 设置作者名称为当前登录用户的名称（冗余字段）
        novel.setAuthorName(loginUser.getName());

        // 设置创建时间和修改时间
        novel.setCreateTime(LocalDateTime.now());
        novel.setUpdateTime(LocalDateTime.now());

        // 检查小说名称是否已存在   这一步是建议写的，即使数据库层面已经有唯一性约束
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Novel::getAuthorId, loginUser.getId())
                .eq(Novel::getName, novel.getName());
        if (novelMapper.selectCount(queryWrapper) > 0) {
            return Result.buildResult(BizCodeEnum.NOVEL_TITLE_EXIST);
        }

        // 执行插入操作
        int result = novelMapper.insert(novel);
        if (result == 1) {
            log.info("新增小说成功：ID={}", novel.getId());
            return Result.success(novel);
        } else {
            log.error("新增小说失败：名称={}", novel.getName());
            return Result.error("新增失败");
        }

    }

    /**
     * 根据ID删除小说
     *
     * @param id 小说ID
     * @return 操作结果
     */
    @Override
    public Result deleteNovel(Integer id) {

        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        // 检查小说是否存在
        Novel novel = novelMapper.selectById(id);
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 检查是否属于当前登录用户
        if (!Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 执行删除操作
        int result = novelMapper.deleteById(id);
        if (result == 1) {
            log.info("删除小说成功：ID={}", id);
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("删除小说失败：ID={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }

    }

    /**
     * 分页查询当前登录作者的小说
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    @Override
    public Result getNovelList(Integer pageNum, Integer pageSize) {

        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        log.info("分页查询小说列表：页码={}, 每页数量={}, 作者ID={}", pageNum, pageSize, loginUser.getId());

        // 创建分页对象
        Page<Novel> page = new Page<>(pageNum, pageSize);

        // 创建查询条件，只查询当前登录用户的小说
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Novel::getAuthorId, loginUser.getId())
                .orderByDesc(Novel::getUpdateTime);

        // 执行分页查询
        Page<Novel> result = novelMapper.selectPage(page, queryWrapper);

        // 构建分页结果
        PageResult<Novel> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );

        log.info("分页查询小说列表成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);

    }

    /**
     * 根据小说名称或副名称进行模糊查询
     *
     * @param name     小说名称
     * @param subName  小说副名称
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    @Override
    public Result searchNovels(String name, String subName, Integer pageNum, Integer pageSize) {

        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        log.info("模糊查询小说：名称={}, 副名称={}, 页码={}, 每页数量={}, 作者ID={}",
                name, subName, pageNum, pageSize, loginUser.getId());

        // 创建分页对象
        Page<Novel> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Novel::getAuthorId, loginUser.getId());

        // 添加名称模糊查询条件
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Novel::getName, name);
        }

        // 添加副名称模糊查询条件
        if (StringUtils.hasText(subName)) {
            queryWrapper.like(Novel::getSubName, subName);
        }

        queryWrapper.orderByDesc(Novel::getUpdateTime);

        // 执行分页查询
        Page<Novel> result = novelMapper.selectPage(page, queryWrapper);

        // 构建分页结果
        PageResult<Novel> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );

        log.info("模糊查询小说成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);

    }

    /**
     * 修改小说信息
     *
     * @param novelDTO 小说信息
     * @return 操作结果
     */
    @Override
    public Result updateNovel(NovelDTO novelDTO) {

        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();
        log.info("修改小说：ID={}, 名称={}, 作者ID={}", novelDTO.getId(), novelDTO.getName(), loginUser.getId());

        // 检查小说是否存在
        Novel existingNovel = novelMapper.selectById(novelDTO.getId());
        if (existingNovel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 检查是否属于当前登录用户
        if (!Objects.equals(existingNovel.getAuthorId(), loginUser.getId())) {
            return Result.error("无权修改他人的小说");
        }

        // 检查小说名称是否已被其他小说使用
        if (!Objects.equals(novelDTO.getName(), existingNovel.getName())) {
            LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Novel::getAuthorId, loginUser.getId())
                    .eq(Novel::getName, novelDTO.getName())
                    .ne(Novel::getId, novelDTO.getId());
            if (novelMapper.selectCount(queryWrapper) > 0) {
                return Result.buildResult(BizCodeEnum.NOVEL_TITLE_EXIST);
            }
        }

        Novel novel = new Novel();
        BeanUtils.copyProperties(novelDTO, novel);
        // 更新修改时间
        novel.setUpdateTime(LocalDateTime.now());

        // 执行更新操作
        int result = novelMapper.updateById(novel);
        if (result == 1) {
            log.info("修改小说成功：ID={}", novel.getId());
            return Result.success(novel);
        } else {
            log.error("修改小说失败：ID={}", novel.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }

    }
}
