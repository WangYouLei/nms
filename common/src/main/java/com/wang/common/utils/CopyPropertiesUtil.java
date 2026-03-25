package com.wang.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * 参数拷贝工具类
 * 用于将源对象的非空属性复制到目标对象，支持忽略指定字段
 *
 * @author wang
 */
public class CopyPropertiesUtil {

    /**
     * 工具类禁止实例化
     */
    private CopyPropertiesUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 将 source 中非 null 的属性复制到 target，忽略指定字段
     *
     * @param source           源对象（通常是 DTO）
     * @param target           目标对象（通常是 Entity）
     * @param ignoreProperties 需要忽略的属性名（如 password、createTime、id 等）
     */
    public static void copyNonNullProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }

        // 获取所有 null 值的属性名
        String[] nullPropertyNames = getNullPropertyNames(source);

        // 合并 null 属性名和用户指定的忽略属性名
        Set<String> ignoreSet = new HashSet<>();
        for (String name : nullPropertyNames) {
            ignoreSet.add(name);
        }
        if (ignoreProperties != null) {
            for (String name : ignoreProperties) {
                ignoreSet.add(name);
            }
        }

        // 转为数组
        String[] allIgnoreProperties = ignoreSet.toArray(new String[0]);

        // 使用 Spring BeanUtils 进行复制
        BeanUtils.copyProperties(source, target, allIgnoreProperties);
    }

    /**
     * 获取对象中所有值为 null 的属性名数组
     *
     * @param source 源对象
     * @return 值为 null 的属性名数组
     */
    public static String[] getNullPropertyNames(Object source) {
        if (source == null) {
            return new String[0];
        }

        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        Set<String> nullProperties = new HashSet<>();

        for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
            String propertyName = pd.getName();
            if ("class".equals(propertyName)) {
                continue;
            }
            Object value = beanWrapper.getPropertyValue(propertyName);
            if (value == null) {
                nullProperties.add(propertyName);
            }
        }

        return nullProperties.toArray(new String[0]);
    }
}