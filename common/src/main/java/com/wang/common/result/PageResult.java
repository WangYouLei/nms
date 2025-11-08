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
    

    
    // equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PageResult<?> that = (PageResult<?>) o;
        
        if (pageNum != that.pageNum) return false;
        if (pageSize != that.pageSize) return false;
        if (total != that.total) return false;
        if (pages != that.pages) return false;
        return list != null ? list.equals(that.list) : that.list == null;
    }
    
    @Override
    public int hashCode() {
        int result = pageNum;
        result = 31 * result + pageSize;
        result = 31 * result + (int) (total ^ (total >>> 32));
        result = 31 * result + pages;
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }

    
    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> build(int pageNum, int pageSize, long total, List<T> list) {
        int pages = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        return new PageResult<T>(pageNum, pageSize, total, pages, list);
    }
}