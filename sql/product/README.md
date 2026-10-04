# product 库设计说明

## 建库

```sql
CREATE DATABASE product DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
mysql -u<user> -p product < sql/product/01_schema.sql
mysql -u<user> -p product < sql/product/02_seed.sql
```

## 相比旧库（bi_video）改了什么

| 问题 | 旧库 | 现在 |
|---|---|---|
| 字符集 | `utf8`（3 字节）与 `utf8mb4` 混用，同一张表里都有 | 全库 `utf8mb4 / utf8mb4_unicode_ci` |
| 外键 | 一条都没有，全靠应用层 | 关键关系都有外键 + 级联 |
| 一表 multifunction | `dynamic` 同时装「动态」「粉丝记录」「待看清单」 | 拆成 `user_post` / `user_follow` / `watch_later` |
| 重复数据 | `follow` 和 `fans` 是同一份数据的两种写法 | 只留 `user_follow`，粉丝列表反向查 |
| 关联键用字符串 | `collects.collect_name` 用收藏夹**名字**关联 | `favorite_folder.id` 外键 |
| 分区用字符串 | `videos.sub_zone_key` + 前端硬编码「全部分区」当哨兵值 | `category` 表 + `video.category_id` |
| 类型错乱 | `scrolling.send_time varchar(255)`、`history.watch_video_time varchar(255)`、`users.birthday varchar(255)`、`int(1)` 当布尔 | `DATETIME(3)` / `INT UNSIGNED` / `TINYINT(1)` |
| 计数混在主表 | `users` 上挂了 11 个计数字段 | 拆到 `user_stats` / `video_stats` |
| 搜索 | 无全文索引，`LIKE '%x%'` 全表扫 | `video` 上 `FULLTEXT ... WITH PARSER ngram` |
| 私信 | 没有会话表，收发都扫 `sender_id`/`receiver_id` | `conversation` + `private_message` |
| 审核 | 无状态流转记录 | `video_status_log` |

## 表清单（28 张）

### 用户域
| 表 | 说明 |
|---|---|
| `user` | 账号主体，只放身份信息 |
| `user_privacy` | 隐私开关与通知开关，1:1 |
| `user_stats` | 计数，派生数据 |

### 内容域
| 表 | 说明 |
|---|---|
| `category` | 分区字典，`code` 稳定、`name` 可改 |
| `video` | 视频主表 |
| `video_stats` | 视频计数，派生数据 |
| `video_status_log` | 状态流转审计 |

### 互动域
| 表 | 说明 |
|---|---|
| `video_reaction` | 点赞 / 投币合表，`(user_id, video_id, reaction_type)` 唯一 |
| `comment` | 评论，视频与动态共用（`video_id` / `post_id` 二选一） |
| `comment_like` | 评论赞踩 |
| `danmaku` | 弹幕，**故意不加外键**（写入极频繁，外键校验是瓶颈） |
| `watch_history` | 观看记录，`(user_id, video_id)` 唯一，存秒级进度 |
| `watch_later` | 待看清单 |

### 收藏域
| 表 | 说明 |
|---|---|
| `favorite_folder` | 收藏夹 |
| `favorite_item` | 收藏夹内容 |

### 社交域
| 表 | 说明 |
|---|---|
| `user_follow` | 关注关系（同时表达粉丝） |
| `user_post` | 用户动态 |
| `notification` | 站内通知（合并旧的 `at` 表） |
| `conversation` | 私信会话 |
| `private_message` | 私信消息 |

### 一起看
| 表 | 说明 |
|---|---|
| `watch_room` | 房间 |
| `watch_room_member` | 成员 |

### 运营域
| 表 | 说明 |
|---|---|
| `search_keyword` | 搜索热榜（累计） |
| `search_keyword_daily` | 搜索热榜（按天） |
| `operation_log` | 后台操作审计 |
| `user_ban` | 封禁记录 |
| `sys_config` | 系统配置 |
| `sys_sensitive_word` | 敏感词库 |

## 约定

**命名**
- 表名单数名词 + 下划线；带归属的一律带前缀（`video_stats`、`user_follow`）
- 主键统一 `id`，外键统一 `<实体>_id`
- 时间列统一 `created_at` / `updated_at`，业务时间点用业务名（`published_at`、`last_login_at`）
- 布尔统一 `is_xxx`，取值 `TINYINT(1)` 0/1
- 软删除统一 `deleted_at DATETIME NULL`，NULL 表示未删除（旧的混用 `delete_flag` / `delete_sign` / `status`）

**类型**
- 主键、外键 `BIGINT UNSIGNED`（旧的 `int(11)`，上限 21 亿，导流后不够用）
- 计数 `INT UNSIGNED` / `BIGINT UNSIGNED`
- 时间 `DATETIME(3)`，毫秒精度够用且比 `TIMESTAMP` 的 2038 问题省心
- 长文本 `TEXT` / `MEDIUMTEXT`，不再用 `varchar(10000)`

**派生数据**
`video_stats`、`user_stats`、`favorite_folder.item_count`、`watch_room.member_count` 都是派生列，
由业务层在写操作后同步更新，读接口直接用，不做实时聚合。
⚠️ 这些列和明细表的一致性由应用保证，不要在报表里当权威数据用。

**不加外键的两张表**
- `danmaku`：写入 QPS 最高，外键每行都要回查 `video`/`user`，收益低于成本。视频删除时由应用按 `video_id` 批量清理。
- `watch_history`：同上，且量大。