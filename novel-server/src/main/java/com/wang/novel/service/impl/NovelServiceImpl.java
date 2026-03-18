package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.vo.NovelDetailVO;
import com.wang.pojo.vo.NovelListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 小说服务实现类
 * 提供 author、manager、visitor 三个端口共用的小说功能
 */
@Service
@Slf4j
public class NovelServiceImpl implements NovelService {

    private final NovelMapper novelMapper;
    private final DefaultUrlConfig defaultUrlConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存Key前缀
    private static final String CACHE_KEY_NOVEL_DETAIL = "novel:detail:";
    private static final String CACHE_KEY_NOVEL_HOT = "novel:hot:";

    // 缓存过期时间（秒）
    private static final long NOVEL_HOT_TTL = 86400L;     // 热门小说缓存1天
    private static final long NOVEL_DETAIL_TTL = 1800L;   // 小说详情缓存30分钟

    // 分页参数限制
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public NovelServiceImpl(NovelMapper novelMapper,
                            DefaultUrlConfig defaultUrlConfig,
                            RedisTemplate<String, Object> redisTemplate) {
        this.novelMapper = novelMapper;
        this.defaultUrlConfig = defaultUrlConfig;
        this.redisTemplate = redisTemplate;
    }

    // ==================== Common - 公共方法 ====================

    @Override
    public Result getNovelDetail(Integer novelId) {
        log.info("[Common] 获取小说详情：ID={}", novelId);

        if (novelId == null || novelId < 1) {
            return Result.error("小说ID无效");
        }

        // 构建缓存Key
        String cacheKey = CACHE_KEY_NOVEL_DETAIL + novelId;

        // 1. 先查Redis缓存
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("从缓存获取小说详情：ID={}", novelId);
                return Result.success(cached);
            }
        } catch (Exception e) {
            log.warn("读取Redis缓存失败：{}", e.getMessage());
        }

        // 2. 缓存未命中，查询数据库
        log.info("缓存未命中，从数据库查询小说详情：ID={}", novelId);
        Map<String, Object> detail = novelMapper.selectNovelDetail(novelId);
        if (detail == null || detail.isEmpty()) {
            return Result.error("小说不存在");
        }

        NovelDetailVO vo = convertToDetailVO(detail);

        // 3. 将结果存入Redis缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, NOVEL_DETAIL_TTL, TimeUnit.SECONDS);
            log.info("小说详情已缓存：key={}, ttl={}秒", cacheKey, NOVEL_DETAIL_TTL);
        } catch (Exception e) {
            log.warn("写入Redis缓存失败：{}", e.getMessage());
        }

        return Result.success(vo);
    }

    // ==================== Author/Manager - 作者/管理端方法 ====================

    @Override
    public Result addNovel(NovelDTO novelDTO) {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        log.info("[Author/Manager] 新增小说：名称={}, 作者ID={}", novelDTO.getName(), loginUser.getId());

        Novel novel = new Novel();
        BeanUtils.copyProperties(novelDTO, novel);
        // 设置作者ID为当前登录用户的ID
        novel.setAuthorId(loginUser.getId());
        // 设置作者名称为当前登录用户的名称（冗余字段）
        novel.setAuthorName(loginUser.getName());

        // 设置小说信息
        novel.setName(novelDTO.getName());
        novel.setTags(novelDTO.getTags());
        novel.setIntroduction(novelDTO.getIntroduction());
        novel.setIsHot(false);
        novel.setIsFinished(false);
        novel.setIfDel(false);

        // 设置创建时间和修改时间
        novel.setCreateTime(LocalDateTime.now());
        novel.setUpdateTime(LocalDateTime.now());

        // 检查小说名称是否已存在
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

        // 执行逻辑删除操作
        novel.setIfDel(true);
        int result = novelMapper.updateById(novel);
        if (result == 1) {
            log.info("[Author/Manager] 逻辑删除小说成功：ID={}", id);
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("[Author/Manager] 逻辑删除小说失败：ID={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    public Result getNovelList(Integer pageNum, Integer pageSize) {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        log.info("[Author/Manager] 分页查询小说列表：页码={}, 每页数量={}, 作者ID={}", pageNum, pageSize, loginUser.getId());

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

    @Override
    public Result searchNovels(NovelSearchDTO dto) {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();

        // 参数校验
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        log.info("[Author/Manager] 搜索小说：dto={}, 作者ID={}", dto, loginUser.getId());

        // 创建分页对象
        Page<Novel> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Novel::getAuthorId, loginUser.getId());

        // 添加名称模糊查询条件
        if (StringUtils.hasText(dto.getName())) {
            queryWrapper.like(Novel::getName, dto.getName());
        }

        // 添加副名称模糊查询条件
        if (StringUtils.hasText(dto.getSubName())) {
            queryWrapper.like(Novel::getSubName, dto.getSubName());
        }

        // 添加是否热门条件
        if (dto.getIsHot() != null) {
            queryWrapper.eq(Novel::getIsHot, dto.getIsHot());
        }

        // 添加是否完结条件
        if (dto.getIsFinished() != null) {
            queryWrapper.eq(Novel::getIsFinished, dto.getIsFinished());
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

        log.info("搜索小说成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    @Override
    public Result updateNovel(NovelDTO novelDTO) {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.THREAD_LOCAL.get();
        log.info("[Author/Manager] 修改小说：ID={}, 名称={}, 作者ID={}", novelDTO.getId(), novelDTO.getName(), loginUser.getId());

        // 检查小说是否存在
        Novel existingNovel = novelMapper.selectById(novelDTO.getId());
        if (existingNovel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 检查是否属于当前登录用户
        if (!Objects.equals(existingNovel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
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

    // ==================== Visitor - 访客端方法 ====================

    @Override
    public Result getVisitorNovelList(Integer pageNum, Integer pageSize, String keyword) {
        log.info("[Visitor] 查询小说列表：页码={}, 每页数量={}, 关键词={}", pageNum, pageSize, keyword);

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        Page<Novel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like(Novel::getName, keyword)
                    .or().like(Novel::getSubName, keyword)
                    .or().like(Novel::getTags, keyword));
        }

        queryWrapper.orderByDesc(Novel::getUpdateTime);

        Page<Novel> result = novelMapper.selectPage(page, queryWrapper);

        List<NovelListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageResult<NovelListVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                (int) result.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getHotNovels(Integer pageNum, Integer pageSize, Integer categoryId) {
        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        try {
            Object object = redisTemplate.opsForValue().get(CACHE_KEY_NOVEL_HOT + categoryId + ":" + pageNum + ":" + pageSize);
            if (object != null) {
                log.info("[Visitor] 从缓存获取热门小说：categoryId={}, pageNum={}, pageSize={}", categoryId, pageNum, pageSize);
                return Result.success(object);
            }
        } catch (Exception e) {
            log.warn("读取Redis缓存失败：{}", e.getMessage());
        }
        log.info("[Visitor] 获取缓存失败，开始从数据库中加载, categoryId={}, pageNum={}, pageSize={}", categoryId, pageNum, pageSize);

        int offset = (pageNum - 1) * pageSize;

        // SQL层面分页查询
        List<Novel> novels = novelMapper.selectHotNovelsByPage(categoryId, offset, pageSize);

        // 查询总数
        Integer total = novelMapper.countHotNovels(categoryId);
        if (total == null) {
            total = 0;
        }

        List<NovelListVO> voList = novels.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageResult<NovelListVO> pageResult = PageResult.build(pageNum, pageSize, total, voList);

        try {
            redisTemplate.opsForValue().set(CACHE_KEY_NOVEL_HOT + categoryId + ":" + pageNum + ":" + pageSize, pageResult, NOVEL_HOT_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入Redis缓存失败：{}", e.getMessage());
        }

        return Result.success(pageResult);
    }

    @Override
    public Result getNovelsByCategory(Integer pageNum, Integer pageSize, Integer categoryId) {
        log.info("[Visitor] 按分类查询小说：分类ID={}", categoryId);

        if (categoryId == null) {
            return Result.error("分类ID不能为空");
        }

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        int offset = (pageNum - 1) * pageSize;

        // SQL层面分页查询
        List<Novel> novels = novelMapper.selectNovelsByCategoryId(categoryId, offset, pageSize);

        // 查询总数
        Integer total = novelMapper.countNovelsByCategoryId(categoryId);
        if (total == null) {
            total = 0;
        }

        // 空数据返回空列表，不是错误
        if (novels.isEmpty()) {
            log.info("该分类下没有小说");
            return Result.success(PageResult.build(pageNum, pageSize, 0, Collections.emptyList()));
        }

        List<NovelListVO> voList = novels.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageResult<NovelListVO> pageResult = PageResult.build(pageNum, pageSize, total, voList);

        return Result.success(pageResult);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为列表VO
     */
    private NovelListVO convertToListVO(Novel novel) {
        NovelListVO vo = new NovelListVO();
        vo.setId(novel.getId());
        vo.setName(novel.getName());
        vo.setSubName(novel.getSubName());
        vo.setTags(novel.getTags());
        vo.setUrl(novel.getUrl());
        vo.setAuthorName(novel.getAuthorName());
        vo.setUpdateTime(novel.getUpdateTime());
        vo.setChapterCount(novel.getChapterCount());
        vo.setIsFinished(novel.getIsFinished());
        vo.setIsHot(novel.getIsHot());
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private NovelDetailVO convertToDetailVO(Map<String, Object> detail) {
        NovelDetailVO vo = new NovelDetailVO();
        vo.setId(getIntValue(detail, "id"));
        vo.setName(getStringValue(detail, "name"));
        vo.setSubName(getStringValue(detail, "sub_name"));
        vo.setTags(getStringValue(detail, "tags"));
        vo.setIntroduction(getStringValue(detail, "introduction"));
        vo.setUrl(getStringValue(detail, "url"));
        vo.setAuthorName(getStringValue(detail, "author_name"));
        vo.setChapterCount(getIntValue(detail, "chapter_count"));
        Object isFinished = detail.get("is_finished");
        vo.setIsFinished(isFinished != null && getIntValue(detail, "is_finished") == 1);
        return vo;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}