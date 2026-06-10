package com.wang.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

/**
 * 作者 ES 文档模型
 */
@Data
@Document(indexName = "author_index")
@Setting(settingPath = "es-settings/author-settings.json")
public class AuthorDocument {

    @Id
    private Long id;

    /** 作者昵称 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /** 作者简介 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String introduction;

    /** 头像 */
    @Field(type = FieldType.Keyword)
    private String avatar;

    /** 等级 */
    @Field(type = FieldType.Integer)
    private Integer rank;

    /** 作品数 */
    @Field(type = FieldType.Integer)
    private Integer novelCount;

    /** 是否删除 */
    @Field(type = FieldType.Boolean)
    private Boolean isDel;
}
