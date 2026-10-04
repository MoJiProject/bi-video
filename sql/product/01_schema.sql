-- =============================================================================
--  青芒视频 · product 库 · 表结构
--  目标库: product        字符集: utf8mb4 / utf8mb4_unicode_ci    引擎: InnoDB
--
--  与旧库 bi_video 的主要差异：
--    1. 全库 utf8mb4（旧的混用 utf8 三字节，存不了 emoji/生僻字）
--    2. 补齐外键约束（旧的完全靠应用层保证，加了索引与级联）
--    3. 拆表：dynamic 一张表塞了三件事 -> user_post / watch_later / user_follow
--    4. 拆表：likes + throw_coin + collects 混存 -> video_reaction / favorite_item
--    5. 删除 fans 表：它和 follow 是同一份数据的两种写法，由 user_follow 反向查询
--    6. 删除 at 表：并入 notification（type = MENTION）
--    7. 计数类字段从主表拆到 *_stats，主表只放业务字段
--    8. 时间/开关统一类型：datetime 用 DATETIME，标记位用 TINYINT(1)
--       （旧的 scrolling.send_time / history.watch_video_time 是 varchar）
--    9. 魔法字符串改外键：videos.sub_zone_key -> video.category_id
--  =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. 用户与账号
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(32)     NOT NULL COMMENT '登录账号，唯一',
    `password_hash` VARCHAR(100)    NOT NULL COMMENT '密码哈希，禁止存明文',
    `nickname`      VARCHAR(32)     NOT NULL COMMENT '昵称',
    `avatar_url`    VARCHAR(512)    NOT NULL DEFAULT '/img/avatar-default.png' COMMENT '头像地址',
    `background_url` VARCHAR(512)   NULL COMMENT '主页背景图地址',
    `signature`     VARCHAR(255)    NULL COMMENT '个性签名',
    `gender`        TINYINT         NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `birthday`      DATE            NULL COMMENT '生日',
    `phone`         VARCHAR(20)     NULL COMMENT '手机号',
    `email`         VARCHAR(128)    NULL COMMENT '邮箱',
    `role`          TINYINT         NOT NULL DEFAULT 0 COMMENT '角色 0普通用户 1管理员',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
    `level`         TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '等级 0-6',
    `exp`           INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '当前等级经验',
    `coin_balance`  INT             NOT NULL DEFAULT 0 COMMENT '硬币余额（可负，投币会扣）',
    `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '注册时间',
    `updated_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `last_login_at` DATETIME(3)     NULL COMMENT '最后登录时间',
    `deleted_at`    DATETIME(3)     NULL COMMENT '软删除时间，NULL 表示未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_phone` (`phone`),
    KEY `idx_user_created` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户账号';

DROP TABLE IF EXISTS `user_privacy`;
CREATE TABLE `user_privacy`
(
    `user_id`             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `show_favorite_list`  TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '公开收藏夹',
    `show_follow_list`    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '公开关注列表',
    `show_fans_list`      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '公开粉丝列表',
    `show_coin_income`    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '公开投币收入',
    `show_like_received`  TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '公开获赞数',
    `show_history`        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '公开观看记录',
    `notify_dynamic`      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '接收动态通知',
    `notify_comment`      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '接收评论通知',
    `notify_like`         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '接收点赞通知',
    `notify_at`           TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '接收@通知',
    `updated_at`          DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_privacy_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户隐私与通知设置';

DROP TABLE IF EXISTS `user_stats`;
CREATE TABLE `user_stats`
(
    `user_id`         BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `video_count`     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '投稿数',
    `post_count`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '动态数',
    `favorite_count`  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '收藏视频数',
    `follower_count`  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `following_count` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '关注数',
    `like_received`   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '累计获赞',
    `play_total`      BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计播放',
    `coin_received`   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '累计收到投币',
    `updated_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户维度计数（派生数据，由业务层维护）';

-- -----------------------------------------------------------------------------
-- 2. 内容：分区 / 视频 / 计数
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `code`       VARCHAR(32)     NOT NULL COMMENT '分类编码，如 anime',
    `name`       VARCHAR(32)     NOT NULL COMMENT '分类名，如 动画',
    `parent_id`  BIGINT UNSIGNED NULL COMMENT '父分类，NULL 表示一级',
    `sort_order` INT             NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `is_enabled` TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`code`),
    KEY `idx_category_sort` (`is_enabled`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='视频分区，替代旧的 sub_zone_key 魔法字符串';

DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`
(
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    `owner_id`         BIGINT UNSIGNED NOT NULL COMMENT '创作者ID',
    `category_id`      BIGINT UNSIGNED NULL COMMENT '分区ID',
    `title`            VARCHAR(200)    NOT NULL COMMENT '标题',
    `description`      TEXT            NULL COMMENT '简介（纯文本）',
    `description_html` MEDIUMTEXT      NULL COMMENT '简介（富文本）',
    `cover_url`        VARCHAR(512)    NOT NULL DEFAULT '/img/cover-fallback.png' COMMENT '封面地址',
    `play_url`         VARCHAR(512)    NOT NULL COMMENT '播放地址',
    `source`           TINYINT         NOT NULL DEFAULT 0 COMMENT '来源 0本地 1远程直链',
    `remote_url`       VARCHAR(1024)   NULL COMMENT '远程直链，source=1 时使用',
    `duration_seconds` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '时长（秒），秒级时间轴',
    `status`           TINYINT         NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1已发布 2审核中 3已下架',
    `reject_reason`    VARCHAR(255)    NULL COMMENT '下架/驳回原因',
    `allow_comment`    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '允许评论',
    `allow_danmaku`    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '允许弹幕',
    `coin_enabled`     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '允许投币',
    `published_at`     DATETIME(3)     NULL COMMENT '发布时间',
    `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at`       DATETIME(3)     NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_video_owner_status` (`owner_id`, `status`, `created_at`),
    KEY `idx_video_category` (`category_id`, `status`, `published_at`),
    KEY `idx_video_published` (`status`, `published_at`),
    FULLTEXT KEY `ft_video_search` (`title`, `description`) WITH PARSER ngram,
    CONSTRAINT `fk_video_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_video_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='视频主表，只放业务字段';

DROP TABLE IF EXISTS `video_stats`;
CREATE TABLE `video_stats`
(
    `video_id`        BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `play_count`      BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '播放量',
    `danmaku_count`   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '弹幕数',
    `like_count`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '点赞数',
    `coin_count`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '投币数',
    `favorite_count`  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '收藏数',
    `share_count`     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '分享数',
    `comment_count`   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '评论数',
    `updated_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`video_id`),
    CONSTRAINT `fk_vstats_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='视频维度计数（派生数据，与 video 1:1）';

DROP TABLE IF EXISTS `video_status_log`;
CREATE TABLE `video_status_log`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `video_id`    BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `from_status` TINYINT         NOT NULL COMMENT '变更前状态',
    `to_status`   TINYINT         NOT NULL COMMENT '变更后状态',
    `reason`      VARCHAR(255)    NULL COMMENT '原因',
    `operator_id` BIGINT UNSIGNED NULL COMMENT '操作人（管理员）',
    `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_vsl_video` (`video_id`, `created_at`),
    CONSTRAINT `fk_vsl_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='视频状态流转记录';

-- -----------------------------------------------------------------------------
-- 3. 互动：点赞/投币、评论、弹幕、观看记录
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `video_reaction`;
CREATE TABLE `video_reaction`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '操作人',
    `video_id`      BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `reaction_type` TINYINT         NOT NULL COMMENT '类型 1点赞 2投币 3收藏到默认夹',
    `coin_count`    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '投币数量，仅 type=2 有效',
    `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reaction` (`user_id`, `video_id`, `reaction_type`),
    KEY `idx_reaction_video` (`video_id`, `reaction_type`),
    KEY `idx_reaction_user_time` (`user_id`, `created_at`),
    CONSTRAINT `fk_reaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reaction_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='视频互动（点赞/投币），合表后一次查询拿到全部状态';

DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `video_id`        BIGINT UNSIGNED NULL COMMENT '视频ID，动态评论时为 NULL',
    `post_id`         BIGINT UNSIGNED NULL COMMENT '动态ID，与 video_id 二选一',
    `author_id`       BIGINT UNSIGNED NOT NULL COMMENT '评论者',
    `root_id`         BIGINT UNSIGNED NULL COMMENT '根评论ID，一级评论为 NULL',
    `reply_to_id`     BIGINT UNSIGNED NULL COMMENT '被回复的评论ID',
    `reply_to_user_id` BIGINT UNSIGNED NULL COMMENT '被回复者（冗余，省一次回表）',
    `content`         TEXT            NOT NULL COMMENT '评论内容',
    `image_urls`      JSON            NULL COMMENT '图片地址数组',
    `status`          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 0待审核 1正常 2已隐藏',
    `like_count`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '点赞数',
    `reply_count`     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '回复数',
    `created_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at`      DATETIME(3)     NULL COMMENT '软删除',
    PRIMARY KEY (`id`),
    KEY `idx_comment_video_time` (`video_id`, `status`, `created_at`),
    KEY `idx_comment_post_time` (`post_id`, `status`, `created_at`),
    KEY `idx_comment_root` (`root_id`, `created_at`),
    KEY `idx_comment_author` (`author_id`, `created_at`),
    CONSTRAINT `fk_comment_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_root` FOREIGN KEY (`root_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='评论（视频/动态共用）';

DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`
(
    `comment_id` BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `is_dislike` TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '1踩 0赞',
    `created_at` DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`comment_id`, `user_id`),
    KEY `idx_cl_user` (`user_id`),
    CONSTRAINT `fk_cl_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cl_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='评论点赞/踩';

DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`
(
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `video_id`     BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `user_id`      BIGINT UNSIGNED NULL COMMENT '发送者，NULL 表示游客',
    `content`      VARCHAR(255)    NOT NULL COMMENT '内容',
    `color`        VARCHAR(9)      NOT NULL DEFAULT '#FFFFFF' COMMENT '颜色 #RRGGBB',
    `font_size`    SMALLINT        NOT NULL DEFAULT 25 COMMENT '字号',
    `mode`         TINYINT         NOT NULL DEFAULT 0 COMMENT '模式 0滚动 1顶部 2底部 3彩色 4高级',
    `video_time_ms` INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '视频时间轴（毫秒）',
    `created_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_danmaku_video_time` (`video_id`, `video_time_ms`),
    KEY `idx_danmaku_user` (`user_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '弹幕。刻意不加外键：写入极频繁，外键校验会成为瓶颈，且视频删除由应用层按 video_id 批量清理';

DROP TABLE IF EXISTS `watch_history`;
CREATE TABLE `watch_history`
(
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `video_id`         BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `progress_seconds` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '观看进度（秒）',
    `watched_times`    INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '观看次数',
    `last_watched_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后观看时间',
    `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_history` (`user_id`, `video_id`),
    KEY `idx_history_time` (`user_id`, `last_watched_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '观看记录。每个用户每个视频只留一条（旧库也是这样，这里把进度拆成秒数而不是字符串）';

DROP TABLE IF EXISTS `watch_later`;
CREATE TABLE `watch_later`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `video_id`   BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `created_at` DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_watch_later` (`user_id`, `video_id`),
    KEY `idx_wl_user_time` (`user_id`, `created_at`),
    KEY `idx_wl_video` (`video_id`),
    CONSTRAINT `fk_wl_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_wl_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '待看清单。旧库把它塞在 dynamic 表里并用中文字符串当主键的一部分，这里独立成表';

-- -----------------------------------------------------------------------------
-- 4. 收藏夹
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `favorite_folder`;
CREATE TABLE `favorite_folder`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
    `owner_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属用户',
    `name`        VARCHAR(64)     NOT NULL COMMENT '收藏夹名称',
    `description` VARCHAR(255)    NULL COMMENT '简介',
    `cover_url`   VARCHAR(512)    NULL COMMENT '封面',
    `visibility`  TINYINT         NOT NULL DEFAULT 0 COMMENT '可见性 0私密 1公开',
    `is_default`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否默认收藏夹（删默认夹时要转移内容）',
    `sort_order`  INT             NOT NULL DEFAULT 0 COMMENT '排序',
    `item_count`  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '视频数（派生）',
    `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at`  DATETIME(3)     NULL COMMENT '软删除',
    PRIMARY KEY (`id`),
    KEY `idx_folder_owner` (`owner_id`, `is_default`, `sort_order`),
    CONSTRAINT `fk_folder_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '收藏夹。旧库用「收藏夹名字」当关联键，换个名字历史数据就全对不上，这里改成外键';

DROP TABLE IF EXISTS `favorite_item`;
CREATE TABLE `favorite_item`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `folder_id`  BIGINT UNSIGNED NOT NULL COMMENT '收藏夹ID',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属用户（冗余，便于按用户查）',
    `video_id`   BIGINT UNSIGNED NOT NULL COMMENT '视频ID',
    `created_at` DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fav_item` (`folder_id`, `video_id`),
    KEY `idx_fav_item_user` (`user_id`, `created_at`),
    KEY `idx_fav_item_video` (`video_id`),
    CONSTRAINT `fk_fav_item_folder` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folder` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fav_item_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='收藏夹内容';

-- -----------------------------------------------------------------------------
-- 5. 社交：关注 / 动态 / 通知 / 私信
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者',
    `followee_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者',
    `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `followee_id`),
    KEY `idx_follow_followee` (`followee_id`, `created_at`),
    CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '关注关系。旧库另有 fans 表，与本表是同一份数据，这里只保留一份，粉丝列表反向查本表';

DROP TABLE IF EXISTS `user_post`;
CREATE TABLE `user_post`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态ID',
    `author_id`     BIGINT UNSIGNED NOT NULL COMMENT '发布者',
    `video_id`      BIGINT UNSIGNED NULL COMMENT '关联视频',
    `content`       TEXT            NULL COMMENT '文本内容',
    `image_urls`    JSON            NULL COMMENT '图片地址数组',
    `visibility`    TINYINT         NOT NULL DEFAULT 0 COMMENT '可见性 0公开 1仅自己',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 0待审核 1正常 2已下架',
    `like_count`    INT UNSIGNED    NOT NULL DEFAULT 0,
    `comment_count` INT UNSIGNED    NOT NULL DEFAULT 0,
    `share_count`   INT UNSIGNED    NOT NULL DEFAULT 0,
    `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at`    DATETIME(3)     NULL,
    PRIMARY KEY (`id`),
    KEY `idx_post_author` (`author_id`, `created_at`),
    KEY `idx_post_video` (`video_id`),
    KEY `idx_post_feed` (`status`, `created_at`),
    CONSTRAINT `fk_post_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户动态';

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者',
    `actor_id`    BIGINT UNSIGNED NULL COMMENT '触发者',
    `type`        TINYINT         NOT NULL COMMENT '类型 1@我 2回复我 3评论我的视频 4点赞我的视频 5点赞我的评论 6关注我 7系统',
    `target_type` TINYINT         NOT NULL COMMENT '对象类型 1视频 2评论 3动态 4用户',
    `target_id`   BIGINT UNSIGNED NULL COMMENT '对象ID',
    `content`     VARCHAR(500)    NULL COMMENT '展示文案（快照）',
    `is_read`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '已读',
    `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_notify_receiver` (`receiver_id`, `is_read`, `created_at`),
    KEY `idx_notify_unread` (`receiver_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '站内通知，合掉旧的 at 表和散落在各处的 notification_* 计数';

DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`
(
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_a_id`      BIGINT UNSIGNED NOT NULL COMMENT '会话成员A（恒为较小ID，保证唯一）',
    `user_b_id`      BIGINT UNSIGNED NOT NULL COMMENT '会话成员B',
    `last_message_id` BIGINT UNSIGNED NULL COMMENT '最后一条消息',
    `last_message_at` DATETIME(3)    NULL COMMENT '最后消息时间',
    `created_at`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation` (`user_a_id`, `user_b_id`),
    KEY `idx_conv_b` (`user_b_id`, `last_message_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '私信会话。旧库每次收发都全表扫 sender/receiver，这里先定位会话再取消息';

DROP TABLE IF EXISTS `private_message`;
CREATE TABLE `private_message`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
    `sender_id`       BIGINT UNSIGNED NOT NULL COMMENT '发送者',
    `receiver_id`     BIGINT UNSIGNED NOT NULL COMMENT '接收者（冗余，便于按人查）',
    `message_type`    TINYINT         NOT NULL DEFAULT 1 COMMENT '类型 1文本 2图片 3系统',
    `content`         VARCHAR(2000)   NOT NULL COMMENT '内容',
    `is_read`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '已读',
    `created_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `deleted_at`      DATETIME(3)     NULL,
    PRIMARY KEY (`id`),
    KEY `idx_msg_conv` (`conversation_id`, `id`),
    KEY `idx_msg_receiver` (`receiver_id`, `is_read`, `created_at`),
    CONSTRAINT `fk_msg_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='私信消息';

-- -----------------------------------------------------------------------------
-- 6. 一起看
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `watch_room`;
CREATE TABLE `watch_room`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `owner_id`      BIGINT UNSIGNED NOT NULL COMMENT '房主',
    `title`         VARCHAR(120)    NOT NULL DEFAULT '一起看' COMMENT '房间标题',
    `video_id`      BIGINT UNSIGNED NULL COMMENT '当前播放视频',
    `access_code`   VARCHAR(16)     NULL COMMENT '房间号，私密时必填',
    `is_private`    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否私密',
    `member_count`  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '成员数（派生）',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 0已结束 1进行中',
    `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_room_owner` (`owner_id`, `created_at`),
    KEY `idx_room_status` (`status`, `created_at`),
    CONSTRAINT `fk_room_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='一起看房间';

DROP TABLE IF EXISTS `watch_room_member`;
CREATE TABLE `watch_room_member`
(
    `room_id`    BIGINT UNSIGNED NOT NULL COMMENT '房间ID',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '成员',
    `joined_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`room_id`, `user_id`),
    KEY `idx_room_member_user` (`user_id`),
    CONSTRAINT `fk_rm_room` FOREIGN KEY (`room_id`) REFERENCES `watch_room` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='一起看成员';

-- -----------------------------------------------------------------------------
-- 7. 搜索与运营
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `search_keyword`;
CREATE TABLE `search_keyword`
(
    `keyword`        VARCHAR(64)     NOT NULL COMMENT '关键词',
    `search_count`   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '累计搜索次数',
    `last_searched_at` DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后搜索时间',
    PRIMARY KEY (`keyword`),
    KEY `idx_kw_count` (`search_count`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='搜索词热榜';

DROP TABLE IF EXISTS `search_keyword_daily`;
CREATE TABLE `search_keyword_daily`
(
    `stat_date`     DATE            NOT NULL COMMENT '统计日期',
    `keyword`       VARCHAR(64)     NOT NULL COMMENT '关键词',
    `search_count`  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '当日搜索次数',
    PRIMARY KEY (`stat_date`, `keyword`),
    KEY `idx_kwd_kw` (`keyword`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT
    = '搜索词按天分表统计，日榜从这张表聚合，不用扫全量热榜';

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `operator_id`   BIGINT UNSIGNED NULL COMMENT '操作人',
    `operator_name` VARCHAR(32)     NULL COMMENT '操作人名快照',
    `module`        VARCHAR(32)     NOT NULL COMMENT '模块 user/video/comment/post/message',
    `action`        VARCHAR(32)     NOT NULL COMMENT '动作 delete/publish/ban/...',
    `target_type`   VARCHAR(32)     NULL COMMENT '对象类型',
    `target_id`     BIGINT UNSIGNED NULL COMMENT '对象ID',
    `detail`        VARCHAR(1000)   NULL COMMENT '补充说明',
    `success`       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否成功',
    `ip`            VARCHAR(45)     NULL COMMENT '来源IP（兼容IPv6）',
    `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_oplog_operator` (`operator_id`, `created_at`),
    KEY `idx_oplog_module` (`module`, `created_at`),
    KEY `idx_oplog_created` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='后台操作审计';

DROP TABLE IF EXISTS `user_ban`;
CREATE TABLE `user_ban`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '被封禁用户',
    `reason`        VARCHAR(500)    NULL COMMENT '原因',
    `status`        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '1封禁中 0已解除',
    `operator_id`   BIGINT UNSIGNED NULL COMMENT '操作管理员',
    `banned_at`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `released_at`   DATETIME(3)     NULL COMMENT '解除时间',
    PRIMARY KEY (`id`),
    KEY `idx_ban_user_status` (`user_id`, `status`),
    KEY `idx_ban_status` (`status`, `banned_at`),
    CONSTRAINT `fk_ban_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户封禁';

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`
(
    `config_key`   VARCHAR(64)     NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(1000)   NOT NULL COMMENT '配置值',
    `description`  VARCHAR(255)    NULL COMMENT '说明',
    `updated_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`config_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统配置项';

DROP TABLE IF EXISTS `sys_sensitive_word`;
CREATE TABLE `sys_sensitive_word`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `word`       VARCHAR(64)     NOT NULL COMMENT '敏感词',
    `word_type`  TINYINT         NOT NULL DEFAULT 1 COMMENT '1评论 2私信 3昵称 4标题',
    `is_enabled` TINYINT(1)      NOT NULL DEFAULT 1,
    `created_at` DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sensitive` (`word`, `word_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='敏感词库';

SET FOREIGN_KEY_CHECKS = 1;