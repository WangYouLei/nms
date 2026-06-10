package com.wang.aiserver.service;

import java.util.List;

/**
 * 知识提取结果
 */
public class KnowledgeExtractResult {

    private int extractedCount;
    private List<Object> newItems;
    private List<Object> updatedItems;

    public KnowledgeExtractResult() {
    }

    public KnowledgeExtractResult(int extractedCount, List<Object> newItems, List<Object> updatedItems) {
        this.extractedCount = extractedCount;
        this.newItems = newItems;
        this.updatedItems = updatedItems;
    }

    public int getExtractedCount() {
        return extractedCount;
    }

    public void setExtractedCount(int extractedCount) {
        this.extractedCount = extractedCount;
    }

    public List<Object> getNewItems() {
        return newItems;
    }

    public void setNewItems(List<Object> newItems) {
        this.newItems = newItems;
    }

    public List<Object> getUpdatedItems() {
        return updatedItems;
    }

    public void setUpdatedItems(List<Object> updatedItems) {
        this.updatedItems = updatedItems;
    }
}
