-- =============================================================================
--  青芒视频 · product 库 · 基础数据
--  只放「没有它系统就跑不起来」的数据：分区、默认配置、搜索占位词。
--  测试/演示数据请单独放 seed_demo.sql，不要混进来。
-- =============================================================================

SET NAMES utf8mb4;

-- ---------------------------- 分区 ----------------------------
-- code 为程序内使用的稳定标识，name 为界面展示名。
-- 以后要改展示名只改 name，code 不动，历史数据就不会错位。
INSERT INTO `category` (`code`, `name`, `parent_id`, `sort_order`, `is_enabled`)
VALUES ('anime', '动画', NULL, 10, 1),
       ('bangumi', '番剧', NULL, 20, 1),
       ('guochuang', '国创', NULL, 30, 1),
       ('variety', '综艺', NULL, 40, 1),
       ('film', '电影', NULL, 50, 1),
       ('tv', '电视剧', NULL, 60, 1),
       ('documentary', '纪录片', NULL, 70, 1),
       ('music', '音乐', NULL, 80, 1),
       ('dance', '舞蹈', NULL, 90, 1),
       ('game', '游戏', NULL, 100, 1),
       ('knowledge', '知识', NULL, 110, 1),
       ('life', '生活', NULL, 120, 1),
       ('food', '美食', NULL, 130, 1),
       ('tech', '科技', NULL, 140, 1),
       ('fashion', '时尚', NULL, 150, 1),
       ('sports', '运动', NULL, 160, 1),
       ('car', '汽车', NULL, 170, 1),
       ('entertainment', '娱乐', NULL, 180, 1),
       ('news', '资讯', NULL, 190, 1),
       ('other', '综合', NULL, 999, 1);

-- ---------------------------- 系统配置 ----------------------------
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`)
VALUES ('site.name', '青芒视频', '站点名称'),
       ('site.slogan', '看视频、发弹幕、追创作者 —— 分享每一次观看', '站点标语'),
       ('register.enabled', 'true', '是否开放注册'),
       ('register.reward_coin', '5', '注册赠送硬币'),
       ('login.reward_coin', '1', '每日登录赠送硬币'),
       ('video.max_coin_per_user', '2', '单个视频单个用户最多可投的硬币数'),
       ('video.max_upload_mb', '500', '单视频体积上限(MB)'),
       ('comment.max_length', '1000', '评论最大字数'),
       ('danmaku.max_length', '100', '弹幕最大字数'),
       ('danmaku.max_count_per_video', '5000', '单视频弹幕条数上限'),
       ('search.hot_limit', '10', '搜索热榜返回条数'),
       ('search.history_limit', '12', '搜索历史保留条数'),
       ('watch.later_limit', '1000', '待看清单上限'),
       ('favorite.folder_limit', '20', '收藏夹数量上限'),
       ('sensitive.check_enabled', 'true', '是否启用敏感词校验');

-- ---------------------------- 搜索占位词 ----------------------------
INSERT INTO `search_keyword` (`keyword`, `search_count`, `last_searched_at`)
VALUES ('动画', 320, NOW(3)),
       ('音乐', 286, NOW(3)),
       ('游戏', 254, NOW(3)),
       ('知识', 231, NOW(3)),
       ('纪录片', 198, NOW(3)),
       ('美食', 176, NOW(3)),
       ('科技', 163, NOW(3)),
       ('影视', 152, NOW(3)),
       ('生活', 141, NOW(3)),
       ('舞蹈', 128, NOW(3));