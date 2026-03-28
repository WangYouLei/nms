package com.wang.common.config;

import com.wang.common.enums.FileUploadTypeEnum;

import java.util.Map;

public class DefaultUrlConfig {
    // 作者默认头像
    public static final String AUTHOR_AVATAR_URL = "http://127.0.0.1:9000/nms/AUTHOR_AVATAR/5c347d6e-8d91-496f-857d-af9b45cdb480.jpg";

    // 用户默认头像
    public static final String VISITOR_AVATAR_URL = "http://127.0.0.1:9000/nms/VISITOR_AVATAR/aa66044f-0d4d-4868-95f3-a95c1ac13f86.jpg";

    // 管理员默认头像
    public static final String MANAGER_AVATAR_URL = "http://127.0.0.1:9000/nms/MANAGER_AVATAR/5bdfcbb8-9b29-4c2d-873a-6a4539714653.jpg";

    // 小说默认封面
    public static final String NOVEL_COVER_URL = "http://127.0.0.1:9000/nms/NOVEL_COVER/88e08331-0134-43f3-ae66-1f81c4ca191e.jpg";

    //默认地址类型（这样方便以后扩展默认地址时，不用修改common-server模块中的文件删除逻辑）
    public static final Map<Integer, String> NOVEL_TYPE = Map.of(
            FileUploadTypeEnum.AUTHOR_AVATAR.getCode(), AUTHOR_AVATAR_URL,
            FileUploadTypeEnum.VISITOR_AVATAR.getCode(), VISITOR_AVATAR_URL,
            FileUploadTypeEnum.NOVEL_COVER.getCode(), NOVEL_COVER_URL
    );
}
