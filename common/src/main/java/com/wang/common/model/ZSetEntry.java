package com.wang.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis Sorted Set 条目
 * 用于排行榜查询时返回成员和分数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZSetEntry {

    /** 成员（小说ID或作者ID的字符串形式） */
    private String member;

    /** 分数 */
    private Double score;
}
