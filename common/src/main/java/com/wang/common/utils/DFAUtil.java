package com.wang.common.utils;

import java.util.*;

/**
 * DFA敏感词检测工具类
 * 使用DFA算法实现高效的敏感词检测、过滤功能
 * 如果敏感词库后期非常庞大（十万级以上）或者性能到达瓶颈    可以考虑升级使用AC自动机
 */
public class DFAUtil {

    /**
     * DFA敏感词树根节点
     */
    private Map<Character, Object> dfaTree;

    /**
     * 词的结束标记
     */
    private static final char END_FLAG = '\0';

    /**
     * 默认构造函数
     */
    public DFAUtil() {
        this.dfaTree = new HashMap<>();
    }

    /**
     * 带初始词库的构造函数
     * @param words 敏感词集合
     */
    public DFAUtil(Set<String> words) {
        this.dfaTree = new HashMap<>();
        if (words != null) {
            for (String word : words) {
                addWord(word);
            }
        }
    }

    /**
     * 初始化/重置DFA树
     * @param words 敏感词集合
     */
    public void init(Set<String> words) {
        this.dfaTree = new HashMap<>();
        if (words != null) {
            for (String word : words) {
                addWord(word);
            }
        }
    }

    /**
     * 添加敏感词到DFA树
     * @param word 敏感词
     */
    @SuppressWarnings("unchecked")
    public void addWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        Map<Character, Object> currentNode = dfaTree;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object node = currentNode.get(c);
            if (node == null) {
                Map<Character, Object> newNode = new HashMap<>();
                currentNode.put(c, newNode);
                currentNode = newNode;
            } else {
                currentNode = (Map<Character, Object>) node;
            }
        }
        // 标记词的结束
        currentNode.put(END_FLAG, true);
    }

    /**
     * 批量添加敏感词
     * @param words 敏感词集合
     */
    public void addWords(Set<String> words) {
        if (words != null) {
            for (String word : words) {
                addWord(word);
            }
        }
    }

    /**
     * 从DFA树中移除敏感词（简化实现，仅移除结束标记）
     * @param word 敏感词
     */
    @SuppressWarnings("unchecked")
    public void removeWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        Map<Character, Object> currentNode = dfaTree;
        for (int i = 0; i < word.length() - 1; i++) {
            char c = word.charAt(i);
            Object node = currentNode.get(c);
            if (node == null) {
                return;
            }
            currentNode = (Map<Character, Object>) node;
        }
        // 移除结束标记
        if (currentNode != null) {
            currentNode.remove(END_FLAG);
        }
    }

    /**
     * 检测文本中的敏感词
     * @param content 待检测内容
     * @return 检测到的敏感词集合
     */
    public Set<String> detect(String content) {
        Set<String> sensitiveWords = new HashSet<>();

        if (content == null || content.isEmpty()) {
            return sensitiveWords;
        }

        for (int i = 0; i < content.length(); i++) {
            int matchLength = checkWord(content, i);
            if (matchLength > 0) {
                String word = content.substring(i, i + matchLength);
                sensitiveWords.add(word);
            }
        }

        return sensitiveWords;
    }

    /**
     * 检测文本中是否包含敏感词
     * @param content 待检测内容
     * @return 是否包含敏感词
     */
    public boolean contains(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        for (int i = 0; i < content.length(); i++) {
            int matchLength = checkWord(content, i);
            if (matchLength > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 过滤文本中的敏感词（替换为指定字符）
     * @param content 原始内容
     * @param replacement 替换字符
     * @return 过滤后的内容
     */
    public String filter(String content, char replacement) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        StringBuilder result = new StringBuilder(content);
        Set<String> detectedWords = detect(content);

        for (String word : detectedWords) {
            int index = result.indexOf(word);
            while (index != -1) {
                for (int i = 0; i < word.length(); i++) {
                    result.setCharAt(index + i, replacement);
                }
                index = result.indexOf(word, index + word.length());
            }
        }

        return result.toString();
    }

    /**
     * 过滤文本中的敏感词（默认替换为*）
     * @param content 原始内容
     * @return 过滤后的内容
     */
    public String filter(String content) {
        return filter(content, '*');
    }

    /**
     * 高亮文本中的敏感词
     * @param content 原始内容
     * @param prefix 高亮前缀（如：<span style="color:red">）
     * @param suffix 高亮后缀（如：</span>）
     * @return 高亮后的内容
     */
    public String highlight(String content, String prefix, String suffix) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;
        Set<String> detectedWords = detect(content);

        for (String word : detectedWords) {
            result = result.replace(word, prefix + word + suffix);
        }

        return result;
    }

    /**
     * 获取文本中敏感词的数量
     * @param content 待检测内容
     * @return 敏感词数量
     */
    public int count(String content) {
        return detect(content).size();
    }

    /**
     * 检查从指定位置开始的敏感词
     * @param content 内容
     * @param beginIndex 开始位置
     * @return 匹配的敏感词长度，0表示不匹配
     */
    @SuppressWarnings("unchecked")
    private int checkWord(String content, int beginIndex) {
        Map<Character, Object> currentNode = dfaTree;
        int matchLength = 0;

        for (int i = beginIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            currentNode = (Map<Character, Object>) currentNode.get(c);

            if (currentNode == null) {
                break;
            }

            matchLength++;

            // 检查是否是词的结束
            if (currentNode.containsKey(END_FLAG)) {
                return matchLength;
            }
        }

        return 0;
    }

    /**
     * 获取DFA树（用于调试或扩展）
     * @return DFA树
     */
    public Map<Character, Object> getDfaTree() {
        return dfaTree;
    }

    /**
     * 清空DFA树
     */
    public void clear() {
        this.dfaTree = new HashMap<>();
    }

    /**
     * 获取DFA树中的词数量（估算，遍历所有结束标记）
     * @return 词数量
     */
    public int size() {
        return countWords(dfaTree);
    }

    /**
     * 递归计算词数量
     */
    @SuppressWarnings("unchecked")
    private int countWords(Map<Character, Object> node) {
        int count = 0;
        for (Map.Entry<Character, Object> entry : node.entrySet()) {
            if (entry.getKey().equals(END_FLAG)) {
                count++;
            } else if (entry.getValue() instanceof Map) {
                count += countWords((Map<Character, Object>) entry.getValue());
            }
        }
        return count;
    }
}