package com.wang.commonserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.Result;
import com.wang.pojo.dto.SensitiveWordDTO;
import com.wang.pojo.dto.SensitiveWordQueryDTO;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.SensitiveWordVO;

import java.util.Set;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService {

    /**
     * 添加敏感词
     * @param dto 敏感词信息
     * @return 添加结果
     */
    Result addSensitiveWord(SensitiveWordDTO dto);

    /**
     * 批量添加敏感词
     * @param words 敏感词列表
     * @param category 类别
     * @param level 等级
     * @param creatorId 创建人ID
     * @return 添加结果
     */
    Result batchAddSensitiveWord(Set<String> words, Integer category, Integer level, Long creatorId);

    /**
     * 更新敏感词
     * @param dto 敏感词信息
     * @return 更新结果
     */
    Result updateSensitiveWord(SensitiveWordDTO dto);

    /**
     * 删除敏感词
     * @param id 敏感词ID
     * @return 删除结果
     */
    Result deleteSensitiveWord(Long id);

    /**
     * 批量删除敏感词
     * @param ids 敏感词ID列表
     * @return 删除结果
     */
    Result batchDeleteSensitiveWord(Set<Long> ids);

    /**
     * 获取敏感词详情
     * @param id 敏感词ID
     * @return 敏感词详情
     */
    Result getSensitiveWordById(Long id);

    /**
     * 分页查询敏感词列表
     * @param queryDTO 查询条件
     * @return 敏感词列表
     */
    Result getSensitiveWordList(SensitiveWordQueryDTO queryDTO);

    /**
     * 启用/禁用敏感词
     * @param id 敏感词ID
     * @param status 状态：0-禁用，1-启用
     * @return 操作结果
     */
    Result updateStatus(Long id, Integer status);


    /**
     * 审核文本内容并返回敏感词详情
     * @param content 待审核内容
     * @param includeLevel 是否按等级处理
     * @return 审核结果
     */
    AuditResultVO auditText(String content, boolean includeLevel);

    /**
     * 检测文本中的敏感词
     * @param content 待检测内容
     * @return 敏感词集合
     */
    Set<String> detectSensitiveWords(String content);

    /**
     * 过滤文本中的敏感词（替换为*）
     * @param content 原始内容
     * @param replacement 替换字符
     * @return 过滤后的内容
     */
    String filterText(String content, char replacement);

    /**
     * 刷新敏感词缓存
     */
    void refreshCache();

    /**
     * 初始化敏感词DFA树
     */
    void initDFA();
}