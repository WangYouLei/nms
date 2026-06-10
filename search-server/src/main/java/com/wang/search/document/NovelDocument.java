package com.wang.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小说 ES 文档模型
 * 冗余存储搜索所需的关联字段（作者信息、分类信息）
 */
@Data
@Document(indexName = "novel_index")
@Setting(settingPath = "es-settings/novel-settings.json")
public class NovelDocument {

    @Id
    private Long id;

    /** 小说名称 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /** 副名称 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String subName;

    /** 标签 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String tags;

    /** 简介 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String introduction;

    /** 作者名称（冗余） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String authorName;

    /** 作者ID */
    @Field(type = FieldType.Long)
    private Long authorId;

    /** 作者头像（冗余） */
    @Field(type = FieldType.Keyword)
    private String authorAvatar;

    /** 作者等级（冗余） */
    @Field(type = FieldType.Integer)
    private Integer authorRank;

    /** 封面URL */
    @Field(type = FieldType.Keyword)
    private String url;

    /** 章节数 */
    @Field(type = FieldType.Integer)
    private Integer chapterCount;

    /** 总字数 */
    @Field(type = FieldType.Integer)
    private Integer allWordCount;

    /** 收藏数 */
    @Field(type = FieldType.Integer)
    private Integer collectCount;

    /** 是否完结 */
    @Field(type = FieldType.Boolean)
    private Boolean isFinished;

    /** 是否热门 */
    @Field(type = FieldType.Boolean)
    private Boolean isHot;

    /** 是否删除 */
    @Field(type = FieldType.Boolean)
    private Boolean isDel;

    /** 更新时间 */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime updateTime;

    /** 分类ID列表（冗余，来自novel_category_relation） */
    @Field(type = FieldType.Long)
    private List<Long> categoryIds;

    /** 分类名称列表（冗余，来自novel_category.type，IK分词支持按分类名搜索） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private List<String> categoryNames;

    /** 分类名称 keyword 字段（用于聚合统计，不分词） */
    @Field(type = FieldType.Keyword)
    private List<String> categoryNamesKeyword;

    /** 频道类型（1男频/2女频，来自novel_category.category） */
    @Field(type = FieldType.Integer)
    private Integer categoryType;
}
