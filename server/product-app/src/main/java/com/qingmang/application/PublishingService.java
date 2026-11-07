package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.interfaces.dto.PublishingRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class PublishingService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbc;
    private final MediaStorageService media;
    private final AuthService auth;
    private final MentionNotificationService mentions;

    public PublishingService(JdbcTemplate jdbc, MediaStorageService media, AuthService auth,
                             MentionNotificationService mentions) {
        this.jdbc = jdbc;
        this.media = media;
        this.auth = auth;
        this.mentions = mentions;
    }

    @Transactional
    public void save(Long actor, PublishingRequest req, MultipartFile cover, String videoName,
                     List<String> tags, boolean edit) {
        auth.requireActiveUser(actor);
        Map<String, Object> before = edit ? owned(actor, req.getId()) : null;
        if (tags == null) tags = List.of();
        if (tags.size() > 5 ||
                tags.stream().anyMatch(t -> t == null || t.isBlank() || t.length() > 50 || t.contains(",")))
            throw new BizException(ErrorCode.PARAM_INVALID, "请填写有效的视频标签");
        String category = switch (req.getSubZoneKey()) {
            case "原创动画" -> "国创";
            case "动画剧场" -> "番剧";
            default -> req.getSubZoneKey();
        };
        List<Long> categories = jdbc.queryForList("SELECT id FROM category WHERE name=? AND is_enabled=1 LIMIT 1",
                Long.class, category);
        if (categories.isEmpty()) throw new BizException(ErrorCode.PARAM_INVALID, "分区不存在或未启用");
        String play;
        if (req.getVideoSource() == 1) {
            requireRemoteUrl(req.getRemoteUrl());
            play = "";
        } else if (videoName != null && !videoName.isBlank()) {
            media.requireOwnedVideo(videoName, actor);
            Long used = jdbc.queryForObject("SELECT COUNT(*) FROM video WHERE play_url=? AND deleted_at IS NULL AND id<>?",
                    Long.class, videoName, edit ? req.getId() : 0L);
            if (used != null && used > 0) throw new BizException(ErrorCode.VIDEO_DUPLICATED);
            play = videoName;
        } else if (edit && number(before.get("source")) == 0) {
            play = (String) before.get("play_url");
        } else throw new BizException(ErrorCode.PARAM_INVALID, "请先上传视频");
        String coverUrl = cover != null && !cover.isEmpty() ? media.store(cover, "cover", actor)
                : edit ? (String) before.get("cover_url") : null;
        if (coverUrl == null || coverUrl.isBlank()) throw new BizException(ErrorCode.PARAM_INVALID, "请上传封面");
        String[] time = req.getVideoTime().split(":");
        long seconds = Long.parseLong(time[0]) * 60 + Integer.parseInt(time[1]);
        if (Integer.parseInt(time[1]) >= 60 || seconds > Integer.MAX_VALUE)
            throw new BizException(ErrorCode.PARAM_INVALID, "视频时长不正确");
        String tagString = String.join(",", tags);
        String subCategory = Objects.toString(req.getSubZoneValue(), "");
        String remote = req.getVideoSource() == 1 ? req.getRemoteUrl() : null;
        Long id;
        if (edit) {
            id = req.getId();
            int count = jdbc.update("UPDATE video SET category_id=?,title=?,description=?,description_html=?,cover_url=?,"
                            + "play_url=?,source=?,remote_url=?,duration_seconds=?,status=2,reject_reason=NULL,"
                            + "tags=?,original_type=?,allow_remix=?,sub_category_name=?,updated_at=NOW(3)"
                            + " WHERE id=? AND owner_id=? AND deleted_at IS NULL",
                    categories.get(0), req.getTitle().trim(), req.getContent(), req.getContentHtml(), coverUrl,
                    play, req.getVideoSource(), remote, seconds, tagString, req.getType(), req.getAllowTwo(),
                    subCategory, id, actor);
            if (count != 1) throw new BizException(ErrorCode.DB_ERROR);
            if (number(before.get("status")) == 1)
                jdbc.update("UPDATE user_stats SET video_count=GREATEST(video_count-1,0) WHERE user_id=?", actor);
        } else {
            GeneratedKeyHolder key = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                var statement = connection.prepareStatement("INSERT INTO video(owner_id,category_id,title,description,"
                                + "description_html,cover_url,play_url,source,remote_url,duration_seconds,status,tags,"
                                + "original_type,allow_remix,sub_category_name) VALUES(?,?,?,?,?,?,?,?,?,?,2,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                Object[] values = {actor, categories.get(0), req.getTitle().trim(), req.getContent(), req.getContentHtml(),
                        coverUrl, play, req.getVideoSource(), remote, seconds, tagString, req.getType(),
                        req.getAllowTwo(), subCategory};
                for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
                return statement;
            }, key);
            if (key.getKey() == null) throw new BizException(ErrorCode.DB_ERROR);
            id = key.getKey().longValue();
            jdbc.update("INSERT INTO video_stats(video_id) VALUES(?)", id);
        }
        jdbc.update("INSERT INTO video_status_log(video_id,from_status,to_status,reason,operator_id) VALUES(?,?,2,?,?)",
                id, edit ? number(before.get("status")) : 0, edit ? "作者编辑后重新审核" : "作者投稿", actor);
        mentions.publish(actor, 1, id, req.getMentionUserIds(), req.getContent());
    }

    public Map<String, Object> detail(Long actor, Long id) { return legacy(owned(actor, id)); }

    public Map<String, Object> list(Long actor, String title, String category, String sort, int status, int page) {
        auth.requireActiveUser(actor);
        if (page < 1 || page > 100000 || status < -1 || status > 2)
            throw new BizException(ErrorCode.PARAM_INVALID);
        StringBuilder where = new StringBuilder(" WHERE v.owner_id=? AND v.deleted_at IS NULL");
        List<Object> args = new ArrayList<>();
        args.add(actor);
        if (title != null && !title.isBlank()) { where.append(" AND v.title LIKE ?"); args.add("%" + title.trim() + "%"); }
        if (category != null && !category.isBlank() && !category.startsWith("全部")) {
            where.append(" AND c.name=?");
            args.add(switch (category) { case "原创动画" -> "国创"; case "动画剧场" -> "番剧"; default -> category; });
        }
        if (status == 0) where.append(" AND v.status IN(0,2)");
        if (status == 1) where.append(" AND v.status=1");
        if (status == 2) where.append(" AND v.status=3");
        String from = " FROM video v LEFT JOIN category c ON c.id=v.category_id LEFT JOIN video_stats s ON s.video_id=v.id";
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + from + where, Long.class, args.toArray());
        String order = switch (sort == null ? "" : sort) {
            case "播放量排序" -> "COALESCE(s.play_count,0)";
            case "收藏量排序" -> "COALESCE(s.favorite_count,0)";
            case "弹幕量排序" -> "COALESCE(s.danmaku_count,0)";
            case "评论数排序" -> "COALESCE(s.comment_count,0)";
            default -> "v.created_at";
        };
        args.add((page - 1) * 5);
        List<Map<String, Object>> rows = jdbc.queryForList(columns() + from + where
                + " ORDER BY " + order + " DESC,v.id DESC LIMIT 5 OFFSET ?", args.toArray());
        Map<String, Object> result = new LinkedHashMap<>(jdbc.queryForMap("SELECT COUNT(*) AS videoAllNumber,"
                + "COALESCE(SUM(status IN(0,2)),0) AS videoWaitNumber,COALESCE(SUM(status=1),0) AS videoSuccessNumber,"
                + "COALESCE(SUM(status=3),0) AS videoErrorNumber FROM video WHERE owner_id=? AND deleted_at IS NULL", actor));
        result.put("users", Map.of("id", actor));
        result.put("videos", rows.stream().map(this::legacy).toList());
        result.put("total", total);
        return result;
    }

    public Map<String, Object> statistics(Long actor) {
        auth.requireActiveUser(actor);
        return jdbc.queryForMap("SELECT COALESCE(SUM(s.play_count),0) AS playNumber,"
                + "COALESCE(SUM(s.like_count),0) AS likeNumber,COALESCE(SUM(s.comment_count),0) AS commentNumber,"
                + "COALESCE(SUM(s.favorite_count),0) AS collectNumber,COALESCE(SUM(s.share_count),0) AS shareNumber,"
                + "COALESCE(SUM(s.danmaku_count),0) AS scrollingNumber,"
                + "COALESCE(SUM(s.new_fans_count),0) AS newFansNumber FROM video v LEFT JOIN video_stats s ON s.video_id=v.id"
                + " WHERE v.owner_id=? AND v.deleted_at IS NULL", actor);
    }

    @Transactional
    public void delete(Long actor, Long id) {
        Map<String, Object> video = owned(actor, id);
        if (jdbc.update("UPDATE video SET deleted_at=NOW(3),updated_at=NOW(3) WHERE id=? AND owner_id=? AND deleted_at IS NULL",
                id, actor) != 1) throw new BizException(ErrorCode.DB_ERROR);
        if (number(video.get("status")) == 1)
            jdbc.update("UPDATE user_stats SET video_count=GREATEST(video_count-1,0) WHERE user_id=?", actor);
    }

    public boolean inUse(String url) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM video WHERE play_url=? AND deleted_at IS NULL", Long.class, url);
        return count != null && count > 0;
    }

    private Map<String, Object> owned(Long actor, Long id) {
        auth.requireActiveUser(actor);
        if (id == null || id <= 0) throw new BizException(ErrorCode.PARAM_INVALID);
        List<Map<String, Object>> rows = jdbc.queryForList(columns()
                + " FROM video v LEFT JOIN category c ON c.id=v.category_id LEFT JOIN video_stats s ON s.video_id=v.id"
                + " WHERE v.id=? AND v.deleted_at IS NULL", id);
        if (rows.isEmpty()) throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        if (((Number) rows.get(0).get("owner_id")).longValue() != actor)
            throw new BizException(ErrorCode.NO_PERMISSION);
        return rows.get(0);
    }

    private static String columns() {
        return "SELECT v.*,c.name AS category_name,COALESCE(s.play_count,0) AS play_count,"
                + "COALESCE(s.like_count,0) AS like_count,COALESCE(s.favorite_count,0) AS favorite_count,"
                + "COALESCE(s.comment_count,0) AS comment_count,COALESCE(s.share_count,0) AS share_count,"
                + "COALESCE(s.danmaku_count,0) AS danmaku_count";
    }

    private Map<String, Object> legacy(Map<String, Object> row) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", row.get("id"));
        v.put("userId", row.get("owner_id"));
        v.put("title", row.get("title"));
        v.put("content", Objects.toString(row.get("description"), ""));
        v.put("contentHtml", Objects.toString(row.get("description_html"), ""));
        v.put("coverAddress", row.get("cover_url"));
        v.put("videoAddress", row.get("play_url"));
        v.put("videoSource", row.get("source"));
        v.put("remoteUrl", row.get("remote_url"));
        v.put("subZoneKey", row.get("category_name"));
        v.put("subZoneValue", row.get("sub_category_name"));
        v.put("tag", row.get("tags"));
        v.put("type", row.get("original_type"));
        v.put("allowTwo", number(row.get("allow_remix")));
        v.put("status", switch (number(row.get("status"))) { case 1 -> 1; case 3 -> 2; default -> 0; });
        v.put("examineFiledMessage", row.get("reject_reason"));
        int seconds = number(row.get("duration_seconds"));
        v.put("videoTime", "%02d:%02d".formatted(seconds / 60, seconds % 60));
        Object created = row.get("created_at");
        v.put("createTime", formatDateTime(created));
        for (String[] pair : List.of(new String[]{"playNumber","play_count"}, new String[]{"likeNumber","like_count"},
                new String[]{"commentNumber","comment_count"}, new String[]{"collectNumber","favorite_count"},
                new String[]{"shareNumber","share_count"}, new String[]{"scrollingNumber","danmaku_count"})) v.put(pair[0], row.get(pair[1]));
        return v;
    }

    private static String formatDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DATE_TIME_FORMATTER);
        }
        throw new IllegalStateException("Unexpected created_at type: " + value.getClass().getName());
    }

    private static int number(Object value) {
        return value instanceof Number n ? n.intValue() : Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private static void requireRemoteUrl(String value) {
        try {
            URI uri = new URI(value == null ? "" : value);
            if (!Set.of("http", "https").contains(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT))
                    || uri.getHost() == null || uri.getUserInfo() != null || value.contains("\\"))
                throw new BizException(ErrorCode.PARAM_INVALID, "请填写有效的视频直链");
        } catch (URISyntaxException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "视频直链格式不正确");
        }
    }

    public List<String> extractRemoteVideoUrls(String pageUrl) {
        String normalized = normalizeRemoteUrl(pageUrl);
        List<String> result = new ArrayList<>();
        Set<String> seen = new java.util.LinkedHashSet<>();
        //如果传入本身就是可播放的视频地址，直接返回
        if (isDirectVideoUrl(normalized)) {
            seen.add(normalized);
        }
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(normalized).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("Referer", normalized);
            connection.setRequestProperty("Connection", "keep-alive");
            int status = connection.getResponseCode();
            if (status >= 400) {
                throw new BizException(ErrorCode.PARAM_INVALID, "页面抓取失败：" + status);
            }
            java.io.InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (stream == null) throw new BizException(ErrorCode.PARAM_INVALID, "页面抓取失败：空响应");
            StringBuilder bodyBuilder = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    bodyBuilder.append(line).append('\n');
                    if (bodyBuilder.length() > 2_000_000) break;
                }
            }
            String body = bodyBuilder.toString();
            if (body.length() > 2_000_000) body = body.substring(0, 2_000_000);
            collectFromHtml(body, normalized, seen);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "页面抓取失败：" + (e.getMessage() == null ? "未知错误" : e.getMessage()));
        }
        result.addAll(seen);
        if (result.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "未在页面中识别到可播放的视频源");
        }
        if (result.size() > 20) result = new ArrayList<>(result.subList(0, 20));
        return result;
    }

    private static String normalizeRemoteUrl(String value) {
        if (value == null || value.isBlank()) throw new BizException(ErrorCode.PARAM_INVALID, "请填写页面地址");
        String v = value.trim();
        if (v.startsWith("//")) v = "https:" + v;
        try {
            URI uri = new URI(v);
            if (!Set.of("http", "https").contains(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT))
                    || uri.getHost() == null) throw new BizException(ErrorCode.PARAM_INVALID, "页面地址格式不正确");
        } catch (URISyntaxException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "页面地址格式不正确");
        }
        return v;
    }

    private static boolean isDirectVideoUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.matches(".*\\.(m3u8|mp4|webm|flv|mov|mkv|m4v|m3u)(\\?.*)?$");
    }

    private static void collectFromHtml(String body, String baseUrl, Set<String> seen) {
        //meta 标签中的视频地址
        for (String attr : new String[]{"og:video", "og:video:url", "og:video:secure_url", "twitter:player:stream"}) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                    "<meta[^>]+(?:property|name)\\s*=\\s*['\"]" + java.util.regex.Pattern.quote(attr) + "['\"][^>]*content\\s*=\\s*['\"]([^'\"]+)['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(body);
            while (m.find()) addCandidate(seen, m.group(1), baseUrl);
            java.util.regex.Matcher m2 = java.util.regex.Pattern.compile(
                    "<meta[^>]+content\\s*=\\s*['\"]([^'\"]+)['\"][^>]*(?:property|name)\\s*=\\s*['\"]" + java.util.regex.Pattern.quote(attr) + "['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(body);
            while (m2.find()) addCandidate(seen, m2.group(1), baseUrl);
        }
        //video/source 标签
        java.util.regex.Matcher vm = java.util.regex.Pattern.compile(
                "<(?:video|source)[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(body);
        while (vm.find()) addCandidate(seen, vm.group(1), baseUrl);
        //data-src / data-url
        java.util.regex.Matcher dm = java.util.regex.Pattern.compile(
                "data-(?:src|url|video)\\s*=\\s*['\"]([^'\"]+)['\"]", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(body);
        while (dm.find()) addCandidate(seen, dm.group(1), baseUrl);
        //JSON 字段
        java.util.regex.Matcher jm = java.util.regex.Pattern.compile(
                "\"(?:url|src|playUrl|videoUrl|video_url|source|file|mp4|m3u8)\"\\s*:\\s*\"([^\"]+)\"",
                java.util.regex.Pattern.CASE_INSENSITIVE).matcher(body);
        while (jm.find()) addCandidate(seen, jm.group(1), baseUrl);
        //直接出现的视频链接
        java.util.regex.Matcher um = java.util.regex.Pattern.compile(
                "https?://[^'\"<>\\s]+?\\.(?:m3u8|mp4|webm|flv|mov|mkv|m4v|m3u)(?:\\?[^'\"<>\\s]*)?",
                java.util.regex.Pattern.CASE_INSENSITIVE).matcher(body);
        while (um.find()) addCandidate(seen, um.group(0), baseUrl);
    }

    private static String extractVideoId(String url) {
        if (url == null) return null;
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query == null) return null;
            for (String part : query.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && "videoId".equalsIgnoreCase(kv[0])) {
                    return kv[1];
                }
            }
        } catch (Exception ignored) {
        }
        //兜底：路径或字符串中匹配 BV 数字
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("BV(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }
    private static void addCandidate(Set<String> seen, String raw, String baseUrl) {
        if (raw == null) return;
        String v = raw.trim().replace("\\/", "/").replace("\\u0026", "&");
        if (v.isEmpty() || v.startsWith("data:") || v.startsWith("blob:")) return;
        if (v.startsWith("//")) v = "https:" + v;
        if (!v.toLowerCase(Locale.ROOT).startsWith("http://") && !v.toLowerCase(Locale.ROOT).startsWith("https://")) {
            //尝试相对路径拼接
            try {
                v = new URI(baseUrl).resolve(v).toString();
            } catch (Exception e) {
                return;
            }
        }
        if (!v.toLowerCase(Locale.ROOT).startsWith("http://") && !v.toLowerCase(Locale.ROOT).startsWith("https://")) return;
        String lower = v.toLowerCase(Locale.ROOT);
        if (!(lower.contains(".m3u8") || lower.contains(".mp4") || lower.contains(".webm") || lower.contains(".flv")
                || lower.contains(".mov") || lower.contains(".mkv") || lower.contains(".m4v") || lower.contains(".m3u"))) return;
        if (v.length() > 2000) return;
        seen.add(v);
    }
}

