package com.qingmang.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationCenterService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public NotificationCenterService(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc = jdbc; this.json = json; }

    public Map<String, Object> settings(Long user) {
        return jdbc.queryForMap("""
                SELECT CAST(COALESCE(p.notify_message,1) AS SIGNED) AS messageWarn,
                  CAST(COALESCE(p.notify_comment,1) AS SIGNED) AS replyMessageWarn,
                  CAST(COALESCE(p.notify_at,1) AS SIGNED) AS atMessageWarn,
                  CAST(COALESCE(p.notify_like,1) AS SIGNED) AS likeMessageWarn
                FROM `user` u LEFT JOIN user_privacy p ON p.user_id=u.id WHERE u.id=?
                """, user);
    }

    @Transactional
    public void settings(Long user, int message, int reply, int at, int like) {
        if (message < 0 || message > 1 || reply < 0 || reply > 2 || at < 0 || at > 2 || like < 0 || like > 1)
            throw new BizException(ErrorCode.PARAM_INVALID, "消息设置不正确");
        jdbc.update("INSERT INTO user_privacy(user_id,notify_message,notify_comment,notify_at,notify_like) VALUES(?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE notify_message=VALUES(notify_message),notify_comment=VALUES(notify_comment),"
                + "notify_at=VALUES(notify_at),notify_like=VALUES(notify_like)", user, message, reply, at, like);
    }

    public List<Map<String, Object>> replies(Long user, int page) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM notification WHERE receiver_id=? "
                + "AND type IN(2,3) ORDER BY id DESC LIMIT 10 OFFSET ?", user, offset(page));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> reply = comment(id(row.get("target_id")), user);
            Map<String, Object> item = actor(row.get("actor_id"));
            item.put("id", row.get("id"));
            item.put("replyComments", reply);
            Long parent = id(reply.get("replyCommentId"));
            item.put("mainComments", parent == null ? null : comment(parent, user));
            item.put("videos", video(id(reply.get("videoId"))));
            item.put("dynamic", post(id(reply.get("dynamicId")), user));
            item.put("replyCommentControls", Map.of("controls", liked(user, id(reply.get("id"))) ? 1 : 0));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> mentions(Long user, int page) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT n.* FROM notification n WHERE n.receiver_id=? AND n.type=1
                  AND (n.target_type<>1 OR EXISTS(SELECT 1 FROM video v WHERE v.id=n.target_id
                    AND v.status=1 AND v.deleted_at IS NULL))
                ORDER BY n.id DESC LIMIT 10 OFFSET ?
                """, user, offset(page));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int target = ((Number) row.get("target_type")).intValue();
            Long targetId = id(row.get("target_id"));
            Map<String, Object> item = actor(row.get("actor_id"));
            Map<String, Object> c = target == 2 ? comment(targetId, user) : null;
            Long videoId = target == 1 ? targetId : c == null ? null : id(c.get("videoId"));
            Long postId = target == 3 ? targetId : c == null ? null : id(c.get("dynamicId"));
            Map<String, Object> at = new LinkedHashMap<>();
            at.put("id", row.get("id"));
            at.put("videoId", videoId);
            at.put("dynamicId", postId);
            item.put("at", at);
            item.put("comments", c);
            item.put("mainComments", c == null || c.get("mainCommentId") == null ? null : comment(id(c.get("mainCommentId")), user));
            item.put("videos", video(videoId));
            item.put("dynamic", post(postId, user));
            item.put("commentControls", Map.of("controls", c != null && liked(user, targetId) ? 1 : 0));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> likes(Long user, int page) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT target_type,target_id,MAX(id) AS id,MAX(created_at) AS created_at
                FROM notification WHERE receiver_id=? AND type IN(4,5,8)
                GROUP BY target_type,target_id ORDER BY id DESC LIMIT 10 OFFSET ?
                """, user, offset(page));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int target = ((Number) row.get("target_type")).intValue();
            Long targetId = id(row.get("target_id"));
            Map<String, Object> item = new LinkedHashMap<>();
            int type = target == 1 ? 1 : target == 2 ? 2 : 3;
            item.put("id", row.get("id"));
            item.put("likes", Map.of("id", row.get("id"), "likeType", type));
            Map<String, Object> content = target == 1 ? video(targetId)
                    : target == 2 ? comment(targetId, user) : post(targetId, user);
            Long muted = jdbc.queryForObject("SELECT COUNT(*) FROM notification_target_mute "
                    + "WHERE user_id=? AND target_type=? AND target_id=?", Long.class, user, target, targetId);
            content.put("likeWarn", muted != null && muted > 0 ? 0 : 1);
            item.put("videos", target == 1 ? content : null);
            item.put("comments", target == 2 ? content : null);
            item.put("dynamic", target == 3 ? content : null);
            List<Map<String, Object>> actors = jdbc.queryForList("""
                    SELECT u.id,u.nickname AS userName,u.avatar_url AS avatarAddress
                    FROM notification n JOIN `user` u ON u.id=n.actor_id
                    WHERE n.receiver_id=? AND n.target_type=? AND n.target_id=? AND n.type IN(4,5,8)
                    GROUP BY u.id,u.nickname,u.avatar_url ORDER BY MAX(n.id) DESC LIMIT 2
                    """, user, target, targetId);
            item.put("userInfoList", actors);
            item.put("likeTime", time(row.get("created_at")));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> likeUsers(Long user, Long targetId, int type, int page) {
        int target = target(type);
        requireOwner(user, target, targetId);
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT n.actor_id AS id,MAX(n.created_at) AS likeTime,u.nickname AS userName,u.avatar_url AS avatarAddress,
                  EXISTS(SELECT 1 FROM user_follow f WHERE f.follower_id=? AND f.followee_id=n.actor_id) AS followed
                FROM notification n JOIN `user` u ON u.id=n.actor_id AND u.deleted_at IS NULL
                WHERE n.receiver_id=? AND n.target_type=? AND n.target_id=? AND n.type IN(4,5,8)
                GROUP BY n.actor_id,u.nickname,u.avatar_url ORDER BY MAX(n.id) DESC LIMIT 10 OFFSET ?
                """, user, user, target, targetId, offset(page));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("id"));
            item.put("userInfo2", Map.of("id", row.get("id"), "userName", row.get("userName"), "avatarAddress", row.get("avatarAddress")));
            item.put("isFansFlag", truth(row.get("followed")) ? 1 : 0);
            item.put("likeTime", time(row.get("likeTime")));
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void read(Long user, int menu) {
        String types = switch (menu) {
            case 2 -> "2,3";
            case 3 -> "1";
            case 4 -> "4,5,8";
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "通知栏目不正确");
        };
        jdbc.update("UPDATE notification SET is_read=1 WHERE receiver_id=? AND type IN(" + types + ")", user);
    }

    @Transactional
    public void delete(Long user, Long targetId, int menu, int type) {
        int changed;
        if (menu == 3) changed = jdbc.update("DELETE FROM notification WHERE receiver_id=? AND id=? AND type=1", user, targetId);
        else if (menu == 2) changed = jdbc.update("DELETE FROM notification WHERE receiver_id=? AND target_id=? "
                + "AND type IN(2,3)", user, targetId);
        else if (menu == 4) changed = jdbc.update("DELETE FROM notification WHERE receiver_id=? AND target_id=? "
                + "AND target_type=? AND type IN(4,5,8)", user, targetId, target(type));
        else throw new BizException(ErrorCode.PARAM_INVALID);
        if (changed == 0) throw new BizException(ErrorCode.NOT_FOUND, "通知不存在或已删除");
    }

    @Transactional
    public boolean toggleMute(Long user, Long id, int type) {
        int target = target(type);
        requireOwner(user, target, id);
        if (jdbc.update("DELETE FROM notification_target_mute WHERE user_id=? AND target_type=? AND target_id=?",
                user, target, id) > 0) return false;
        jdbc.update("INSERT INTO notification_target_mute(user_id,target_type,target_id) VALUES(?,?,?) "
                + "ON DUPLICATE KEY UPDATE target_id=VALUES(target_id)", user, target, id);
        return true;
    }

    public void publish(Long actor, Long receiver, int type, int target, Long targetId, String text) {
        if (receiver == null || receiver.equals(actor)) return;
        boolean like = List.of(4, 5, 8).contains(type);
        String preference = like ? "notify_like" : "notify_comment";
        String content = text == null ? "" : text.replaceAll("<[^>]*>", "").strip();
        if (content.length() > 450) content = content.substring(0, 450);
        jdbc.update("INSERT INTO notification(receiver_id,actor_id,type,target_type,target_id,content) "
                + "SELECT ?,?,?,?,?,? WHERE COALESCE((SELECT " + preference + " FROM user_privacy WHERE user_id=?),1)<>0 "
                + "AND (COALESCE((SELECT " + preference + " FROM user_privacy WHERE user_id=?),1)<>2 "
                + "OR EXISTS(SELECT 1 FROM user_follow WHERE follower_id=? AND followee_id=?)) "
                + (like ? "AND NOT EXISTS(SELECT 1 FROM notification_target_mute WHERE user_id=? AND target_type=? AND target_id=?) " : "")
                + "AND NOT EXISTS(SELECT 1 FROM notification WHERE receiver_id=? AND actor_id=? AND type=? AND target_type=? AND target_id=?)",
                publishArgs(actor, receiver, type, target, targetId, content, like));
    }

    public void removeLike(Long actor, Long receiver, int type, int target, Long id) {
        if (receiver != null) jdbc.update("DELETE FROM notification WHERE receiver_id=? AND actor_id=? AND type=? "
                + "AND target_type=? AND target_id=?", receiver, actor, type, target, id);
    }

    private Object[] publishArgs(Long actor, Long receiver, int type, int target, Long id, String content, boolean like) {
        List<Object> args = new ArrayList<>(List.of(receiver, actor, type, target, id, content, receiver, receiver, receiver, actor));
        if (like) args.addAll(List.of(receiver, target, id));
        args.addAll(List.of(receiver, actor, type, target, id));
        return args.toArray();
    }

    private void requireOwner(Long user, int target, Long id) {
        if (id == null || id <= 0) throw new BizException(ErrorCode.PARAM_INVALID);
        String table = target == 1 ? "video" : target == 2 ? "comment" : "user_post";
        String owner = target == 1 ? "owner_id" : "author_id";
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE id=? AND " + owner + "=?", Long.class, id, user);
        if (count == null || count == 0) throw new BizException(ErrorCode.NO_PERMISSION);
    }

    private Map<String, Object> actor(Object actorId) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id AS userId,nickname AS userName,"
                + "avatar_url AS avatarAddress FROM `user` WHERE id=? AND deleted_at IS NULL", actorId);
        return rows.isEmpty() ? new LinkedHashMap<>(Map.of("userId", actorId == null ? 0 : actorId,
                "userName", "已注销用户", "avatarAddress", "/img/avatar-default.png")) : new LinkedHashMap<>(rows.get(0));
    }

    private Map<String, Object> comment(Long id, Long viewer) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = id == null ? List.of() : jdbc.queryForList("""
                SELECT c.*,u.nickname FROM comment c LEFT JOIN `user` u ON u.id=c.author_id WHERE c.id=?
                """, id);
        Map<String, Object> row = rows.isEmpty() ? Map.of() : rows.get(0);
        Long postId = id(row.get("post_id"));
        boolean available = !rows.isEmpty() && row.get("deleted_at") == null && ((Number) row.get("status")).intValue() == 1
                && (postId == null || postAvailable(postId, viewer));
        result.put("id", id);
        result.put("videoId", row.get("video_id"));
        result.put("dynamicId", postId);
        result.put("userId", row.get("author_id"));
        result.put("userName", row.getOrDefault("nickname", "已注销用户"));
        result.put("mainCommentId", row.get("root_id"));
        result.put("replyCommentId", row.get("reply_to_id"));
        result.put("content", available ? row.get("content") : "该评论已删除或不可见");
        result.put("imgAddress", available ? images(row.get("image_urls")) : "");
        result.put("deleteSign", available ? 0 : 1);
        result.put("status", available ? 0 : 1);
        result.put("likeCommentNumber", row.getOrDefault("like_count", 0));
        result.put("commentTime", time(row.get("created_at")));
        return result;
    }

    private Map<String, Object> video(Long id) {
        List<Map<String, Object>> rows = id == null ? List.of() : jdbc.queryForList("""
                SELECT v.id,v.title,v.cover_url AS coverAddress,COALESCE(s.like_count,0) AS likeNumber
                FROM video v LEFT JOIN video_stats s ON s.video_id=v.id WHERE v.id=? AND v.status=1 AND v.deleted_at IS NULL
                """, id);
        if (!rows.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>(rows.get(0));
            result.put("deleteSign", 0);
            result.put("status", 1);
            return result;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("title", "视频已删除或不可见");
        result.put("coverAddress", "");
        result.put("deleteSign", 1);
        result.put("status", 0);
        result.put("likeNumber", 0);
        return result;
    }

    private Map<String, Object> post(Long id, Long viewer) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM user_post WHERE id=? AND status=1 "
                + "AND deleted_at IS NULL AND (visibility=0 OR author_id=?)", id, viewer);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("title", rows.isEmpty() ? "动态已删除或不可见" : rows.get(0).get("content"));
        result.put("content", result.get("title"));
        result.put("imgAddress", rows.isEmpty() ? "" : images(rows.get(0).get("image_urls")));
        result.put("publishTime", rows.isEmpty() ? null : time(rows.get(0).get("created_at")));
        result.put("likeNumber", rows.isEmpty() ? 0 : rows.get(0).get("like_count"));
        result.put("deleteSign", rows.isEmpty() ? 1 : 0);
        result.put("status", rows.isEmpty() ? 1 : 0);
        return result;
    }

    private boolean postAvailable(Long id, Long user) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM user_post WHERE id=? AND status=1 AND deleted_at IS NULL "
                + "AND (visibility=0 OR author_id=?)", Long.class, id, user);
        return count != null && count > 0;
    }

    private boolean liked(Long user, Long id) {
        if (id == null) return false;
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM comment_like WHERE user_id=? AND comment_id=? AND is_dislike=0",
                Long.class, user, id);
        return count != null && count > 0;
    }

    private String images(Object value) {
        if (value == null) return "";
        try {
            String text = value instanceof byte[] bytes ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : value.toString();
            List<String> urls = json.readValue(text, new TypeReference<List<String>>() {});
            return String.join(",", urls) + (urls.isEmpty() ? "" : ",");
        } catch (JsonProcessingException error) { throw new BizException(ErrorCode.DB_ERROR, "通知关联的图片数据格式不正确"); }
    }

    private static int target(int type) {
        if (type < 1 || type > 3) throw new BizException(ErrorCode.PARAM_INVALID);
        return type;
    }
    private static int offset(int page) {
        if (page < 1 || page > 1000) throw new BizException(ErrorCode.PARAM_INVALID);
        return (page - 1) * 10;
    }
    private static Long id(Object value) { return value == null ? null : ((Number) value).longValue(); }
    private static boolean truth(Object value) { return Boolean.TRUE.equals(value) || value instanceof Number n && n.intValue() != 0; }
    private static String time(Object value) {
        if (value == null) return "";
        LocalDateTime date = value instanceof Timestamp stamp ? stamp.toLocalDateTime() : (LocalDateTime) value;
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
