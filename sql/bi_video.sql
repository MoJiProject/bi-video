/*
 Navicat Premium Data Transfer

 Source Server         : bi_video
 Source Server Type    : MySQL
 Source Server Version : 50726 (5.7.26)
 Source Host           : localhost:3306
 Source Schema         : bi_video

 Target Server Type    : MySQL
 Target Server Version : 50726 (5.7.26)
 File Encoding         : 65001

 Date: 30/11/2025 19:52:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for at
-- ----------------------------
DROP TABLE IF EXISTS `at`;
CREATE TABLE `at`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `at_user_id` int(11) NOT NULL,
  `video_id` int(11) NULL DEFAULT NULL,
  `comment_id` int(11) NULL DEFAULT NULL,
  `dynamic_id` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_at_video_id`(`user_id`, `at_user_id`, `video_id`) USING BTREE,
  INDEX `idx_at_comment_id`(`at_user_id`, `user_id`, `comment_id`) USING BTREE,
  INDEX `idx_at_dynamic_id`(`user_id`, `at_user_id`, `dynamic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for collects
-- ----------------------------
DROP TABLE IF EXISTS `collects`;
CREATE TABLE `collects`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `collect_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `collect_time` datetime NOT NULL,
  `delete_flag` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_collects_user_id`(`user_id`) USING BTREE,
  INDEX `idx_collects_video_id`(`video_id`) USING BTREE,
  INDEX `idx_collects_uc`(`user_id`, `collect_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for collects_classify
-- ----------------------------
DROP TABLE IF EXISTS `collects_classify`;
CREATE TABLE `collects_classify`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `collect_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NOT NULL DEFAULT 1,
  `video_number` int(11) NOT NULL DEFAULT 0,
  `cover_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_collects_classify_uc`(`user_id`, `collect_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment_controls
-- ----------------------------
DROP TABLE IF EXISTS `comment_controls`;
CREATE TABLE `comment_controls`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `video_id` int(11) NULL DEFAULT NULL,
  `controls` int(11) NULL DEFAULT NULL,
  `comment_id` int(11) NOT NULL,
  `like_comment_time` datetime NULL DEFAULT NULL,
  `dynamic_id` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_controls_uvc`(`user_id`, `video_id`, `comment_id`) USING BTREE,
  INDEX `idx_comment_controls_udc`(`user_id`, `comment_id`, `dynamic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `video_id` int(11) NULL DEFAULT NULL,
  `comment_time` datetime NOT NULL,
  `like_comment_number` int(11) NOT NULL DEFAULT 0,
  `content` varchar(10000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `img_address` varchar(10000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dynamic_flag` int(11) NOT NULL DEFAULT 0,
  `dislike` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `reply_comment_id` int(11) NULL DEFAULT NULL,
  `main_comment_id` int(11) NULL DEFAULT NULL,
  `delete_sign` int(11) NOT NULL DEFAULT 0,
  `reply_user_id` int(11) NULL DEFAULT NULL,
  `notification_reply_flag` int(11) NOT NULL DEFAULT 1,
  `like_warn` int(11) NOT NULL DEFAULT 1,
  `up_flag` int(11) NOT NULL DEFAULT 0,
  `dynamic_id` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comments_uv`(`user_id`, `video_id`) USING BTREE,
  INDEX `idx_comments_ud`(`user_id`, `dynamic_id`) USING BTREE,
  INDEX `idx_comments_reply_user_id`(`reply_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dialogue
-- ----------------------------
DROP TABLE IF EXISTS `dialogue`;
CREATE TABLE `dialogue`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `dialogue_id` int(11) NOT NULL,
  `dnd` int(11) NULL DEFAULT 0,
  `new_content` varchar(10000) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `up_date_time` datetime NULL DEFAULT NULL,
  `sign` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dialogue_user_id`(`user_id`) USING BTREE,
  INDEX `idx_dialogue_dialogue_id`(`dialogue_id`) USING BTREE,
  INDEX `idx_dialogue_ud`(`user_id`, `dialogue_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dynamic
-- ----------------------------
DROP TABLE IF EXISTS `dynamic`;
CREATE TABLE `dynamic`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `video_id` int(11) NULL DEFAULT NULL,
  `fans_id` int(11) NULL DEFAULT NULL,
  `follow_id` int(11) NOT NULL,
  `watch_dynamic_flag` int(11) NOT NULL DEFAULT 0,
  `fans_flag` int(11) NOT NULL DEFAULT 1,
  `wait_watch` int(11) NOT NULL DEFAULT 0,
  `comment_id` int(11) NULL DEFAULT NULL,
  `img_address` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `content` varchar(10000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `up_flag` int(11) NOT NULL DEFAULT 0,
  `like_number` int(11) NOT NULL DEFAULT 0,
  `comment_number` int(11) NOT NULL DEFAULT 0,
  `share_number` int(11) NOT NULL DEFAULT 0,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `publish_time` datetime NULL DEFAULT NULL,
  `dynamic_id` int(11) NULL DEFAULT NULL,
  `like_warn` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dynamic_ff`(`fans_id`, `follow_id`) USING BTREE,
  INDEX `idx_dynamic_ffd`(`fans_id`, `follow_id`, `dynamic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 222 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for fans
-- ----------------------------
DROP TABLE IF EXISTS `fans`;
CREATE TABLE `fans`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `fans_id` int(11) NOT NULL,
  `new_fans_id` int(11) NULL DEFAULT NULL,
  `fans_user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_fans_uf`(`user_id`, `fans_id`) USING BTREE,
  INDEX `idx_fans_user_id`(`user_id`) USING BTREE,
  INDEX `idx_fans_fans_id`(`fans_id`) USING BTREE,
  INDEX `idx_fans_unf`(`user_id`, `new_fans_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `follow_id` int(11) NOT NULL,
  `follow_user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_follow_uf`(`user_id`, `follow_id`) USING BTREE,
  INDEX `idx_follow_user_id`(`user_id`) USING BTREE,
  INDEX `idx_follow_follow_id`(`follow_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for history
-- ----------------------------
DROP TABLE IF EXISTS `history`;
CREATE TABLE `history`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `watch_video_date` datetime NOT NULL,
  `watch_video_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `watch_video_flag` int(11) NOT NULL DEFAULT 0,
  `watch_current_time` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_history_uv`(`video_id`, `user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for key_word
-- ----------------------------
DROP TABLE IF EXISTS `key_word`;
CREATE TABLE `key_word`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `word` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `fond_id` int(11) NOT NULL,
  `like_time` datetime NOT NULL,
  `like_user_id` int(11) NOT NULL,
  `like_type` int(11) NOT NULL,
  `delete_flag` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_likes_uflt`(`user_id`, `fond_id`, `like_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 154 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for private_message
-- ----------------------------
DROP TABLE IF EXISTS `private_message`;
CREATE TABLE `private_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) NOT NULL,
  `content` varchar(10000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `send_time` datetime NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `select_sign` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `delete_sign` int(11) NULL DEFAULT NULL,
  `message_type` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_private_message_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_private_message_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_private_message_sr`(`sender_id`, `receiver_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for scrolling
-- ----------------------------
DROP TABLE IF EXISTS `scrolling`;
CREATE TABLE `scrolling`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `video_id` int(11) NOT NULL,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `send_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `color` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `size` int(11) NOT NULL,
  `location` int(11) NOT NULL,
  `video_time` double NOT NULL,
  `top` int(11) NOT NULL DEFAULT 0,
  `all_display_top` int(255) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scrolling_uv`(`user_id`, `video_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for throw_coin
-- ----------------------------
DROP TABLE IF EXISTS `throw_coin`;
CREATE TABLE `throw_coin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `video_id` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_throw_coin_uv`(`user_id`, `video_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `gender` int(1) NULL DEFAULT NULL COMMENT '0是男1是女',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `avatar_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `video_number` int(11) NOT NULL DEFAULT 0,
  `collect_number` int(11) NOT NULL DEFAULT 0,
  `grade` int(1) NULL DEFAULT 0,
  `create_time` datetime NOT NULL,
  `coin_number` int(11) NOT NULL DEFAULT 0,
  `question` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `all_message_number` int(11) NOT NULL DEFAULT 0,
  `at_number` int(11) NOT NULL DEFAULT 0,
  `like_all_number` int(11) NOT NULL DEFAULT 0,
  `reply_comment_number` int(11) NOT NULL DEFAULT 0,
  `message_number` int(11) NOT NULL DEFAULT 0,
  `dynamic_number` int(11) NOT NULL DEFAULT 0,
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `exp` int(11) NOT NULL DEFAULT 0,
  `follow_number` int(11) NOT NULL DEFAULT 0,
  `fans_number` int(11) NOT NULL DEFAULT 0,
  `own_dynamic_number` int(11) NOT NULL DEFAULT 0,
  `introduce` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `like_number` int(11) NOT NULL DEFAULT 0,
  `login_date_time` datetime NULL DEFAULT NULL,
  `message_warn` int(11) NOT NULL DEFAULT 1,
  `reply_message_warn` int(11) NOT NULL DEFAULT 1,
  `at_message_warn` int(11) NOT NULL DEFAULT 1,
  `like_message_warn` int(11) NOT NULL DEFAULT 1,
  `background_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `birthday` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `public_collect` int(11) NOT NULL DEFAULT 1,
  `public_anime` int(11) NOT NULL DEFAULT 1,
  `public_coin` int(11) NOT NULL DEFAULT 1,
  `public_love` int(11) NOT NULL DEFAULT 1,
  `public_follow_list` int(11) NOT NULL DEFAULT 1,
  `public_fans_list` int(11) NOT NULL DEFAULT 1,
  `public_birthday` int(11) NOT NULL DEFAULT 1,
  `history_flag` int(11) NOT NULL DEFAULT 1,
  `admin_flag` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12383 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for videos
-- ----------------------------
DROP TABLE IF EXISTS `videos`;
CREATE TABLE `videos`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_id` int(10) NOT NULL,
  `tag` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `content_html` varchar(10000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `content` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `video_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `like_number` int(11) NOT NULL DEFAULT 0,
  `comment_number` int(11) NOT NULL DEFAULT 0,
  `collect_number` int(11) NOT NULL DEFAULT 0,
  `share_number` int(11) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL,
  `cover_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `coin_throw_number` int(11) NOT NULL DEFAULT 0,
  `type` int(11) NULL DEFAULT NULL,
  `sub_zone_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `allow_two` int(11) NULL DEFAULT 0,
  `sub_zone_value` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `play_number` int(10) NOT NULL DEFAULT 0,
  `status` int(1) NOT NULL DEFAULT 0,
  `new_fans_number` int(11) NOT NULL DEFAULT 0,
  `scrolling_number` int(11) NOT NULL DEFAULT 0,
  `video_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `examine_filed_message` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `wait_watch` int(11) UNSIGNED NULL DEFAULT 0,
  `like_warn` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_videos_user_id`(`user_id`) USING BTREE,
  INDEX `idx_videos_title`(`title`) USING BTREE,
  INDEX `idx_videos_status`(`status`) USING BTREE,
  INDEX `idx_videos_sub_zone_key`(`sub_zone_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
