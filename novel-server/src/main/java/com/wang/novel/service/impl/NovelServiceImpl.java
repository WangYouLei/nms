package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.mapper.StatisticsMapper;
import com.wang.novel.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.vo.AuthorRankingVO;
import com.wang.pojo.vo.AuthorStatisticsVO;
import com.wang.pojo.vo.NovelDetailVO;
import com.wang.pojo.vo.NovelListVO;
import com.wang.pojo.vo.NovelRankingVO;
import com.wang.pojo.vo.NovelStatisticsVO;
import com.wang.pojo.vo.TrendVO;
import com.wang.pojo.vo.VisitorStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    private final StatisticsMapper statisticsMapper;
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
                            StatisticsMapper statisticsMapper,
                            RedisTemplate<String, Object> redisTemplate) {
        this.novelMapper = novelMapper;
        this.statisticsMapper = statisticsMapper;
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

        log.info("[Author] 新增小说：名称={}, 作者ID={}", novelDTO.getName(), loginUser.getId());

        // 权限校验：只有作者可以添加小说
        if (!UserRole.AUTHOR.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

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
        novel.setIfDel(true);
        int result = novelMapper.updateById(novel);
        if (result == 1) {
            log.info("{}, 逻辑删除小说成功：ID={}", loginUser.getRole(),id);
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("{},逻辑删除小说失败：ID={}", loginUser.getRole(),id);
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

        log.info("[Author/Manager] 分页查询小说列表：页码={}, 每页数量={}, 用户角色={}", pageNum, pageSize, loginUser.getRole());

        // 创建分页对象
        Page<Novel> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        
        // 权限过滤：作者只能查看自己的小说，管理员可以查看所有小说
        boolean isManager = UserRole.MANAGER.equals(loginUser.getRole());
        if (!isManager) {
            // 作者只能查看自己的小说
            queryWrapper.eq(Novel::getAuthorId, loginUser.getId());
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

        log.info("[Author/Manager] 搜索小说：dto={}, 用户角色={}", dto, loginUser.getRole());

        // 创建分页对象
        Page<Novel> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        
        // 权限过滤：作者只能搜索自己的小说，管理员可以搜索所有小说
        boolean isManager = UserRole.MANAGER.equals(loginUser.getRole());
        if (!isManager) {
            // 作者只能搜索自己的小说
            queryWrapper.eq(Novel::getAuthorId, loginUser.getId());
        }

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
        log.info("[Author] 修改小说：ID={}, 名称={}, 作者ID={}", novelDTO.getId(), novelDTO.getName(), loginUser.getId());

        // 权限校验：只有作者可以修改小说
        if (!UserRole.AUTHOR.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

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

    // ==================== Manager - 统计分析 ====================

    @Override
    public Result getNovelCountStatistics(String groupBy) {
        log.info("[Manager] 获取小说数量统计：groupBy={}", groupBy);

        if (groupBy == null || groupBy.isBlank()) {
            return Result.error("groupBy参数不能为空");
        }

        List<LinkedHashMap<String, Object>> dataList;
        
        switch (groupBy.toLowerCase()) {
            case "category":
                dataList = novelMapper.countNovelsByCategory();
                break;
            case "channel":
                dataList = novelMapper.countNovelsByChannel();
                break;
            case "status":
                dataList = novelMapper.countNovelsByStatus();
                break;
            case "hot":
                dataList = novelMapper.countNovelsByHot();
                break;
            default:
                return Result.error("不支持的分组维度：" + groupBy + "，支持：category/channel/status/hot");
        }

        // 转换为VO
        NovelStatisticsVO vo = new NovelStatisticsVO();
        List<NovelStatisticsVO.Item> items = dataList.stream()
                .map(map -> {
                    NovelStatisticsVO.Item item = new NovelStatisticsVO.Item();
                    item.setName((String) map.get("name"));
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("小说数量统计完成：groupBy={}, 结果数={}", groupBy, items.size());
        return Result.success(vo);
    }

    @Override
    public Result getAuthorCountStatistics() {
        log.info("[Manager] 获取作者数量统计（按等级）");

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.countAuthorsByRank();

        // 转换为VO
        AuthorStatisticsVO vo = new AuthorStatisticsVO();
        List<AuthorStatisticsVO.Item> items = dataList.stream()
                .map(map -> {
                    AuthorStatisticsVO.Item item = new AuthorStatisticsVO.Item();
                    Object rank = map.get("rank");
                    item.setRank(rank instanceof Number ? ((Number) rank).intValue() : 0);
                    item.setRankName((String) map.get("rankName"));
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("作者数量统计完成：结果数={}", items.size());
        return Result.success(vo);
    }

    @Override
    public Result getVisitorCountStatistics() {
        log.info("[Manager] 获取用户数量统计（按VIP等级）");

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.countVisitorsByVipLevel();

        // 转换为VO
        VisitorStatisticsVO vo = new VisitorStatisticsVO();
        List<VisitorStatisticsVO.Item> items = dataList.stream()
                .map(map -> {
                    VisitorStatisticsVO.Item item = new VisitorStatisticsVO.Item();
                    Object vipLevel = map.get("vipLevel");
                    item.setVipLevel(vipLevel instanceof Number ? ((Number) vipLevel).intValue() : 0);
                    item.setVipName((String) map.get("vipName"));
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("用户数量统计完成：结果数={}", items.size());
        return Result.success(vo);
    }

    // ==================== Manager - 小说排行榜 ====================

    @Override
    public Result getNovelOngoingRanking(Integer limit) {
        log.info("[Manager] 获取连载榜：limit={}", limit);
        return buildNovelRanking(statisticsMapper.rankNovelsByOngoing(limit), "连载榜");
    }

    // ==================== Manager - 作者排行榜 ====================

    @Override
    public Result getAuthorProductiveRanking(Integer limit) {
        log.info("[Manager] 获取作者高产榜：limit={}", limit);

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.rankAuthorsByProductive(limit);

        AuthorRankingVO vo = new AuthorRankingVO();
        List<AuthorRankingVO.Item> items = new java.util.ArrayList<>();

        int rank = 1;
        for (LinkedHashMap<String, Object> map : dataList) {
            AuthorRankingVO.Item item = new AuthorRankingVO.Item();
            item.setRank(rank++);
            item.setId(getIntValue(map, "id"));
            item.setName((String) map.get("name"));
            item.setAuthorRank(getIntValue(map, "authorRank"));
            item.setRankName((String) map.get("rankName"));
            item.setNovelCount(getIntValue(map, "novelCount"));
            item.setAvatar((String) map.get("avatar"));
            items.add(item);
        }
        vo.setItems(items);

        log.info("作者高产榜获取完成：结果数={}", items.size());
        return Result.success(vo);
    }

    // ==================== Manager - 趋势统计 ====================

    @Override
    public Result getNovelTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("[Manager] 获取小说趋势：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return buildTrendResult(startDate, endDate, type, "小说",
                statisticsMapper::novelTrendByDay,
                statisticsMapper::novelTrendByWeek,
                statisticsMapper::novelTrendByMonth,
                statisticsMapper::novelTrendByYear);
    }

    @Override
    public Result getAuthorTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("[Manager] 获取作者注册趋势：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return buildTrendResult(startDate, endDate, type, "作者注册",
                statisticsMapper::authorTrendByDay,
                statisticsMapper::authorTrendByWeek,
                statisticsMapper::authorTrendByMonth,
                statisticsMapper::authorTrendByYear);
    }

    @Override
    public Result getVisitorTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("[Manager] 获取用户注册趋势：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return buildTrendResult(startDate, endDate, type, "用户注册",
                statisticsMapper::visitorTrendByDay,
                statisticsMapper::visitorTrendByWeek,
                statisticsMapper::visitorTrendByMonth,
                statisticsMapper::visitorTrendByYear);
    }

    /**
     * 构建趋势统计结果
     */
    @FunctionalInterface
    private interface TrendQuery {
        List<LinkedHashMap<String, Object>> query(LocalDate start, LocalDate end);
    }

    private Result buildTrendResult(LocalDate startDate, LocalDate endDate, String type,
                                     String trendName,
                                     TrendQuery dayQuery,
                                     TrendQuery weekQuery,
                                     TrendQuery monthQuery,
                                     TrendQuery yearQuery) {
        // 参数校验
        if (startDate == null || endDate == null) {
            return Result.error("日期参数不能为空");
        }
        if (startDate.isAfter(endDate)) {
            return Result.error("开始日期不能晚于结束日期");
        }

        // 默认按月统计
        if (type == null || type.isBlank()) {
            type = "month";
        }

        List<LinkedHashMap<String, Object>> dataList;
        switch (type.toLowerCase()) {
            case "day":
                dataList = dayQuery.query(startDate, endDate);
                break;
            case "week":
                dataList = weekQuery.query(startDate, endDate);
                break;
            case "month":
                dataList = monthQuery.query(startDate, endDate);
                break;
            case "year":
                dataList = yearQuery.query(startDate, endDate);
                break;
            default:
                return Result.error("不支持的统计粒度：" + type + "，支持：day/week/month/year");
        }

        // 转换为VO
        TrendVO vo = new TrendVO();
        List<TrendVO.Item> items = dataList.stream()
                .map(map -> {
                    TrendVO.Item item = new TrendVO.Item();
                    Object date = map.get("date");
                    item.setDate(date != null ? date.toString() : "");
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("{}趋势统计完成：type={}, 结果数={}", trendName, type, items.size());
        return Result.success(vo);
    }

    /**
     * 构建小说排行榜VO
     */
    private Result buildNovelRanking(List<LinkedHashMap<String, Object>> dataList, String rankingName) {
        NovelRankingVO vo = new NovelRankingVO();
        List<NovelRankingVO.Item> items = new java.util.ArrayList<>();
        
        int rank = 1;
        for (LinkedHashMap<String, Object> map : dataList) {
            NovelRankingVO.Item item = new NovelRankingVO.Item();
            item.setRank(rank++);
            item.setId(getIntValue(map, "id"));
            item.setName((String) map.get("name"));
            item.setAuthorName((String) map.get("authorName"));
            item.setChapterCount(getIntValue(map, "chapterCount"));
            item.setIsFinished(getBoolValue(map, "isFinished"));
            item.setIsHot(getBoolValue(map, "isHot"));
            item.setUrl((String) map.get("url"));
            item.setUpdateTime((String) map.get("updateTime"));
            items.add(item);
        }
        vo.setItems(items);

        log.info("{}获取完成：结果数={}", rankingName, items.size());
        return Result.success(vo);
    }

    private Boolean getBoolValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return false;
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
        if (value instanceof Number num) {
            return num.intValue();
        }
        return null;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}