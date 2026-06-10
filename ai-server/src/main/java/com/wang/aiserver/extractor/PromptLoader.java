package com.wang.aiserver.extractor;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Prompt 模板加载器
 * 从 classpath 下的 prompts/ 目录加载 .txt 模板文件
 */
@Component
public class PromptLoader {

    private static final Logger log = LoggerFactory.getLogger(PromptLoader.class);

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("PromptLoader 初始化完成");
    }

    /**
     * 加载指定名称的 prompt 模板
     */
    public String load(String name) {
        return cache.computeIfAbsent(name, this::loadFromFile);
    }

    private String loadFromFile(String name) {
        String path = "prompts/" + name + ".txt";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                log.debug("Prompt 模板加载成功: {}", path);
                return content;
            }
        } catch (Exception e) {
            log.warn("Prompt 模板加载失败: {}，使用空模板", path, e);
            return "";
        }
    }

    /**
     * 重新加载所有模板（清除缓存）
     */
    public void reload() {
        cache.clear();
        log.info("Prompt 缓存已清除");
    }
}
