-- =====================================================
-- 小说章节表
-- =====================================================

USE `nms`;

-- 创建章节表
CREATE TABLE IF NOT EXISTS `novel_chapter` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `novel_id` int(11) NOT NULL COMMENT '小说ID',
    `title` varchar(255) NOT NULL COMMENT '章节标题',
    `content_url` varchar(500) NOT NULL COMMENT '章节内容URL（MinIO存储路径）',
    `chapter_order` int(11) NOT NULL DEFAULT 1 COMMENT '章节顺序',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_novel_id` (`novel_id`),
    KEY `idx_chapter_order` (`chapter_order`),
    UNIQUE KEY `uk_novel_title` (`novel_id`, `title`),
    CONSTRAINT `fk_chapter_novel` FOREIGN KEY (`novel_id`) REFERENCES `novel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说章节表';

-- 添加说明
-- 存储路径格式: manager/NOVEL_CHAPTER/{小说ID}/{章节标题}.md
-- 同一小说下章节标题唯一
-- 章节顺序用于排序显示