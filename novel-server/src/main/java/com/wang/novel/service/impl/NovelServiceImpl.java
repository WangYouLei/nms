package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.common.utils.CopyPropertiesUtil;
import com.wang.novel.mapper.AuthorMapper;
import com.wang.novel.mapper.NovelCategoryMapper;
import com.wang.novel.mapper.NovelCategoryRelationMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelCategory;
import com.wang.pojo.entity.NovelCategoryRelation;
import com.wang.pojo.vo.NovelCategoryVO;
import com.wang.pojo.vo.NovelDetailVO;
import com.wang.pojo.vo.NovelListVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final NovelCategoryMapper novelCategoryMapper;
    private final NovelCategoryRelationMapper novelCategoryRelationMapper;
    private final AuthorMapper authorMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultUrlConfig defaultUrlConfig;

    // 缓存Key前缀
    private static final String CACHE_KEY_NOVEL_DETAIL = "novel:detail:";
    private static final String CACHE_KEY_NOVEL_HOT = "novel:hot:";

    // 缓存过期时间（秒）
    // 热门小说缓存1天
    private static final long NOVEL_HOT_TTL = 86400L;
    // 小说详情缓存30分钟
    private static final long NOVEL_DETAIL_TTL = 1800L;

    // 分页参数限制
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public NovelServiceImpl(NovelMapper novelMapper,
                            NovelCategoryMapper novelCategoryMapper,
                            NovelCategoryRelationMapper novelCategoryRelationMapper,
                            AuthorMapper authorMapper,
                            RedisTemplate<String, Object> redisTemplate,
                            DefaultUrlConfig defaultUrlConfig) {
        this.novelMapper = novelMapper;
        this.novelCategoryMapper = novelCategoryMapper;
        this.novelCategoryRelationMapper = novelCategoryRelationMapper;
        this.authorMapper = authorMapper;
        this.redisTemplate = redisTemplate;
        this.defaultUrlConfig = defaultUrlConfig;
    }

    // ==================== Common - 公共方法 ====================

    @Override
    public Result getNovelDetail(Integer novelId) {
        // 参数校验
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

        Novel novel = novelMapper.selectById(novelId);
        if (novel == null) {
            log.warn("小说不存在：ID={}", novelId);
            return Result.error("小说不存在");
        }

        // 3. 查询小说关联的分类（多对多，可能有多条记录）
        List<NovelCategoryVO> categoryVOList = new ArrayList<>();
        LambdaQueryWrapper<NovelCategoryRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(NovelCategoryRelation::getNovelId, novelId);
        List<NovelCategoryRelation> relations = novelCategoryRelationMapper.selectList(relationWrapper);

        if (!relations.isEmpty()) {
            // 提取所有分类ID
            List<Integer> categoryIds = relations.stream()
                    .map(NovelCategoryRelation::getCategoryId)
                    .collect(Collectors.toList());
            // 批量查询分类
            List<NovelCategory> categories = novelCategoryMapper.selectBatchIds(categoryIds);
            categoryVOList = categories.stream()
                    .map(this::convertToCategoryVO)
                    .collect(Collectors.toList());
        }

        // 4. 构建VO
        NovelDetailVO vo = convertToDetailVO(novel, categoryVOList);

        // 5. 将结果存入Redis缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, NOVEL_DETAIL_TTL, TimeUnit.SECONDS);
            log.info("小说详情已缓存：key={}, ttl={}秒", cacheKey, NOVEL_DETAIL_TTL);
        } catch (Exception e) {
            log.warn("写入Redis缓存失败：{}", e.getMessage());
        }

        return Result.success(vo);
    }

    /**
     * 统一搜索小说列表
     * 权限控制：
     * - Author: 只能搜索自己的小说
     * - Manager/Visitor: 可以搜索所有小说，支持按authorId筛选
     * 搜索条件：
     * - keyword: 关键词搜索（模糊匹配名称、副名称、标签）
     * - name/subName/isHot/isFinished: 精确条件筛选
     * - authorId: 按作者ID筛选（仅Manager/Visitor可用）
     */
    @Override
    public Result searchNovels(NovelSearchDTO dto) {
        // 参数校验
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // 创建分页对象
        Page<Novel> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();

        // 获取当前登录用户（可能为空，Visitor无需登录）
        LoginUser loginUser = RoleContextUtil.getCurrentUser();

        // 权限过滤：Author只能搜索自己的小说，Manager/Visitor可以搜索所有小说
        if (loginUser != null && UserRole.AUTHOR.equals(loginUser.getRole())) {
            queryWrapper.eq(Novel::getAuthorId, loginUser.getId());
            log.info("[Author] 搜索小说：作者ID={}", loginUser.getId());
        } else {
            // Manager/Visitor 可以按 authorId 筛选
            if (dto.getAuthorId() != null) {
                queryWrapper.eq(Novel::getAuthorId, dto.getAuthorId());
                log.info("[Manager/Visitor] 搜索小说：按作者ID={}筛选", dto.getAuthorId());
            } else {
                log.info("[Manager/Visitor] 搜索小说：查询全部");
            }
        }

        // 关键词搜索（模糊匹配名称、副名称、标签）
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(w -> w.like(Novel::getName, dto.getKeyword())
                    .or().like(Novel::getSubName, dto.getKeyword())
                    .or().like(Novel::getTags, dto.getKeyword()));
        }

        // 精确条件筛选
        if (StringUtils.hasText(dto.getName())) {
            queryWrapper.like(Novel::getName, dto.getName());
        }

        if (StringUtils.hasText(dto.getSubName())) {
            queryWrapper.like(Novel::getSubName, dto.getSubName());
        }

        if (dto.getIsHot() != null) {
            queryWrapper.eq(Novel::getIsHot, dto.getIsHot());
        }

        if (dto.getIsFinished() != null) {
            queryWrapper.eq(Novel::getIsFinished, dto.getIsFinished());
        }

        queryWrapper.orderByDesc(Novel::getUpdateTime);

        // 执行分页查询
        Page<Novel> result = novelMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<NovelListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<NovelListVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                (int) result.getTotal(),
                voList
        );

        log.info("搜索小说成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    // ==================== Author - 作者端方法 ====================

    @Override
    public Result addNovel(NovelDTO novelDTO) {
        // 获取当前登录用户信息
        LoginUser loginUser = RoleContextUtil.getCurrentUser();


        Novel novel = new Novel();

        // 将DTO中的非空属性复制到实体对象中, 忽略空属性作者id，作者姓名，小说章节数
        CopyPropertiesUtil.copyNonNullProperties(novelDTO, novel,"authorId","authorName","chapterCount");

        // 设置作者ID为当前登录用户的ID
        novel.setAuthorId(loginUser.getId());
        // 设置作者名称为当前登录用户的名称（冗余字段）
        novel.setAuthorName(loginUser.getName());
        // 设置作者头像为当前登录用户的头像（冗余字段）
        novel.setAuthorAvatar(loginUser.getAvatar());

        // 设置小说信息
        novel.setIsHot(false);
        novel.setIsFinished(false);
        novel.setIsDel(false);
        if(!StringUtils.hasText(novelDTO.getUrl())){
            novel.setUrl(defaultUrlConfig.getNovelCoverUrl());
        }

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
        LoginUser loginUser = RoleContextUtil.getCurrentUser();

        // 检查小说是否存在
        Novel novel = novelMapper.selectById(id);
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 权限校验：作者只能删除自己的小说，管理员可以删除所有小说
        boolean isManager = UserRole.MANAGER.equals(loginUser.getRole());
        boolean isAuthor = UserRole.AUTHOR.equals(loginUser.getRole());
        
        if (isAuthor && !Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }
        if (!isManager && !isAuthor) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 执行逻辑删除操作
        novel.setIsDel(true);
        int result = novelMapper.updateById(novel);
        if (result == 1) {
            log.info("{}的ID={}, 逻辑删除小说成功：小说ID={}", loginUser.getRole(),loginUser.getId(),id);
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("{}的ID={},逻辑删除小说失败：小说ID={}",  loginUser.getRole(),loginUser.getId(),id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    public Result updateNovel(NovelDTO novelDTO) {
        // 获取当前登录用户信息
        LoginUser loginUser = RoleContextUtil.getCurrentUser();


        // 检查小说是否存在
        Novel existingNovel = novelMapper.selectById(novelDTO.getId());
        if (existingNovel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 检查是否属于当前登录作者
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
        CopyPropertiesUtil.copyNonNullProperties(novelDTO, novel);
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
        CopyPropertiesUtil.copyNonNullProperties(novel, vo);
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private NovelDetailVO convertToDetailVO(Novel novel, List<NovelCategoryVO> categories) {
        NovelDetailVO vo = new NovelDetailVO();
        CopyPropertiesUtil.copyNonNullProperties(novel, vo);
        vo.setCategories(categories);
        
        // 设置作者ID
        vo.setAuthorId(novel.getAuthorId());
        
        // 设置作者头像：优先使用冗余字段，为空时从author表查询
        if (StringUtils.hasText(novel.getAuthorAvatar())) {
            vo.setAuthorAvatar(novel.getAuthorAvatar());
        } else if (novel.getAuthorId() != null) {
            String avatar = authorMapper.selectAvatarById(novel.getAuthorId());
            vo.setAuthorAvatar(avatar != null ? avatar : defaultUrlConfig.getAuthorAvatarUrl());
        } else {
            vo.setAuthorAvatar(defaultUrlConfig.getAuthorAvatarUrl());
        }
        
        return vo;
    }

    /**
     * 转换为分类VO
     */
    private NovelCategoryVO convertToCategoryVO(NovelCategory category) {
        NovelCategoryVO vo = new NovelCategoryVO();
        CopyPropertiesUtil.copyNonNullProperties(category, vo, "category");
        vo.setCategoryName(category.getCategory() == 1 ? "男频" : "女频");
        return vo;
    }
}