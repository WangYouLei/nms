-- =====================================================
-- NovelManagementSystem 测试数据
-- 生成时间: 2026-03-06
-- 密码: 123456 (Argon2id加密)
-- 注意: manager 表已有数据，无需生成
-- =====================================================

USE `nms`;

-- =====================================================
-- 1. novel_category 小说分类测试数据
-- =====================================================
INSERT INTO `novel_category` (`type`, `category`, `is_hot`, `create_time`, `update_time`) VALUES
-- 男频分类
('玄幻', 1, 1, NOW(), NOW()),
('奇幻', 1, 1, NOW(), NOW()),
('武侠', 1, 0, NOW(), NOW()),
('仙侠', 1, 1, NOW(), NOW()),
('都市', 1, 1, NOW(), NOW()),
('现实', 1, 0, NOW(), NOW()),
('军事', 1, 0, NOW(), NOW()),
('历史', 1, 0, NOW(), NOW()),
('游戏', 1, 1, NOW(), NOW()),
('科幻', 1, 0, NOW(), NOW()),
-- 女频分类
('古代言情', 2, 1, NOW(), NOW()),
('现代言情', 2, 1, NOW(), NOW()),
('幻想言情', 2, 1, NOW(), NOW()),
('浪漫青春', 2, 0, NOW(), NOW()),
('悬疑推理', 2, 0, NOW(), NOW());

-- =====================================================
-- 2. visitor 访问者测试数据
-- 密码均为: 123456 (Argon2id加密)
-- =====================================================
INSERT INTO `visitor` (`name`, `avatar`, `account`, `password`, `vip_level`, `create_time`, `update_time`) VALUES
-- 普通用户 (vip_level=0)
('张三', 'https://avatar.example.com/zhangsan.jpg', '13800138001', '$argon2id$v=19$m=65536,t=2,p=1$wxsxMjHA9lqVV7SW1t8vMg$LxqPbHB80R/mtIIPIO8Ncy+khPGrRbLwq+DklhUwRlI', 0, NOW(), NOW()),
('李四', 'https://avatar.example.com/lisi.jpg', '13800138002', '$argon2id$v=19$m=65536,t=2,p=1$dGO5FaiLF4woSzBxqnriRQ$yVwAn2Ned/eTkek1IecTPM9Gvr37OQYyE3JERfTDABw', 0, NOW(), NOW()),
('王五', 'https://avatar.example.com/wangwu.jpg', '13800138003', '$argon2id$v=19$m=65536,t=2,p=1$ccah6h2lA2wcVUvVRSs8eg$L5BxUvi809iN4cQd44IxegX2ICFbhWP0HHdUuzfru10', 0, NOW(), NOW()),
-- VIP用户
('赵六', 'https://avatar.example.com/zhaoliu.jpg', '13800138004', '$argon2id$v=19$m=65536,t=2,p=1$WtW5aqgAr1GQ9YgbgbQDhg$HARSl1hCr/MTMsgMfKaWVbPw8qzBc8QHLPv7EMNPQWM', 1, NOW(), NOW()),
('钱七', 'https://avatar.example.com/qianqi.jpg', '13800138005', '$argon2id$v=19$m=65536,t=2,p=1$2/0vR5kfR0kKq68tvWFnzA$vbwQNqHFD2q9hNcGkIc03IWutdJ4fnmsjB987IY/rS0', 2, NOW(), NOW()),
('孙八', 'https://avatar.example.com/sunba.jpg', '13800138006', '$argon2id$v=19$m=65536,t=2,p=1$TjF8ENCePiqj/lfprdz6rA$wQ5aXRwB2a31tCB1VuYJzCz7n7SUp6IaYZceL6SWTt8', 3, NOW(), NOW()),
-- 金主用户 (vip_level=4)
('周九', 'https://avatar.example.com/zhoujiu.jpg', '13800138007', '$argon2id$v=19$m=65536,t=2,p=1$sNdo7GJEbKlsxNYNFX4dJQ$bW+FtABvcptRb6vGRX1Okw+GRCep7GkFc/xW/PT9Y14', 4, NOW(), NOW()),
('吴十', 'https://avatar.example.com/wushi.jpg', '13800138008', '$argon2id$v=19$m=65536,t=2,p=1$l4lJJZglsVcBwNiE0rv3Hw$QiqpUZ7TMmOyPxD4wfZiKrWhCJy3fwUbpRE0azL4h/c', 4, NOW(), NOW()),
-- 更多普通用户
('林小明', 'https://avatar.example.com/xiaoming.jpg', '13800138009', '$argon2id$v=19$m=65536,t=2,p=1$aKx6tfpp/Cf3WO0mK2MoFA$Wp3hw9Jgx+ltAmk+B38W61CdZ4B9soBUArYhZAFhOrQ', 0, NOW(), NOW()),
('陈小红', 'https://avatar.example.com/xiaohong.jpg', '13800138010', '$argon2id$v=19$m=65536,t=2,p=1$E7zaZDEI/gXF9B0j12Y2kw$A+QyX7ZS0oBww0KIsBcbRyM3mFzqzYfcuj+lOg3K', 1, NOW(), NOW());

-- =====================================================
-- 3. novel 小说测试数据
-- author_id = 1 (假设 manager 表中已有 ID=1 的记录)
-- =====================================================
INSERT INTO `novel` (`name`, `sub_name`, `tags`, `introduction`, `author_id`, `url`, `create_time`, `update_time`) VALUES
-- 男频小说
('斗破苍穹', '斗气大陆风云录', '玄幻,热血,升级', '三十年河东，三十年河西，莫欺少年穷！这是一个属于斗气的世界，没有花俏艳丽的魔法，有的，仅仅是繁衍到巅峰的斗气！', 1, 'https://cover.example.com/doupo.jpg', NOW(), NOW()),
('完美世界', '荒天帝传说', '玄幻,热血,无敌', '一粒尘可填海，一根草斩日月星辰，弹指间诸天星辰寂灭。一个少年从大荒中走出，一切从这里开始……', 1, 'https://cover.example.com/wanmei.jpg', NOW(), NOW()),
('遮天', '叶天帝传奇', '仙侠,热血,群像', '冰冷与黑暗并存的宇宙深处，九具庞大的龙尸拉着一口古老的青铜棺，在茫茫天宇中无声滑行……', 1, 'https://cover.example.com/zhetian.jpg', NOW(), NOW()),
('凡人修仙传', '韩立修仙记', '仙侠,修炼,凡人流', '一个普通的山村穷小子，偶然之下跨入江湖小门派，虽然资质平庸，但依靠自身努力和算计修炼成仙。', 1, 'https://cover.example.com/fanren.jpg', NOW(), NOW()),
('全职高手', '荣耀巅峰', '游戏,电竞,热血', '网游荣耀中被誉为教科书级别的顶尖高手叶修，因为种种原因遭到俱乐部的驱逐……', 1, 'https://cover.example.com/quanzhi.jpg', NOW(), NOW()),
('诡秘之主', '蒸汽朋克克苏鲁', '奇幻,克苏鲁,蒸汽朋克', '蒸汽与机械的浪潮中，谁能触及非凡？历史和黑暗的迷雾里，又是谁在低语？', 1, 'https://cover.example.com/guimi.jpg', NOW(), NOW()),
('庆余年', '范闲传奇', '历史,权谋,武侠', '积善之家，必有余庆，留余庆，留余庆，忽遇恩人；幸娘亲，幸娘亲，积得阴功。', 1, 'https://cover.example.com/qingyu.jpg', NOW(), NOW()),
('斗罗大陆', '唐三的冒险', '玄幻,武魂,热血', '唐门外门弟子唐三，因偷学内门绝学为唐门所不容，跳崖明志时却发现没有死……', 1, 'https://cover.example.com/douluo.jpg', NOW(), NOW()),
-- 女频小说
('步步惊心', '穿越大清', '古代言情,穿越,清宫', '一脚踏空的女子穿越时空，来到大清康熙年间，卷入九子夺嫡的纷争……', 1, 'https://cover.example.com/bubu.jpg', NOW(), NOW()),
('知否知否应是绿肥红瘦', '盛明兰传', '古代言情,宅斗,励志', '一个穿越成古代庶女的女孩，在大家族的复杂关系中谨慎生存的故事。', 1, 'https://cover.example.com/zhifou.jpg', NOW(), NOW()),
('何以笙箫默', '何以琛默笙', '现代言情,都市,重逢', '一段年少时的爱恋，牵出一生的纠缠。大学时代的何以琛和赵默笙……', 1, 'https://cover.example.com/heis.jpg', NOW(), NOW()),
('三生三世十里桃花', '白浅夜华', '幻想言情,仙侠,虐恋', '三生三世，十里桃花，她是他三生三世的劫，他是她三生三世的缘。', 1, 'https://cover.example.com/sansheng.jpg', NOW(), NOW());

-- =====================================================
-- 4. novel_category_relation 小说分类关联测试数据
-- 小说ID按照插入顺序: 1-12
-- 分类ID按照插入顺序: 1-15
-- =====================================================
INSERT INTO `novel_category_relation` (`novel_id`, `category_id`) VALUES
-- 斗破苍穹(1): 玄幻(1), 仙侠(4)
(1, 1), (1, 4),
-- 完美世界(2): 玄幻(1), 奇幻(2)
(2, 1), (2, 2),
-- 遮天(3): 仙侠(4), 玄幻(1)
(3, 4), (3, 1),
-- 凡人修仙传(4): 仙侠(4), 都市(5)
(4, 4), (4, 5),
-- 全职高手(5): 游戏(9), 都市(5)
(5, 9), (5, 5),
-- 诡秘之主(6): 奇幻(2), 科幻(10)
(6, 2), (6, 10),
-- 庆余年(7): 历史(8), 武侠(3)
(7, 8), (7, 3),
-- 斗罗大陆(8): 玄幻(1), 科幻(10)
(8, 1), (8, 10),
-- 步步惊心(9): 古代言情(11), 历史(8)
(9, 11), (9, 8),
-- 知否(10): 古代言情(11), 悬疑推理(15)
(10, 11), (10, 15),
-- 何以笙箫默(11): 现代言情(12), 都市(5)
(11, 12), (11, 5),
-- 三生三世(12): 幻想言情(13), 仙侠(4)
(12, 13), (12, 4);

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '测试数据插入完成！' AS message;
SELECT 
    (SELECT COUNT(*) FROM novel_category) AS category_count,
    (SELECT COUNT(*) FROM visitor) AS visitor_count,
    (SELECT COUNT(*) FROM novel) AS novel_count,
    (SELECT COUNT(*) FROM novel_category_relation) AS relation_count;