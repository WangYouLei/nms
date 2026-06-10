package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import org.springframework.beans.BeanUtils;
import com.wang.common.feign.AuthorServiceFeign;
import com.wang.common.feign.SearchServiceFeign;
import com.wang.novel.event.NovelEventPublisher;
import com.wang.novel.mapper.NovelCategoryMapper;
import com.wang.novel.mapper.NovelCategoryRelationMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;
import com.wang.pojo.dto.SearchDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelCategory;
import com.wang.pojo.entity.NovelCategoryRelation;
import com.wang.pojo.entity.Author;
import com.wang.pojo.vo.AuthorDetailVO;
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
import java.util.HashMap;
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
    private final NovelCategoryMapper novelCategoryMapper;
    private final NovelCategoryRelationMapper novelCategoryRelationMapper;
    private final AuthorServiceFeign authorServiceFeign;
    private final SearchServiceFeign searchServiceFeign;
    private final NovelEventPublisher novelEventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheService cacheService;

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
                            AuthorServiceFeign authorServiceFeign,
                            SearchServiceFeign searchServiceFeign,
                            NovelEventPublisher novelEventPublisher,
                            RedisTemplate<String, Object> redisTemplate,
                            CacheService cacheService) {
        this.novelMapper = novelMapper;
        this.novelCategoryMapper = novelCategoryMapper;
        this.novelCategoryRelationMapper = novelCategoryRelationMapper;
        this.authorServiceFeign = authorServiceFeign;
        this.searchServiceFeign = searchServiceFeign;
        this.novelEventPublisher = novelEventPublisher;
        this.redisTemplate = redisTemplate;
        this.cacheService = cacheService;
    }

    // ==================== Common - 公共方法 ====================

    @Override
    public Result getNovelDetail(Long novelId) {
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
                // 检查小说是否已被逻辑删除（查DB确保一致性，缓存可能过期）
                Novel checkNovel = novelMapper.selectById(novelId);
                if (checkNovel == null || (checkNovel.getIsDel() != null && checkNovel.getIsDel())) {
                    log.warn("缓存命中但小说已被删除，清除缓存：ID={}", novelId);
                    redisTemplate.delete(cacheKey);
                    return Result.error("小说不存在");
                }
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

        // 检查小说是否已被逻辑删除
        if (novel.getIsDel() != null && novel.getIsDel()) {
            log.warn("小说已被删除：ID={}", novelId);
            // 删除缓存中的脏数据
            try {
                redisTemplate.delete(cacheKey);
            } catch (Exception e) {
                log.warn("删除缓存失败：{}", e.getMessage());
            }
            return Result.error("小说不存在");
        }

        // 3. 查询小说关联的分类（多对多，可能有多条记录）
        List<NovelCategoryVO> categoryVOList = new ArrayList<>();
        LambdaQueryWrapper<NovelCategoryRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(NovelCategoryRelation::getNovelId, novelId);
        List<NovelCategoryRelation> relations = novelCategoryRelationMapper.selectList(relationWrapper);

        if (!relations.isEmpty()) {
            // 提取所有分类ID
            List<Long> categoryIds = relations.stream()
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
     * 优先调用 search-server（ES搜索），降级回 MySQL LIKE 查询
     */
    @Override
    public Result searchNovels(NovelSearchDTO dto) {
        // 优先尝试 ES 搜索（通过 search-server）
        try {
            SearchDTO searchDTO = convertToSearchDTO(dto);
            Result esResult = searchServiceFeign.searchNovels(searchDTO);
            if (esResult.getCode() == BizCodeEnum.SUCCESS.getCode() && esResult.getData() != null) {
                log.info("ES搜索小说成功：keyword={}", dto.getKeyword());
                return esResult;
            }
            log.warn("ES搜索返回异常，降级到MySQL搜索：{}", esResult.getMsg());
        } catch (Exception e) {
            log.warn("ES搜索失败，降级到MySQL搜索：{}", e.getMessage());
        }

        // 降级：MySQL LIKE 查询
        return searchNovelsFromMySQL(dto);
    }

    /**
     * 将 NovelSearchDTO 转换为 SearchDTO
     */
    private SearchDTO convertToSearchDTO(NovelSearchDTO dto) {
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setKeyword(dto.getKeyword());
        searchDTO.setName(dto.getName());
        searchDTO.setSubName(dto.getSubName());
        searchDTO.setAuthorId(dto.getAuthorId());
        searchDTO.setIsHot(dto.getIsHot());
        searchDTO.setIsFinished(dto.getIsFinished());
        searchDTO.setCategoryId(dto.getCategoryId());
        searchDTO.setCategoryType(dto.getCategoryType());
        searchDTO.setTag(dto.getTag());
        searchDTO.setSortBy(dto.getSortBy());
        searchDTO.setPageNum(dto.getPageNum());
        searchDTO.setPageSize(dto.getPageSize());
        return searchDTO;
    }

    /**
     * MySQL LIKE 查询（降级方案）
     */
    private Result searchNovelsFromMySQL(NovelSearchDTO dto) {
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

        // 排序逻辑
        if (StringUtils.hasText(dto.getSortBy())) {
            switch (dto.getSortBy()) {
                case "collect":
                    // 按收藏数降序
                    queryWrapper.orderByDesc(Novel::getCollectCount);
                    break;
                case "word":
                    // 按字数降序
                    queryWrapper.orderByDesc(Novel::getAllWordCount);
                    break;
                case "update":
                default:
                    // 按更新时间降序（默认）
                    queryWrapper.orderByDesc(Novel::getUpdateTime);
                    break;
            }
        } else {
            // 默认按更新时间降序
            queryWrapper.orderByDesc(Novel::getUpdateTime);
        }

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
        BeanUtils.copyProperties(novelDTO, novel, "authorId", "authorName", "chapterCount");

        // 设置作者ID为当前登录用户的ID
        novel.setAuthorId(loginUser.getId());
        // 设置作者名称为当前登录用户的名称（冗余字段）
        novel.setAuthorName(loginUser.getName());
        // 设置作者头像为当前登录用户的头像（冗余字段）
        novel.setAuthorAvatar(loginUser.getAvatar());
        // 设置作者等级，默认为1（执笔者）
        novel.setAuthorRank(novelDTO.getAuthorRank() != null ? novelDTO.getAuthorRank() : 1);

        // 设置小说信息
        novel.setIsHot(false);
        novel.setIsFinished(false);
        novel.setIsDel(false);
        if(!StringUtils.hasText(novelDTO.getUrl())){
            novel.setUrl(DefaultUrlConfig.NOVEL_COVER_URL);
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
            // 发布小说创建事件，同步ES索引
            try {
                novelEventPublisher.publishNovelUpdated(novel.getId(), "CREATE");
            } catch (Exception e) {
                log.error("发布小说创建事件失败，不影响主业务：novelId={}, error={}", novel.getId(), e.getMessage());
            }
            // 更新排行榜 ZSET
            updateRankingOnNovelCreate(novel);
            return Result.success(novel);
        } else {
            log.error("新增小说失败：名称={}", novel.getName());
            return Result.error("新增失败");
        }
    }

    @Override
    public Result deleteNovel(Long id) {
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
        int result = novelMapper.updateSelective(novel);
        if (result == 1) {
            log.info("{}的ID={}, 逻辑删除小说成功：小说ID={}", loginUser.getRole(),loginUser.getId(),id);
            // 清除小说详情缓存
            try {
                redisTemplate.delete(CACHE_KEY_NOVEL_DETAIL + id);
                log.info("已清除小说详情缓存：ID={}", id);
            } catch (Exception e) {
                log.warn("清除缓存失败：{}", e.getMessage());
            }
            // 发布小说删除事件，同步ES索引
            try {
                novelEventPublisher.publishNovelUpdated(id, "DELETE");
            } catch (Exception e) {
                log.error("发布小说删除事件失败，不影响主业务：novelId={}, error={}", id, e.getMessage());
            }
            // 更新排行榜 ZSET
            updateRankingOnNovelDelete(novel);
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
        BeanUtils.copyProperties(novelDTO, novel);
        // 更新修改时间
        novel.setUpdateTime(LocalDateTime.now());

        // 执行更新操作
        int result = novelMapper.updateSelective(novel);
        if (result == 1) {
            log.info("修改小说成功：ID={}", novel.getId());
            // 发布小说更新事件，同步ES索引
            try {
                novelEventPublisher.publishNovelUpdated(novel.getId(), "UPDATE");
            } catch (Exception e) {
                log.error("发布小说更新事件失败，不影响主业务：novelId={}, error={}", novel.getId(), e.getMessage());
            }
            // 更新最新更新榜 ZSET
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_LATEST, System.currentTimeMillis(), String.valueOf(novel.getId()));
            return Result.success(novel);
        } else {
            log.error("修改小说失败：ID={}", novel.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    // ==================== Visitor - 访客端方法 ====================

    @Override
    public Result getHotNovels(Integer pageNum, Integer pageSize, Long categoryId) {
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
        Long total = novelMapper.countHotNovels(categoryId);
        if (total == null) {
            total = 0L;
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
    public Result getNovelsByCategory(Integer pageNum, Integer pageSize, Long categoryId, String sortBy, Boolean isFinished) {
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

        // 构建查询条件
        LambdaQueryWrapper<NovelCategoryRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(NovelCategoryRelation::getCategoryId, categoryId);
        List<NovelCategoryRelation> relations = novelCategoryRelationMapper.selectList(relationWrapper);

        if (relations.isEmpty()) {
            log.info("该分类下没有小说");
            return Result.success(PageResult.build(pageNum, pageSize, 0, Collections.emptyList()));
        }

        // 提取小说ID列表
        List<Long> novelIds = relations.stream()
                .map(NovelCategoryRelation::getNovelId)
                .collect(Collectors.toList());

        // 构建小说查询条件
        LambdaQueryWrapper<Novel> novelWrapper = new LambdaQueryWrapper<>();
        novelWrapper.in(Novel::getId, novelIds)
                .eq(Novel::getIsDel, false);

        // 完结状态筛选
        if (isFinished != null) {
            novelWrapper.eq(Novel::getIsFinished, isFinished);
        }

        // 排序逻辑
        if (sortBy != null) {
            switch (sortBy) {
                case "collect":
                    novelWrapper.orderByDesc(Novel::getCollectCount);
                    break;
                case "word":
                    novelWrapper.orderByDesc(Novel::getAllWordCount);
                    break;
                case "update":
                default:
                    novelWrapper.orderByDesc(Novel::getUpdateTime);
                    break;
            }
        } else {
            novelWrapper.orderByDesc(Novel::getUpdateTime);
        }

        // 分页查询
        Page<Novel> page = new Page<>(pageNum, pageSize);
        Page<Novel> result = novelMapper.selectPage(page, novelWrapper);

        // 转换为VO
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

    // ==================== 私有方法 ====================

    /**
     * 转换为列表VO
     */
    private NovelListVO convertToListVO(Novel novel) {
        NovelListVO vo = new NovelListVO();
        BeanUtils.copyProperties(novel, vo);
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private NovelDetailVO convertToDetailVO(Novel novel, List<NovelCategoryVO> categories) {
        NovelDetailVO vo = new NovelDetailVO();
        BeanUtils.copyProperties(novel, vo);
        vo.setCategories(categories);
        
        // 设置作者ID
        vo.setAuthorId(novel.getAuthorId());
        
        // 设置作者头像：优先使用冗余字段，为空时通过Feign调用author-server获取
        if (StringUtils.hasText(novel.getAuthorAvatar())) {
            vo.setAuthorAvatar(novel.getAuthorAvatar());
        } else if (novel.getAuthorId() != null) {
            try {
                Result avatarResult = authorServiceFeign.getAuthorAvatar(novel.getAuthorId());
                if (avatarResult.getCode() == BizCodeEnum.SUCCESS.getCode() && avatarResult.getData() != null) {
                    vo.setAuthorAvatar((String) avatarResult.getData());
                } else {
                    vo.setAuthorAvatar(DefaultUrlConfig.AUTHOR_AVATAR_URL);
                }
            } catch (Exception e) {
                log.warn("Feign调用获取作者头像失败，使用默认头像：authorId={}, error={}", novel.getAuthorId(), e.getMessage());
                vo.setAuthorAvatar(DefaultUrlConfig.AUTHOR_AVATAR_URL);
            }
        } else {
            vo.setAuthorAvatar(DefaultUrlConfig.AUTHOR_AVATAR_URL);
        }
        
        // 设置作者作品数量：通过Feign调用author-server获取
        if (novel.getAuthorId() != null) {
            try {
                Result basicResult = authorServiceFeign.getAuthorBasicInfo(novel.getAuthorId());
                if (basicResult.getCode() == BizCodeEnum.SUCCESS.getCode() && basicResult.getData() != null) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> basicInfo = (java.util.Map<String, Object>) basicResult.getData();
                    Object novelCount = basicInfo.get("novelCount");
                    vo.setAuthorNovelCount(novelCount != null ? ((Number) novelCount).intValue() : 0);
                } else {
                    vo.setAuthorNovelCount(0);
                }
            } catch (Exception e) {
                log.warn("Feign调用获取作者基本信息失败：authorId={}, error={}", novel.getAuthorId(), e.getMessage());
                vo.setAuthorNovelCount(0);
            }
        } else {
            vo.setAuthorNovelCount(0);
        }
        
        return vo;
    }

    /**
     * 转换为分类VO
     */
    private NovelCategoryVO convertToCategoryVO(NovelCategory category) {
        NovelCategoryVO vo = new NovelCategoryVO();
        BeanUtils.copyProperties(category, vo, "category");
        vo.setCategoryName(category.getCategory() == 1 ? "男频" : "女频");
        return vo;
    }

    /**
     * 获取等级名称
     */
    private String getRankName(Integer rank) {
        if (rank == null) {
            return "执笔者";
        }
        return switch (rank) {
            case 1 -> "执笔者";
            case 2 -> "织梦师";
            case 3 -> "造界者";
            case 4 -> "渡舟人";
            case 5 -> "燃灯者";
            default -> "执笔者";
        };
    }

    // ==================== Visitor - 作者详情 ====================

    @Override
    public Result getAuthorDetail(Long authorId, Integer pageNum, Integer pageSize) {
        // 参数校验
        if (authorId == null || authorId < 1) {
            return Result.error("作者ID无效");
        }

        // 通过Feign调用author-server获取作者基本信息
        Author author = null;
        try {
            Result basicResult = authorServiceFeign.getAuthorBasicInfo(authorId);
            if (basicResult.getCode() == BizCodeEnum.SUCCESS.getCode() && basicResult.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> basicInfo = (java.util.Map<String, Object>) basicResult.getData();
                author = new Author();
                author.setId(((Number) basicInfo.get("id")).longValue());
                author.setName((String) basicInfo.get("name"));
                author.setAvatar((String) basicInfo.get("avatar"));
                Object rankObj = basicInfo.get("rank");
                author.setRank(rankObj != null ? ((Number) rankObj).intValue() : null);
                author.setIntroduction((String) basicInfo.get("introduction"));
                Object novelCountObj = basicInfo.get("novelCount");
                author.setNovelCount(novelCountObj != null ? ((Number) novelCountObj).intValue() : null);
            }
        } catch (Exception e) {
            log.warn("Feign调用获取作者基本信息失败：authorId={}, error={}", authorId, e.getMessage());
        }

        if (author == null) {
            log.warn("作者不存在：ID={}", authorId);
            return Result.error("作者不存在");
        }

        // 分页参数校验
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

        // 查询作者作品列表
        List<Novel> novels = novelMapper.selectNovelsByAuthorId(authorId, offset, pageSize);
        Long total = novelMapper.countNovelsByAuthorIdInNovel(authorId);
        if (total == null) {
            total = 0L;
        }

        // 转换作品列表为VO
        List<NovelListVO> novelVOList = novels.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        // 构建作者详情VO
        AuthorDetailVO vo = new AuthorDetailVO();
        vo.setId(author.getId());
        vo.setName(author.getName());
        vo.setAvatar(StringUtils.hasText(author.getAvatar()) ? author.getAvatar() : DefaultUrlConfig.AUTHOR_AVATAR_URL);
        vo.setRank(author.getRank());
        vo.setRankName(getRankName(author.getRank()));
        vo.setIntroduction(author.getIntroduction());
        vo.setNovelCount(total.intValue());
        vo.setNovels(novelVOList);

        log.info("获取作者详情成功：ID={}, 作品数={}", authorId, total);
        return Result.success(vo);
    }

    @Override
    public Result getNovelAuthorId(Long novelId) {
        log.info("[内部调用] 获取小说作者ID：novelId={}", novelId);
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null || novel.getIsDel()) {
            log.warn("[内部调用] 小说不存在：novelId={}", novelId);
            return Result.error("小说不存在");
        }
        return Result.success(novel.getAuthorId());
    }

    @Override
    public Result batchGetNovelAuthorIds(List<Long> novelIds) {
        log.info("[内部调用] 批量获取小说作者ID：count={}", novelIds.size());
        if (novelIds.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        List<Novel> novels = novelMapper.selectBatchIds(novelIds);
        Map<Long, Long> authorIdMap = new HashMap<>();
        for (Novel novel : novels) {
            if (novel != null && !novel.getIsDel()) {
                authorIdMap.put(novel.getId(), novel.getAuthorId());
            }
        }
        log.info("[内部调用] 批量获取小说作者ID完成：请求{}个，返回{}个", novelIds.size(), authorIdMap.size());
        return Result.success(authorIdMap);
    }

    @Override
    public Result getNovelBasicInfo(Long novelId) {
        log.info("[内部调用] 获取小说基本信息：novelId={}", novelId);
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null || novel.getIsDel()) {
            log.warn("[内部调用] 小说不存在：novelId={}", novelId);
            return Result.error("小说不存在");
        }

        java.util.Map<String, Object> basicInfo = new java.util.HashMap<>();
        basicInfo.put("id", novel.getId());
        basicInfo.put("name", novel.getName());
        basicInfo.put("subName", novel.getSubName());
        basicInfo.put("url", novel.getUrl());
        basicInfo.put("tags", novel.getTags());
        basicInfo.put("introduction", novel.getIntroduction());
        basicInfo.put("authorId", novel.getAuthorId());
        basicInfo.put("authorName", novel.getAuthorName());
        basicInfo.put("authorAvatar", novel.getAuthorAvatar());
        basicInfo.put("authorRank", novel.getAuthorRank());
        basicInfo.put("chapterCount", novel.getChapterCount());
        basicInfo.put("allWordCount", novel.getAllWordCount());
        basicInfo.put("collectCount", novel.getCollectCount());
        basicInfo.put("isFinished", novel.getIsFinished());
        basicInfo.put("isHot", novel.getIsHot());
        basicInfo.put("isDel", novel.getIsDel());
        basicInfo.put("updateTime", novel.getUpdateTime());

        // 查询关联的分类信息
        List<Long> categoryIds = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();
        List<String> categoryNamesKeyword = new ArrayList<>();
        Integer categoryType = null;

        LambdaQueryWrapper<NovelCategoryRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(NovelCategoryRelation::getNovelId, novelId);
        List<NovelCategoryRelation> relations = novelCategoryRelationMapper.selectList(relationWrapper);

        if (!relations.isEmpty()) {
            List<Long> catIds = relations.stream()
                    .map(NovelCategoryRelation::getCategoryId)
                    .collect(Collectors.toList());
            List<NovelCategory> categories = novelCategoryMapper.selectBatchIds(catIds);
            for (NovelCategory cat : categories) {
                categoryIds.add(cat.getId());
                categoryNames.add(cat.getType());
                categoryNamesKeyword.add(cat.getType());
                // 取第一个分类的频道类型
                if (categoryType == null && cat.getCategory() != null) {
                    categoryType = cat.getCategory();
                }
            }
        }

        basicInfo.put("categoryIds", categoryIds);
        basicInfo.put("categoryNames", categoryNames);
        basicInfo.put("categoryNamesKeyword", categoryNamesKeyword);
        basicInfo.put("categoryType", categoryType);

        return Result.success(basicInfo);
    }

    /**
     * 小说创建时更新排行榜 ZSET
     */
    private void updateRankingOnNovelCreate(Novel novel) {
        try {
            // 新书榜：score = 创建时间戳
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_NEW,
                    novel.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    String.valueOf(novel.getId()));
            // 最新更新榜：score = 更新时间戳
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_LATEST,
                    novel.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    String.valueOf(novel.getId()));
            // 连载榜：score = 章节数（初始为0）
            if (!novel.getIsFinished()) {
                cacheService.zAdd(CacheConstants.RANKING_NOVEL_ONGOING,
                        novel.getChapterCount() != null ? novel.getChapterCount() : 0,
                        String.valueOf(novel.getId()));
            }
            // 收藏榜：score = 收藏数（初始为0）
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_COLLECT,
                    novel.getCollectCount() != null ? novel.getCollectCount() : 0,
                    String.valueOf(novel.getId()));
            // 作者高产榜：score = 作品数+1
            cacheService.zIncrBy(CacheConstants.RANKING_AUTHOR_PRODUCTIVE, 1, String.valueOf(novel.getAuthorId()));
            log.info("排行榜ZSET更新成功（小说创建）：novelId={}", novel.getId());
        } catch (Exception e) {
            log.error("排行榜ZSET更新失败（小说创建）：novelId={}, error={}", novel.getId(), e.getMessage());
        }
    }

    /**
     * 小说删除时更新排行榜 ZSET
     */
    private void updateRankingOnNovelDelete(Novel novel) {
        try {
            String novelIdStr = String.valueOf(novel.getId());
            // 从所有小说排行榜中移除
            cacheService.zRem(CacheConstants.RANKING_NOVEL_COLLECT, novelIdStr);
            cacheService.zRem(CacheConstants.RANKING_NOVEL_ONGOING, novelIdStr);
            cacheService.zRem(CacheConstants.RANKING_NOVEL_LATEST, novelIdStr);
            cacheService.zRem(CacheConstants.RANKING_NOVEL_NEW, novelIdStr);
            // 作者高产榜：score = 作品数-1
            cacheService.zIncrBy(CacheConstants.RANKING_AUTHOR_PRODUCTIVE, -1, String.valueOf(novel.getAuthorId()));
            log.info("排行榜ZSET更新成功（小说删除）：novelId={}", novel.getId());
        } catch (Exception e) {
            log.error("排行榜ZSET更新失败（小说删除）：novelId={}, error={}", novel.getId(), e.getMessage());
        }
    }
}