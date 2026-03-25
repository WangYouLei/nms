package com.wang.commonserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.DataSourceEnum;
import com.wang.common.enums.EnableStatusEnum;
import com.wang.common.enums.SensitiveCategoryEnum;
import com.wang.common.enums.SensitiveLevelEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.common.utils.CopyPropertiesUtil;
import com.wang.common.utils.DFAUtil;
import com.wang.commonserver.mapper.SensitiveWordMapper;
import com.wang.commonserver.service.SensitiveWordService;
import com.wang.pojo.dto.SensitiveWordDTO;
import com.wang.pojo.dto.SensitiveWordQueryDTO;
import com.wang.pojo.entity.SensitiveWord;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.SensitiveWordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 敏感词服务实现类
 * 使用DFA算法实现敏感词检测
 */
@Slf4j
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    /**
     * DFA敏感词检测工具类
     */
    private final DFAUtil dfaUtil = new DFAUtil();

    /**
     * 敏感词与等级的映射
     */
    private Map<String, Integer> wordLevelMap = new HashMap<>();

    public SensitiveWordServiceImpl(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    /**
     * 应用启动时初始化DFA树
     */
    @PostConstruct
    public void init() {
        initDFA();
    }

    @Override
    public void initDFA() {
        log.info("开始初始化敏感词DFA树...");
        long startTime = System.currentTimeMillis();

        // 查询所有启用的敏感词
        List<SensitiveWord> words = sensitiveWordMapper.selectAllEnabled();

        // 重建DFA树
        Set<String> wordSet = new HashSet<>();
        Map<String, Integer> newLevelMap = new HashMap<>();

        for (SensitiveWord sw : words) {
            wordSet.add(sw.getWord());
            newLevelMap.put(sw.getWord(), sw.getLevel());
        }

        dfaUtil.init(wordSet);
        this.wordLevelMap = newLevelMap;

        long endTime = System.currentTimeMillis();
        log.info("敏感词DFA树初始化完成，共加载{}个敏感词，耗时{}ms", words.size(), endTime - startTime);
    }

    @Override
    @Transactional
    public Result addSensitiveWord(SensitiveWordDTO dto) {
        log.info("添加敏感词：word={}", dto.getWord());

        // 检查是否已存在
        SensitiveWord existing = sensitiveWordMapper.selectByWord(dto.getWord());
        if (existing != null) {
            log.warn("敏感词已存在：word={}", dto.getWord());
            return Result.buildResult(BizCodeEnum.DATA_DUPLICATE);
        }

        SensitiveWord entity = new SensitiveWord();
        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory());
        entity.setLevel(dto.getLevel());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : EnableStatusEnum.ENABLED.getValue());
        entity.setSource(dto.getSource() != null ? dto.getSource() : DataSourceEnum.MANAGER.getValue());
        entity.setCreatorId(dto.getCreatorId());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        int result = sensitiveWordMapper.insert(entity);
        if (result == 1) {
            // 更新DFA树
            dfaUtil.addWord(entity.getWord());
            wordLevelMap.put(entity.getWord(), entity.getLevel());

            log.info("敏感词添加成功：id={}", entity.getId());
            return Result.success(convertToVO(entity));
        } else {
            log.error("敏感词添加失败：word={}", dto.getWord());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    @Transactional
    public Result batchAddSensitiveWord(Set<String> words, Integer category, Integer level, Long creatorId) {
        log.info("批量添加敏感词：count={}", words.size());

        int successCount = 0;
        int duplicateCount = 0;
        List<SensitiveWordVO> addedWords = new ArrayList<>();

        for (String word : words) {
            // 检查是否已存在
            SensitiveWord existing = sensitiveWordMapper.selectByWord(word);
            if (existing != null) {
                duplicateCount++;
                continue;
            }

            SensitiveWord entity = new SensitiveWord();
            entity.setWord(word);
            entity.setCategory(category != null ? category : SensitiveCategoryEnum.OTHER.getValue());
            entity.setLevel(level != null ? level : SensitiveLevelEnum.LOW.getValue());
            entity.setStatus(EnableStatusEnum.ENABLED.getValue());
            entity.setSource(DataSourceEnum.MANAGER.getValue());
            entity.setCreatorId(creatorId);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            int result = sensitiveWordMapper.insert(entity);
            if (result == 1) {
                successCount++;
                dfaUtil.addWord(entity.getWord());
                wordLevelMap.put(entity.getWord(), entity.getLevel());
                addedWords.add(convertToVO(entity));
            }
        }

        log.info("批量添加敏感词完成：成功{}个，重复{}个", successCount, duplicateCount);
        return Result.success(Map.of(
                "successCount", successCount,
                "duplicateCount", duplicateCount,
                "addedWords", addedWords
        ));
    }

    @Override
    @Transactional
    public Result updateSensitiveWord(SensitiveWordDTO dto) {
        log.info("更新敏感词：id={}", dto.getId());

        SensitiveWord existing = sensitiveWordMapper.selectById(dto.getId());
        if (existing == null) {
            log.warn("敏感词不存在：id={}", dto.getId());
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        // 如果修改了敏感词，检查新词是否已存在
        if (!existing.getWord().equals(dto.getWord())) {
            SensitiveWord duplicate = sensitiveWordMapper.selectByWord(dto.getWord());
            if (duplicate != null) {
                log.warn("敏感词已存在：word={}", dto.getWord());
                return Result.buildResult(BizCodeEnum.DATA_DUPLICATE);
            }
            // 从DFA树中移除旧词
            dfaUtil.removeWord(existing.getWord());
            wordLevelMap.remove(existing.getWord());
        }

        CopyPropertiesUtil.copyNonNullProperties(dto, existing, "id", "createTime", "source", "creatorId");
        existing.setUpdateTime(LocalDateTime.now());

        int result = sensitiveWordMapper.updateById(existing);
        if (result == 1) {
            // 更新DFA树
            dfaUtil.addWord(existing.getWord());
            wordLevelMap.put(existing.getWord(), existing.getLevel());

            log.info("敏感词更新成功：id={}", dto.getId());
            return Result.success(convertToVO(existing));
        } else {
            log.error("敏感词更新失败：id={}", dto.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    @Transactional
    public Result deleteSensitiveWord(Long id) {
        log.info("删除敏感词：id={}", id);

        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            log.warn("敏感词不存在：id={}", id);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        int result = sensitiveWordMapper.deleteById(id);
        if (result == 1) {
            // 刷新DFA树
            initDFA();

            log.info("敏感词删除成功：id={}", id);
            return Result.success("删除成功");
        } else {
            log.error("敏感词删除失败：id={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    @Transactional
    public Result batchDeleteSensitiveWord(Set<Long> ids) {
        log.info("批量删除敏感词：count={}", ids.size());

        int successCount = 0;
        for (Long id : ids) {
            int result = sensitiveWordMapper.deleteById(id);
            if (result == 1) {
                successCount++;
            }
        }

        // 刷新DFA树
        initDFA();

        log.info("批量删除敏感词完成：成功{}个", successCount);
        return Result.success(Map.of("deletedCount", successCount));
    }

    @Override
    public Result getSensitiveWordById(Long id) {
        log.info("获取敏感词详情：id={}", id);

        SensitiveWord entity = sensitiveWordMapper.selectById(id);
        if (entity == null) {
            log.warn("敏感词不存在：id={}", id);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        return Result.success(convertToVO(entity));
    }

    @Override
    public Result getSensitiveWordList(SensitiveWordQueryDTO queryDTO) {
        log.info("分页查询敏感词列表：queryDTO={}", queryDTO);

        LambdaQueryWrapper<SensitiveWord> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getWord() != null && !queryDTO.getWord().isEmpty()) {
            queryWrapper.like(SensitiveWord::getWord, queryDTO.getWord());
        }
        if (queryDTO.getCategory() != null) {
            queryWrapper.eq(SensitiveWord::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getLevel() != null) {
            queryWrapper.eq(SensitiveWord::getLevel, queryDTO.getLevel());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SensitiveWord::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getSource() != null) {
            queryWrapper.eq(SensitiveWord::getSource, queryDTO.getSource());
        }

        queryWrapper.orderByDesc(SensitiveWord::getCreateTime);

        Page<SensitiveWord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<SensitiveWord> resultPage = sensitiveWordMapper.selectPage(page, queryWrapper);

        List<SensitiveWordVO> voList = new ArrayList<>();
        for (SensitiveWord entity : resultPage.getRecords()) {
            voList.add(convertToVO(entity));
        }

        PageResult<SensitiveWordVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result updateStatus(Long id, Integer status) {
        log.info("更新敏感词状态：id={}, status={}", id, status);

        SensitiveWord entity = sensitiveWordMapper.selectById(id);
        if (entity == null) {
            log.warn("敏感词不存在：id={}", id);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        entity.setStatus(status);
        entity.setUpdateTime(LocalDateTime.now());

        int result = sensitiveWordMapper.updateById(entity);
        if (result == 1) {
            // 刷新DFA树
            initDFA();

            log.info("敏感词状态更新成功：id={}", id);
            return Result.success("状态更新成功");
        } else {
            log.error("敏感词状态更新失败：id={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    @Transactional
    public AuditResultVO auditText(String content, boolean includeLevel) {
        log.info("审核文本内容：length={}", content != null ? content.length() : 0);

        if (content == null || content.isEmpty()) {
            return AuditResultVO.passed();
        }

        Set<String> detectedWords = dfaUtil.detect(content);
        if (detectedWords.isEmpty()) {
            return AuditResultVO.passed();
        }

        if (includeLevel) {
            // 检查最高敏感等级
            int maxLevel = SensitiveLevelEnum.LOW.getValue();
            for (String word : detectedWords) {
                Integer level = wordLevelMap.get(word);
                if (level != null && level > maxLevel) {
                    maxLevel = level;
                }
            }

            // 高级别敏感词直接拒绝
            if (SensitiveLevelEnum.HIGH.getValue().equals(maxLevel)) {
                return AuditResultVO.rejected(detectedWords);
            }

            // 低级别敏感词需要人工审核
            return AuditResultVO.needReview(detectedWords, maxLevel);
        }

        // 不区分等级，直接返回检测结果
        return AuditResultVO.needReview(detectedWords, SensitiveLevelEnum.LOW.getValue());
    }

    @Override
    public Set<String> detectSensitiveWords(String content) {
        return dfaUtil.detect(content);
    }

    @Override
    public String filterText(String content, char replacement) {
        return dfaUtil.filter(content, replacement);
    }

    @Override
    public void refreshCache() {
        initDFA();
    }

    /**
     * 转换为VO
     */
    private SensitiveWordVO convertToVO(SensitiveWord entity) {
        SensitiveWordVO vo = new SensitiveWordVO();
        CopyPropertiesUtil.copyNonNullProperties(entity, vo);
        vo.setCategoryName(SensitiveCategoryEnum.getDescription(entity.getCategory()));
        vo.setLevelName(getLevelName(entity.getLevel()));
        vo.setStatusName(EnableStatusEnum.getDescription(entity.getStatus()));
        vo.setSourceName(DataSourceEnum.getDescription(entity.getSource()));
        return vo;
    }

    private String getLevelName(Integer level) {
        if (level == null) return "未知";
        String desc = SensitiveLevelEnum.getDescription(level);
        if (SensitiveLevelEnum.LOW.getValue().equals(level)) {
            return desc + "（需人工审核）";
        } else if (SensitiveLevelEnum.HIGH.getValue().equals(level)) {
            return desc + "（直接拒绝）";
        }
        return desc;
    }
}