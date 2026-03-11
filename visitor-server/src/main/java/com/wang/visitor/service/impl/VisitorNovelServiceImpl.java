package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.vo.NovelDetailVO;
import com.wang.pojo.vo.NovelListVO;
import com.wang.visitor.mapper.VisitorNovelMapper;
import com.wang.visitor.service.VisitorNovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访客端小说服务实现类
 */
@Slf4j
@Service
public class VisitorNovelServiceImpl implements VisitorNovelService {

    private final VisitorNovelMapper visitorNovelMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存Key前缀
    private static final String CACHE_KEY_NOVEL_DETAIL = "novel:detail:";
    private static final String CACHE_KEY_NOVEL_HOT = "novel:hot:";

    // 缓存过期时间（秒）

    //热门小说信息缓存时间1天
    private static final long NOVEL_HOT_TTL = 86400L;

    // 小说详情缓存30分钟
    private static final long NOVEL_DETAIL_TTL = 1800L;

    // 分页参数限制
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public VisitorNovelServiceImpl(VisitorNovelMapper visitorNovelMapper,
                                    RedisTemplate<String, Object> redisTemplate) {
        this.visitorNovelMapper = visitorNovelMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result getNovelList(Integer pageNum, Integer pageSize, String keyword) {
        log.info("查询小说列表：页码={}, 每页数量={}, 关键词={}", pageNum, pageSize, keyword);

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

        Page<Novel> result = visitorNovelMapper.selectPage(page, queryWrapper);

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

        try{
            Object object = redisTemplate.opsForValue().get(CACHE_KEY_NOVEL_HOT + categoryId + ":" + pageNum + ":" + pageSize);
            if(object != null){
                log.info("从缓存获取热门小说：categoryId={},pageNum={},pageSize={}", categoryId,pageNum,pageSize);
                return Result.success(object);
            }
        }catch (Exception e){
            log.warn("读取Redis缓存失败：{}", e.getMessage());
        }
        log.info("获取缓存失败，开始从数据库中加载,categoryId={},pageNum={},pageSize={}",categoryId,pageNum,pageSize);

        int offset = (pageNum - 1) * pageSize;

        // SQL层面分页查询
        List<Novel> novels = visitorNovelMapper.selectHotNovelsByPage(categoryId, offset, pageSize);

        // 查询总数
        Integer total = visitorNovelMapper.countHotNovels(categoryId);
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
    public Result getNovelDetail(Integer novelId) {
        log.info("获取小说详情：ID={}", novelId);

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
        Map<String, Object> detail = visitorNovelMapper.selectNovelDetail(novelId);
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

    @Override
    public Result getNovelsByCategory(Integer pageNum, Integer pageSize, Integer categoryId) {
        log.info("按分类查询小说：分类ID={}", categoryId);

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
        List<Novel> novels = visitorNovelMapper.selectNovelsByCategoryId(categoryId, offset, pageSize);

        // 查询总数
        Integer total = visitorNovelMapper.countNovelsByCategoryId(categoryId);
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
     * 转换为详情VO（安全类型转换）
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

    /**
     * 安全获取Integer值
     */
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

    /**
     * 安全获取String值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}