-- =============================================================================
-- 青芒视频 · product 库 · 演示数据
-- 前置条件：先执行 table.sql。
-- 本脚本仅供本地开发/演示环境使用，不要在生产库执行。
-- 示例账号：demo_creator、demo_viewer、demo_friend
-- 示例密码：password（仅用于演示）
-- =============================================================================

SET NAMES utf8mb4;

START TRANSACTION;

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
       ('other', '综合', NULL, 999, 1)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `parent_id` = VALUES(`parent_id`),
    `sort_order` = VALUES(`sort_order`),
    `is_enabled` = VALUES(`is_enabled`);

-- ---------------------------- 系统配置 ----------------------------
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`)
VALUES ('site.name', '青芒视频', '站点名称'),
       ('site.slogan', '看视频、发弹幕、追创作者 —— 分享每一次观看', '站点标语'),
       ('register.enabled', 'true', '是否开放注册'),
       ('video.max_upload_mb', '500', '单视频体积上限(MB)'),
       ('comment.max_length', '1000', '评论最大字数'),
       ('danmaku.max_length', '100', '弹幕最大字数'),
       ('danmaku.max_count_per_video', '5000', '单视频弹幕条数上限'),
       ('search.hot_limit', '10', '搜索热榜返回条数'),
       ('search.history_limit', '12', '搜索历史保留条数'),
       ('watch.later_limit', '1000', '待看清单上限'),
       ('favorite.folder_limit', '20', '收藏夹数量上限'),
       ('sensitive.check_enabled', 'true', '是否启用敏感词校验')
ON DUPLICATE KEY UPDATE
    `config_value` = VALUES(`config_value`),
    `description` = VALUES(`description`);

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
       ('舞蹈', 128, NOW(3))
ON DUPLICATE KEY UPDATE
    `search_count` = VALUES(`search_count`),
    `last_searched_at` = VALUES(`last_searched_at`);

-- 演示账号使用应用兼容的 BCrypt 哈希；账号均为普通用户。
INSERT IGNORE INTO `user`
    (`username`, `password_hash`, `nickname`, `avatar_url`, `signature`, `gender`, `role`, `status`, `level`, `exp`)
VALUES
    ('demo_creator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '青芒体验官', '/img/avatar-default.png', '记录生活里的小灵感。', 1, 0, 1, 3, 280),
    ('demo_viewer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '青柠汽水', '/img/woman2.png', '喜欢旅行、音乐和好看的视频。', 2, 0, 1, 2, 120),
    ('demo_friend', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '山间来信', '/img/man2.png', '把沿途风景分享给你。', 1, 0, 1, 1, 60);

SET @creator_id = (SELECT `id` FROM `user` WHERE `username` = 'demo_creator' LIMIT 1);
SET @viewer_id = (SELECT `id` FROM `user` WHERE `username` = 'demo_viewer' LIMIT 1);
SET @friend_id = (SELECT `id` FROM `user` WHERE `username` = 'demo_friend' LIMIT 1);
SET @anime_category_id = (SELECT `id` FROM `category` WHERE `code` = 'anime' LIMIT 1);
SET @music_category_id = (SELECT `id` FROM `category` WHERE `code` = 'music' LIMIT 1);
SET @life_category_id = (SELECT `id` FROM `category` WHERE `code` = 'life' LIMIT 1);

INSERT IGNORE INTO `user_privacy` (`user_id`) VALUES
    (@creator_id), (@viewer_id), (@friend_id);

INSERT IGNORE INTO `user_stats` (`user_id`) VALUES
    (@creator_id), (@viewer_id), (@friend_id);

INSERT INTO `favorite_folder`
    (`owner_id`, `name`, `description`, `visibility`, `is_default`, `sort_order`)
SELECT u.`id`, '默认收藏夹', '系统默认收藏夹', 1, 1, 0
FROM `user` u
WHERE u.`username` IN ('demo_creator', 'demo_viewer', 'demo_friend')
  AND NOT EXISTS (
      SELECT 1 FROM `favorite_folder` f
      WHERE f.`owner_id` = u.`id` AND f.`is_default` = 1 AND f.`deleted_at` IS NULL
  );

INSERT INTO `favorite_folder`
    (`owner_id`, `name`, `description`, `visibility`, `is_default`, `sort_order`)
SELECT @viewer_id, '旅行收藏', '想去的地方和沿途风景', 1, 0, 1
WHERE NOT EXISTS (
    SELECT 1 FROM `favorite_folder`
    WHERE `owner_id` = @viewer_id AND `name` = '旅行收藏' AND `deleted_at` IS NULL
);

SET @creator_folder_id = (
    SELECT `id` FROM `favorite_folder` WHERE `owner_id` = @creator_id AND `is_default` = 1
    ORDER BY `id` LIMIT 1
);
SET @viewer_folder_id = (
    SELECT `id` FROM `favorite_folder` WHERE `owner_id` = @viewer_id AND `is_default` = 1
    ORDER BY `id` LIMIT 1
);
SET @friend_folder_id = (
    SELECT `id` FROM `favorite_folder` WHERE `owner_id` = @friend_id AND `is_default` = 1
    ORDER BY `id` LIMIT 1
);
SET @viewer_travel_folder_id = (
    SELECT `id` FROM `favorite_folder`
    WHERE `owner_id` = @viewer_id AND `name` = '旅行收藏' AND `deleted_at` IS NULL
    ORDER BY `id` LIMIT 1
);

-- 使用可公开访问的 CC0 示例视频；封面使用项目内置占位图。
INSERT INTO `video`
    (`owner_id`, `category_id`, `title`, `description`, `tags`, `original_type`, `cover_url`,
     `play_url`, `source`, `remote_url`, `duration_seconds`, `status`, `published_at`)
SELECT @creator_id, @life_category_id, '演示视频：窗边的春日花朵',
       '用于本地演示的视频条目，播放源为公开的 CC0 示例素材。', '生活,花朵,演示',
       0, '/img/cover-fallback.png',
       'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
       1, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
       30, 1, NOW(3) - INTERVAL 3 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM `video` WHERE `owner_id` = @creator_id AND `title` = '演示视频：窗边的春日花朵'
);

INSERT INTO `video`
    (`owner_id`, `category_id`, `title`, `description`, `tags`, `original_type`, `cover_url`,
     `play_url`, `source`, `remote_url`, `duration_seconds`, `status`, `published_at`)
SELECT @creator_id, @music_category_id, '演示视频：午后音乐时间',
       '用于演示搜索、收藏和评论的数据。', '音乐,午后,演示',
       0, '/img/cover-fallback.png',
       'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
       1, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
       30, 1, NOW(3) - INTERVAL 7 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM `video` WHERE `owner_id` = @creator_id AND `title` = '演示视频：午后音乐时间'
);

INSERT INTO `video`
    (`owner_id`, `category_id`, `title`, `description`, `tags`, `original_type`, `cover_url`,
     `play_url`, `source`, `remote_url`, `duration_seconds`, `status`, `published_at`)
SELECT @creator_id, @anime_category_id, '演示视频：花园里的小发现',
       '用于演示分区浏览和稍后观看的数据。', '动画,花园,演示',
       0, '/img/cover-fallback.png',
       'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
       1, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
       30, 1, NOW(3) - INTERVAL 14 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM `video` WHERE `owner_id` = @creator_id AND `title` = '演示视频：花园里的小发现'
);

SET @video_flower_id = (
    SELECT `id` FROM `video` WHERE `owner_id` = @creator_id AND `title` = '演示视频：窗边的春日花朵'
    ORDER BY `id` LIMIT 1
);
SET @video_music_id = (
    SELECT `id` FROM `video` WHERE `owner_id` = @creator_id AND `title` = '演示视频：午后音乐时间'
    ORDER BY `id` LIMIT 1
);
SET @video_garden_id = (
    SELECT `id` FROM `video` WHERE `owner_id` = @creator_id AND `title` = '演示视频：花园里的小发现'
    ORDER BY `id` LIMIT 1
);

INSERT IGNORE INTO `video_stats`
    (`video_id`, `play_count`, `danmaku_count`, `like_count`, `favorite_count`, `comment_count`)
VALUES
    (@video_flower_id, 128, 2, 2, 1, 1),
    (@video_music_id, 86, 2, 1, 1, 1),
    (@video_garden_id, 54, 2, 1, 1, 1);

INSERT IGNORE INTO `user_follow` (`follower_id`, `followee_id`, `last_seen_post_at`)
VALUES
    (@viewer_id, @creator_id, NOW(3) - INTERVAL 1 DAY),
    (@friend_id, @creator_id, NOW(3) - INTERVAL 2 DAY),
    (@creator_id, @viewer_id, NOW(3) - INTERVAL 1 DAY);

INSERT IGNORE INTO `video_reaction` (`user_id`, `video_id`, `reaction_type`)
VALUES
    (@viewer_id, @video_flower_id, 1),
    (@friend_id, @video_flower_id, 1),
    (@viewer_id, @video_music_id, 1),
    (@creator_id, @video_garden_id, 1);

INSERT INTO `favorite_item` (`folder_id`, `user_id`, `video_id`)
SELECT @viewer_travel_folder_id, @viewer_id, @video_flower_id
WHERE NOT EXISTS (
    SELECT 1 FROM `favorite_item`
    WHERE `folder_id` = @viewer_travel_folder_id AND `video_id` = @video_flower_id
);

INSERT INTO `favorite_item` (`folder_id`, `user_id`, `video_id`)
SELECT @viewer_travel_folder_id, @viewer_id, @video_garden_id
WHERE NOT EXISTS (
    SELECT 1 FROM `favorite_item`
    WHERE `folder_id` = @viewer_travel_folder_id AND `video_id` = @video_garden_id
);

INSERT INTO `favorite_item` (`folder_id`, `user_id`, `video_id`)
SELECT @friend_folder_id, @friend_id, @video_music_id
WHERE NOT EXISTS (
    SELECT 1 FROM `favorite_item`
    WHERE `folder_id` = @friend_folder_id AND `video_id` = @video_music_id
);

INSERT INTO `user_post` (`author_id`, `video_id`, `title`, `content`, `status`, `created_at`)
SELECT @creator_id, @video_flower_id, '春日分享', '今天拍到一段很治愈的小花视频。', 1, NOW(3) - INTERVAL 2 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM `user_post` WHERE `author_id` = @creator_id AND `title` = '春日分享'
);

SET @post_spring_id = (
    SELECT `id` FROM `user_post` WHERE `author_id` = @creator_id AND `title` = '春日分享'
    ORDER BY `id` LIMIT 1
);

INSERT INTO `comment`
    (`video_id`, `author_id`, `content`, `status`, `created_at`)
SELECT @video_flower_id, @viewer_id, '画面很舒服，像是春天到了。', 1, NOW(3) - INTERVAL 1 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM `comment` WHERE `video_id` = @video_flower_id
      AND `author_id` = @viewer_id AND `content` = '画面很舒服，像是春天到了。'
);

INSERT INTO `comment`
    (`video_id`, `author_id`, `content`, `status`, `created_at`)
SELECT @video_music_id, @friend_id, '适合午休时听一会儿。', 1, NOW(3) - INTERVAL 5 HOUR
WHERE NOT EXISTS (
    SELECT 1 FROM `comment` WHERE `video_id` = @video_music_id
      AND `author_id` = @friend_id AND `content` = '适合午休时听一会儿。'
);

INSERT INTO `comment`
    (`post_id`, `author_id`, `content`, `status`, `created_at`)
SELECT @post_spring_id, @viewer_id, '这段分享太可爱了！', 1, NOW(3) - INTERVAL 1 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM `comment` WHERE `post_id` = @post_spring_id
      AND `author_id` = @viewer_id AND `content` = '这段分享太可爱了！'
);

INSERT IGNORE INTO `danmaku`
    (`video_id`, `user_id`, `content`, `color`, `font_size`, `mode`, `video_time_ms`)
VALUES
    (@video_flower_id, @viewer_id, '好漂亮的花！', '#FFFFFF', 25, 0, 3200),
    (@video_flower_id, @friend_id, '春天的感觉', '#FF8A65', 25, 0, 11200),
    (@video_music_id, @viewer_id, '午后好适合这首', '#FFFFFF', 25, 0, 8200),
    (@video_music_id, @creator_id, '感谢收看', '#80DEEA', 25, 1, 21000),
    (@video_garden_id, @friend_id, '发现小惊喜', '#FFFFFF', 25, 0, 5100),
    (@video_garden_id, @viewer_id, '收藏起来了', '#FFD54F', 25, 0, 17400);

INSERT IGNORE INTO `watch_later` (`user_id`, `video_id`)
VALUES (@viewer_id, @video_music_id), (@friend_id, @video_garden_id);

INSERT IGNORE INTO `watch_history`
    (`user_id`, `video_id`, `progress_seconds`, `watched_times`, `last_watched_at`)
VALUES
    (@viewer_id, @video_flower_id, 18, 2, NOW(3) - INTERVAL 2 HOUR),
    (@viewer_id, @video_music_id, 9, 1, NOW(3) - INTERVAL 1 DAY),
    (@friend_id, @video_garden_id, 24, 3, NOW(3) - INTERVAL 4 HOUR);

-- 根据演示明细回算派生计数，重复执行不会不断累加。
UPDATE `video_stats` s
SET s.`like_count` = (SELECT COUNT(*) FROM `video_reaction` r
                      WHERE r.`video_id` = s.`video_id` AND r.`reaction_type` = 1),
    s.`favorite_count` = (SELECT COUNT(*) FROM `favorite_item` f WHERE f.`video_id` = s.`video_id`),
    s.`comment_count` = (SELECT COUNT(*) FROM `comment` c
                         WHERE c.`video_id` = s.`video_id` AND c.`status` = 1 AND c.`deleted_at` IS NULL),
    s.`danmaku_count` = (SELECT COUNT(*) FROM `danmaku` d WHERE d.`video_id` = s.`video_id`)
WHERE s.`video_id` IN (@video_flower_id, @video_music_id, @video_garden_id);

UPDATE `favorite_folder` f
SET f.`item_count` = (SELECT COUNT(*) FROM `favorite_item` i WHERE i.`folder_id` = f.`id`)
WHERE f.`id` IN (@viewer_folder_id, @viewer_travel_folder_id, @friend_folder_id, @creator_folder_id);

UPDATE `user_stats` s
SET s.`video_count` = (SELECT COUNT(*) FROM `video` v
                       WHERE v.`owner_id` = s.`user_id` AND v.`deleted_at` IS NULL),
    s.`post_count` = (SELECT COUNT(*) FROM `user_post` p
                      WHERE p.`author_id` = s.`user_id` AND p.`deleted_at` IS NULL),
    s.`favorite_count` = (SELECT COUNT(*) FROM `favorite_item` f WHERE f.`user_id` = s.`user_id`),
    s.`follower_count` = (SELECT COUNT(*) FROM `user_follow` f WHERE f.`followee_id` = s.`user_id`),
    s.`following_count` = (SELECT COUNT(*) FROM `user_follow` f WHERE f.`follower_id` = s.`user_id`),
    s.`play_total` = (SELECT COALESCE(SUM(vs.`play_count`), 0)
                      FROM `video` v JOIN `video_stats` vs ON vs.`video_id` = v.`id`
                      WHERE v.`owner_id` = s.`user_id`)
WHERE s.`user_id` IN (@creator_id, @viewer_id, @friend_id);

INSERT INTO `banner` (`image_url`, `content`, `link_url`, `sort_order`, `enabled`)
SELECT '/img/hero-bg.png', '欢迎来到青芒视频', '/', 10, 1
WHERE NOT EXISTS (
    SELECT 1 FROM `banner` WHERE `content` = '欢迎来到青芒视频' AND `image_url` = '/img/hero-bg.png'
);

INSERT IGNORE INTO `search_keyword_daily` (`stat_date`, `keyword`, `search_count`)
VALUES
    (CURRENT_DATE, '春日花朵', 12),
    (CURRENT_DATE, '午后音乐', 8),
    (CURRENT_DATE, '生活记录', 6);

COMMIT;
