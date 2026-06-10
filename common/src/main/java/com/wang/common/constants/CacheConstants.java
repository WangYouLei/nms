package com.wang.common.constants;

/**
 * 缓存常量类
 * 统一管理缓存键前缀和过期时间
 */
public final class CacheConstants {

    private CacheConstants() {
        // 工具类，禁止实例化
    }

    // ==================== 缓存键前缀 ====================
    
    /** 用户 Token */
    public static final String TOKEN_PREFIX = "token:";
    
    /** 小说详情 */
    public static final String NOVEL_DETAIL_PREFIX = "novel:detail:";
    
    /** 热门小说 */
    public static final String NOVEL_HOT_PREFIX = "novel:hot:";
    
    /** 小说分类关联 */
    public static final String NOVEL_CATEGORY_PREFIX = "novel:category:";
    
    /** 章节详情 */
    public static final String CHAPTER_DETAIL_PREFIX = "chapter:detail:";
    
    /** 作者信息 */
    public static final String AUTHOR_DETAIL_PREFIX = "author:detail:";
    
    /** 作者名称头像 */
    public static final String AUTHOR_NAME_AVATAR_PREFIX = "author:nameAvatar:";
    
    /** 访客信息 */
    public static final String VISITOR_DETAIL_PREFIX = "visitor:detail:";
    
    /** 访客名称头像 */
    public static final String VISITOR_NAME_AVATAR_PREFIX = "visitor:nameAvatar:";
    
    /** 管理员信息 */
    public static final String MANAGER_DETAIL_PREFIX = "manager:detail:";
    
    /** 管理员名称头像 */
    public static final String MANAGER_NAME_AVATAR_PREFIX = "manager:nameAvatar:";
    
    /** 分类列表 */
    public static final String CATEGORY_ALL = "category:all";
    
    /** 热门分类 */
    public static final String CATEGORY_HOT = "category:hot";
    
    /** 仪表盘概览 */
    public static final String DASHBOARD_OVERVIEW = "dashboard:overview";
    
    /** 统计数据 */
    public static final String DASHBOARD_STATS_PREFIX = "dashboard:stats:";
    
    /** 趋势数据 */
    public static final String TREND_PREFIX = "trend:";
    
    /** 排行榜 */
    public static final String RANKING_PREFIX = "ranking:";

    /** 排行榜 ZSET Key - 小说收藏榜 */
    public static final String RANKING_NOVEL_COLLECT = "ranking:novel:collect";

    /** 排行榜 ZSET Key - 连载榜（仅连载中小说） */
    public static final String RANKING_NOVEL_ONGOING = "ranking:novel:ongoing";

    /** 排行榜 ZSET Key - 最新更新榜 */
    public static final String RANKING_NOVEL_LATEST = "ranking:novel:latest";

    /** 排行榜 ZSET Key - 新书榜 */
    public static final String RANKING_NOVEL_NEW = "ranking:novel:new";

    /** 排行榜 ZSET Key - 作者高产榜 */
    public static final String RANKING_AUTHOR_PRODUCTIVE = "ranking:author:productive";
    
    /** 关注检查 */
    public static final String FOLLOW_CHECK_PREFIX = "follow:check:";
    
    /** 关注数量 */
    public static final String FOLLOW_COUNT_PREFIX = "follow:count:";
    
    /** 粉丝数量 */
    public static final String FANS_COUNT_PREFIX = "follow:fans:";
    
    /** 收藏检查 */
    public static final String COLLECT_CHECK_PREFIX = "collect:check:";
    
    /** 收藏数量 */
    public static final String COLLECT_COUNT_PREFIX = "collect:count:";
    
    /** 小说收藏数量 */
    public static final String NOVEL_COLLECT_COUNT_PREFIX = "novel:collect:count:";

    /** 阅读进度 */
    public static final String READING_PROGRESS_PREFIX = "reading:progress:";

    /** 小说在读人数（原子计数器） */
    public static final String READING_COUNT_PREFIX = "reading:count:";

    // ==================== 缓存过期时间（秒） ====================
    
    /** Token 过期时间：24小时 */
    public static final long TOKEN_TTL = 86400L;
    
    /** 小说详情过期时间：30分钟 */
    public static final long NOVEL_DETAIL_TTL = 1800L;
    
    /** 热门小说过期时间：24小时 */
    public static final long NOVEL_HOT_TTL = 86400L;
    
    /** 章节详情过期时间：1小时 */
    public static final long CHAPTER_DETAIL_TTL = 3600L;
    
    /** 用户信息过期时间：24小时 */
    public static final long USER_DETAIL_TTL = 86400L;
    
    /** 用户名称头像过期时间：12小时 */
    public static final long USER_NAME_AVATAR_TTL = 43200L;
    
    /** 分类数据过期时间：24小时 */
    public static final long CATEGORY_TTL = 86400L;
    
    /** 仪表盘概览过期时间：5分钟 */
    public static final long DASHBOARD_OVERVIEW_TTL = 300L;
    
    /** 统计数据过期时间：15分钟 */
    public static final long STATS_TTL = 900L;
    
    /** 趋势数据过期时间：30分钟 */
    public static final long TREND_TTL = 1800L;
    
    /** 排行榜过期时间：30分钟 */
    public static final long RANKING_TTL = 1800L;
    
    /** 关注/收藏检查过期时间：5分钟 */
    public static final long CHECK_TTL = 300L;
    
    /** 关注/收藏数量过期时间：10分钟 */
    public static final long COUNT_TTL = 600L;
    
    /** 默认过期时间：30分钟 */
    public static final long DEFAULT_TTL = 1800L;

    /** 阅读进度过期时间：10分钟 */
    public static final long READING_PROGRESS_TTL = 600L;

    /** 原子计数器过期时间：24小时 */
    public static final long COUNTER_TTL = 86400L;

    // ==================== 缓存键构建方法 ====================
    
    /**
     * 构建作者详情缓存键
     */
    public static String buildAuthorDetailKey(Long authorId) {
        return AUTHOR_DETAIL_PREFIX + authorId;
    }
    
    /**
     * 构建作者名称头像缓存键
     */
    public static String buildAuthorNameAvatarKey(Long authorId) {
        return AUTHOR_NAME_AVATAR_PREFIX + authorId;
    }
    
    /**
     * 构建访客详情缓存键
     */
    public static String buildVisitorDetailKey(Long visitorId) {
        return VISITOR_DETAIL_PREFIX + visitorId;
    }
    
    /**
     * 构建访客名称头像缓存键
     */
    public static String buildVisitorNameAvatarKey(Long visitorId) {
        return VISITOR_NAME_AVATAR_PREFIX + visitorId;
    }
    
    /**
     * 构建管理员详情缓存键
     */
    public static String buildManagerDetailKey(Long managerId) {
        return MANAGER_DETAIL_PREFIX + managerId;
    }
    
    /**
     * 构建管理员名称头像缓存键
     */
    public static String buildManagerNameAvatarKey(Long managerId) {
        return MANAGER_NAME_AVATAR_PREFIX + managerId;
    }
    
    /**
     * 构建小说分类关联缓存键
     */
    public static String buildNovelCategoryKey(Long novelId) {
        return NOVEL_CATEGORY_PREFIX + novelId;
    }
    
    /**
     * 构建统计数据缓存键
     */
    public static String buildStatsKey(String type) {
        return DASHBOARD_STATS_PREFIX + type;
    }
    
    /**
     * 构建趋势数据缓存键
     */
    public static String buildTrendKey(String type, String startDate, String endDate) {
        return TREND_PREFIX + type + ":" + startDate + ":" + endDate;
    }
    
    /**
     * 构建排行榜缓存键
     */
    public static String buildRankingKey(String type, Integer limit) {
        return RANKING_PREFIX + type + ":" + limit;
    }
    
    /**
     * 构建关注检查缓存键
     */
    public static String buildFollowCheckKey(Long visitorId, Long authorId) {
        return FOLLOW_CHECK_PREFIX + visitorId + ":" + authorId;
    }
    
    /**
     * 构建关注数量缓存键
     */
    public static String buildFollowCountKey(Long visitorId) {
        return FOLLOW_COUNT_PREFIX + visitorId;
    }
    
    /**
     * 构建粉丝数量缓存键
     */
    public static String buildFansCountKey(Long authorId) {
        return FANS_COUNT_PREFIX + authorId;
    }
    
    /**
     * 构建收藏检查缓存键
     */
    public static String buildCollectCheckKey(Long visitorId, Long novelId) {
        return COLLECT_CHECK_PREFIX + visitorId + ":" + novelId;
    }
    
    /**
     * 构建访客收藏数量缓存键
     */
    public static String buildCollectCountKey(Long visitorId) {
        return COLLECT_COUNT_PREFIX + visitorId;
    }
    
    /**
     * 构建小说收藏数量缓存键
     */
    public static String buildNovelCollectCountKey(Long novelId) {
        return NOVEL_COLLECT_COUNT_PREFIX + novelId;
    }

    public static String buildReadingProgressKey(Long visitorId, Long novelId) {
        return READING_PROGRESS_PREFIX + visitorId + ":" + novelId;
    }

    public static String buildReadingCountKey(Long novelId) {
        return READING_COUNT_PREFIX + novelId;
    }
}
