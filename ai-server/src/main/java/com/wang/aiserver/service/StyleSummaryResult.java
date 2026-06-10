package com.wang.aiserver.service;

/**
 * 写作风格总结结果
 */
public class StyleSummaryResult {

    private String styleText;
    private int lastSummarizedChapter;
    private int version;
    private boolean exists;

    public StyleSummaryResult() {
    }

    public StyleSummaryResult(String styleText, int lastSummarizedChapter, int version, boolean exists) {
        this.styleText = styleText;
        this.lastSummarizedChapter = lastSummarizedChapter;
        this.version = version;
        this.exists = exists;
    }

    public String getStyleText() {
        return styleText;
    }

    public void setStyleText(String styleText) {
        this.styleText = styleText;
    }

    public int getLastSummarizedChapter() {
        return lastSummarizedChapter;
    }

    public void setLastSummarizedChapter(int lastSummarizedChapter) {
        this.lastSummarizedChapter = lastSummarizedChapter;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
