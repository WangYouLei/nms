package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.constants.CacheConstants;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.common.service.CacheService;
import com.wang.common.utils.RoleContextUtil;
import com.wang.novel.mapper.NovelMapper;
import com.wang.pojo.dto.NovelSearchDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.vo.NovelListVO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 小说降级搜索服务
 * 当 ES 搜索不可用时，回退到 MySQL LIKE 查询
 * 使用 Resilience4j 三重保护：RateLimiter + CircuitBreaker + Bulkhead，防止 MySQL 被打爆
 */
@Service
@Slf4j
public class NovelSearchFallbackService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final NovelMapper novelMapper;
    private final CacheService cacheService;

    public NovelSearchFallbackService(NovelMapper novelMapper, CacheService cacheService) {
        this.novelMapper = novelMapper;
        this.cacheService = cacheService;
    }

    /**
     * MySQL LIKE 查询（降级方案）
     * 三重保护：
     * - RateLimiter: 限制 QPS，防止雪崩
     * - CircuitBreaker: DB 慢/异常时熔断，快速失败
     * - Bulkhead: 限制并发，保护连接池
     */
    @RateLimiter(name = "novelSearchRateLimiter", fallbackMethod = "searchFallback")
    @CircuitBreaker(name = "novelSearchCircuitBreaker", fallbackMethod = "searchFallback")
    @Bulkhead(name = "novelSearchBulkhead", fallbackMethod = "searchFallback")
    public Result searchNovelsFromMySQL(NovelSearchDTO dto) {
        // 1. 参数校验
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // 2. 获取当前登录用户（用于权限过滤 + 缓存 Key 隔离）
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        String role = (loginUser != null && loginUser.getRole() != null)
                ? loginUser.getRole().name() : "VISITOR";
        Long userId = loginUser != null ? loginUser.getId() : null;

        // 3. 构建缓存 Key（必须含 role + userId 防越权）
        String dtoHash = buildDtoHash(dto, pageNum, pageSize);
        String cacheKey = CacheConstants.buildNovelSearchKey(role, userId, dtoHash);

        // 4. 查缓存（使用类型安全重载，避免反序列化为 LinkedHashMap）
        try {
            PageResult<?> cached = cacheService.get(cacheKey, PageResult.class);
            if (cached != null) {
                log.info("[降级搜索] 缓存命中：key={}", cacheKey);
                return Result.success(cached);
            }
        } catch (Exception e) {
            log.warn("[降级搜索] 读取缓存失败：{}", e.getMessage());
        }

        // 5. 构建查询条件
        LambdaQueryWrapper<Novel> queryWrapper = buildQueryWrapper(dto, loginUser);

        // 6. 执行分页查询
        Page<Novel> page = new Page<>(pageNum, pageSize);
        Page<Novel> result = novelMapper.selectPage(page, queryWrapper);

        // 7. 转换为 VO
        List<NovelListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageResult<NovelListVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                (int) result.getTotal(),
                voList
        );

        // 8. 回写缓存
        try {
            cacheService.set(cacheKey, pageResult, CacheConstants.NOVEL_SEARCH_TTL);
        } catch (Exception e) {
            log.warn("[降级搜索] 写入缓存失败：{}", e.getMessage());
        }

        log.info("[降级搜索] 查询成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    /**
     * Resilience4j 兜底方法：限流/熔断/隔离触发时返回空列表
     * 签名必须与原方法一致，多一个 Throwable 末尾参数
     */
    private Result searchFallback(NovelSearchDTO dto, Throwable t) {
        log.warn("[降级搜索] 触发 Resilience4j 保护，返回空结果：type={}, msg={}",
                t.getClass().getSimpleName(), t.getMessage());
        // 使用用户请求的页码，避免前端误判当前页
        int pageNum = (dto.getPageNum() != null && dto.getPageNum() > 0) ? dto.getPageNum() : 1;
        int pageSize = (dto.getPageSize() != null && dto.getPageSize() > 0 && dto.getPageSize() <= MAX_PAGE_SIZE)
                ? dto.getPageSize() : DEFAULT_PAGE_SIZE;
        PageResult<NovelListVO> empty = PageResult.build(pageNum, pageSize, 0, List.of());
        return Result.success(empty);
    }

    /**
     * 构建查询条件（从 NovelServiceImpl 迁移，逻辑保持一致）
     */
    private LambdaQueryWrapper<Novel> buildQueryWrapper(NovelSearchDTO dto, LoginUser loginUser) {
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();

        // 权限过滤：Author 只能搜索自己的小说，Manager/Visitor 可以搜索所有小说
        if (loginUser != null && UserRole.AUTHOR.equals(loginUser.getRole())) {
            queryWrapper.eq(Novel::getAuthorId, loginUser.getId());
            log.info("[Author] 降级搜索小说：作者ID={}", loginUser.getId());
        } else {
            // Manager/Visitor 可以按 authorId 筛选
            if (dto.getAuthorId() != null) {
                queryWrapper.eq(Novel::getAuthorId, dto.getAuthorId());
                log.info("[Manager/Visitor] 降级搜索小说：按作者ID={}筛选", dto.getAuthorId());
            } else {
                log.info("[Manager/Visitor] 降级搜索小说：查询全部");
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

        return queryWrapper;
    }

    /**
     * 基于 DTO 关键字段计算 hash（用于缓存 Key）
     * 注意：不含 categoryId/categoryType/tag，因为原 MySQL 降级方法未使用这三个字段（只用于 ES）
     */
    private String buildDtoHash(NovelSearchDTO dto, int pageNum, int pageSize) {
        return Objects.hash(
                dto.getKeyword(), dto.getName(), dto.getSubName(),
                dto.getAuthorId(), dto.getIsHot(), dto.getIsFinished(),
                dto.getSortBy(), pageNum, pageSize
        ) + "";
    }

    /**
     * 转换为列表 VO（与 NovelServiceImpl.convertToListVO 等价，本地副本避免循环依赖）
     */
    private NovelListVO convertToListVO(Novel novel) {
        NovelListVO vo = new NovelListVO();
        BeanUtils.copyProperties(novel, vo);
        return vo;
    }
}
