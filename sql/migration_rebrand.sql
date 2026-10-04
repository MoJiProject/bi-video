-- ============================================================
--  去 B 站化 · 数据迁移
--  品牌改为「青芒视频」后，以下中文串原本是数据库里的**业务数据值**
--  （不是界面文案），代码已同步改名，历史数据必须一并迁移，
--  否则会出现「收藏数对不上 / 待看清单里的视频消失 / 清空失败」等问题。
--
--  执行方式：mysql -u<user> -p bi_video < sql/migration_rebrand.sql
--  建议先备份：mysqldump -u<user> -p bi_video > backup.sql
-- ============================================================


-- ------------------------------------------------------------
-- 1. 稍后再看  ->  待看清单
--    出现在 collects 与 collects_classify 两张表的 collect_name 上，
--    两张表都要改，否则收藏夹列表与待看清单会互相对不上。
-- ------------------------------------------------------------
UPDATE `collects`         SET `collect_name` = '待看清单' WHERE `collect_name` = '稍后再看';
UPDATE `collects_classify` SET `collect_name` = '待看清单' WHERE `collect_name` = '稍后再看';


-- ------------------------------------------------------------
-- 2. 全部分区  ->  全部分类
--    videos.sub_zone_key 里存的是分区名，与前端下拉、查询条件共用同一套字符串。
--    注意：后端 SystemServiceImpl.getSubZoneKeys() 直接从库里读分区列表，
--    所以这里改完，下拉选项会自动跟着变，不需要动代码。
-- ------------------------------------------------------------
UPDATE `videos` SET `sub_zone_key` = '全部分类' WHERE `sub_zone_key` = '全部分区';


-- ------------------------------------------------------------
-- 3. 默认头像路径  /img/默认头像.gif  ->  /img/avatar-default.png
--    用户注册时把这个路径写进了 users.avatar_address，
--    UserServiceImpl 里判断「用户是否换过头像」也依赖这个字面量。
-- ------------------------------------------------------------
UPDATE `users` SET `avatar_address` = '/img/avatar-default.png'
 WHERE `avatar_address` = '/img/默认头像.gif';


-- ------------------------------------------------------------
-- 4. 核对（可选执行，只读）
-- ------------------------------------------------------------
-- SELECT collect_name, COUNT(*) FROM collects GROUP BY collect_name;
-- SELECT collect_name, COUNT(*) FROM collects_classify GROUP BY collect_name;
-- SELECT sub_zone_key, COUNT(*) FROM videos GROUP BY sub_zone_key;
-- SELECT COUNT(*) FROM users WHERE avatar_address = '/img/avatar-default.png';