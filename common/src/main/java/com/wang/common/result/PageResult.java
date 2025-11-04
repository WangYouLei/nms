package com.wang.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果工具类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private int pageNum;
    
    /**
     * 每页数量
     */
    private int pageSize;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 总页数
     */
    private int pages;
    
    /**
     * 分页数据列表
     */
    private List<T> list;
    
    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> build(int pageNum, int pageSize, long total, List<T> list) {
        int pages = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        return new PageResult<>(pageNum, pageSize, total, pages, list);
    }
}