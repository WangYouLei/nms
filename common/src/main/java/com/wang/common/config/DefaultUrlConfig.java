package com.wang.common.config;

import com.wang.common.enums.FileUploadTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "default")
@Component
@Data
public class DefaultUrlConfig {
    // 作者默认头像
    private String authorAvatarUrl;

    // 用户默认头像
    private String visitorAvatarUrl;

    // 管理员默认头像
    private String managerAvatarUrl;

    // 小说默认封面
    private String novelCoverUrl;

    //默认地址类型（这样方便以后扩展默认地址时，不用修改common-server模块中的文件删除逻辑）
    private Map<Integer,String> novelType;



    //在依赖注入完成后执行初始化方法
    @PostConstruct
    public void init() {
        novelType = Map.of(
                FileUploadTypeEnum.AUTHOR_AVATAR.getCode(), authorAvatarUrl,
                FileUploadTypeEnum.USER_AVATAR.getCode(), visitorAvatarUrl,
                FileUploadTypeEnum.NOVEL_COVER.getCode(), novelCoverUrl
        );
    }



}
