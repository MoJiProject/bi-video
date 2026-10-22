package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentionNotificationService {
    private final JdbcTemplate jdbc;

    public MentionNotificationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void publish(Long actor, int targetType, Long targetId, List<Long> recipients, String preview) {
        Set<Long> ids = new LinkedHashSet<>(recipients == null ? List.of() : recipients);
        if (ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0))
            throw new BizException(ErrorCode.PARAM_INVALID, "@用户参数不正确");
        ids.remove(actor);
        List<Long> active = ids.isEmpty() ? List.of() : jdbc.queryForList(
                "SELECT id FROM `user` WHERE status=1 AND deleted_at IS NULL AND id IN(" + placeholders(ids.size()) + ")",
                Long.class, ids.toArray());
        if (active.size() != ids.size()) throw new BizException(ErrorCode.PARAM_INVALID, "被@的用户不存在或不可用");
        String text = Objects.toString(preview, "").replaceAll("<[^>]*>", "").strip();
        if (text.length() > 450) text = text.substring(0, 450);
        for (Long receiver : ids) {
            jdbc.update("INSERT INTO notification(receiver_id,actor_id,type,target_type,target_id,content) "
                    + "SELECT ?,?,1,?,?,? WHERE COALESCE((SELECT notify_at FROM user_privacy WHERE user_id=?),1)<>0 "
                    + "AND (COALESCE((SELECT notify_at FROM user_privacy WHERE user_id=?),1)<>2 "
                    + "OR EXISTS(SELECT 1 FROM user_follow WHERE follower_id=? AND followee_id=?)) "
                    + "AND NOT EXISTS(SELECT 1 FROM notification WHERE receiver_id=? AND actor_id=? "
                    + "AND type=1 AND target_type=? AND target_id=?)",
                    receiver, actor, targetType, targetId, text, receiver, receiver, receiver, actor, receiver, actor, targetType, targetId);
        }
        List<Object> args = new ArrayList<>(List.of(actor, targetType, targetId));
        String removed = ids.isEmpty() ? "" : " AND receiver_id NOT IN(" + placeholders(ids.size()) + ")";
        args.addAll(ids);
        jdbc.update("DELETE FROM notification WHERE actor_id=? AND type=1 AND target_type=? AND target_id=?" + removed,
                args.toArray());
    }

    public Map<String, Object> list(Long receiver, Long cursorId, int size) {
        if (size < 1 || size > 100) throw new BizException(ErrorCode.PARAM_INVALID);
        List<Object> args = new ArrayList<>(List.of(receiver, receiver));
        String where = " WHERE n.receiver_id=? AND n.type=1 AND (n.target_type<>1 OR v.id IS NOT NULL)";
        if (cursorId != null) { where += " AND n.id<?"; args.add(cursorId); }
        args.add(size + 1);
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT n.id,n.actor_id AS actorId,u.nickname AS actorNickname,u.avatar_url AS actorAvatar,
                  n.target_type AS targetType,n.target_id AS targetId,n.created_at AS createdAt,n.is_read AS isRead,
                  CASE WHEN n.target_type=1 THEN v.id ELSE c.video_id END AS videoId,
                  CASE WHEN n.target_type=3 THEN p.id ELSE c.post_id END AS postId,
                  c.root_id AS rootId,
                  CASE WHEN (n.target_type=1 AND v.id IS NOT NULL)
                    OR (n.target_type=3 AND p.id IS NOT NULL)
                    OR (n.target_type=2 AND c.id IS NOT NULL AND (v.id IS NOT NULL OR p.id IS NOT NULL))
                    THEN n.content ELSE '该内容已删除或不可见' END AS content,
                  CASE WHEN n.target_type=1 THEN v.id IS NOT NULL
                    WHEN n.target_type=3 THEN p.id IS NOT NULL
                    ELSE c.id IS NOT NULL AND (v.id IS NOT NULL OR p.id IS NOT NULL) END AS available
                FROM notification n
                LEFT JOIN `user` u ON u.id=n.actor_id AND u.deleted_at IS NULL
                LEFT JOIN comment c ON n.target_type=2 AND c.id=n.target_id AND c.deleted_at IS NULL AND c.status=1
                LEFT JOIN video v ON v.id=CASE WHEN n.target_type=1 THEN n.target_id ELSE c.video_id END
                  AND v.status=1 AND v.deleted_at IS NULL
                LEFT JOIN user_post p ON p.id=CASE WHEN n.target_type=3 THEN n.target_id ELSE c.post_id END
                  AND p.status=1 AND p.deleted_at IS NULL AND (p.visibility=0 OR p.author_id=?)
                """ + where + " ORDER BY n.id DESC LIMIT ?", args.toArray());
        boolean more = rows.size() > size;
        List<Map<String, Object>> records = more ? rows.subList(0, size) : rows;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("hasMore", more);
        result.put("cursorId", records.isEmpty() ? null : records.get(records.size() - 1).get("id"));
        return result;
    }

    public Map<String, Object> unread(Long userId) {
        Map<String, Object> counts = new LinkedHashMap<>(jdbc.queryForMap("""
                SELECT COALESCE(SUM(type=1),0) AS atNumber,
                  COALESCE(SUM(type IN(2,3)),0) AS replyCommentNumber,
                  COALESCE(SUM(type IN(4,5,8)),0) AS likeAllNumber
                FROM notification n WHERE receiver_id=? AND is_read=0
                  AND (target_type<>1 OR EXISTS(SELECT 1 FROM video v
                    WHERE v.id=n.target_id AND v.status=1 AND v.deleted_at IS NULL))
                """, userId));
        Long messages = jdbc.queryForObject("SELECT COUNT(*) FROM private_message WHERE receiver_id=? "
                + "AND is_read=0 AND deleted_at IS NULL AND receiver_deleted_at IS NULL AND revoked_at IS NULL", Long.class, userId);
        counts.put("messageNumber", messages);
        long total = counts.values().stream().mapToLong(value -> ((Number) value).longValue()).sum();
        counts.put("allMessageNumber", total);
        return counts;
    }

    @Transactional
    public void read(Long receiver, List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 1000 || ids.stream().anyMatch(id -> id == null || id <= 0))
            throw new BizException(ErrorCode.PARAM_INVALID);
        List<Object> args = new ArrayList<>(List.of(receiver));
        args.addAll(ids);
        jdbc.update("UPDATE notification SET is_read=1 WHERE receiver_id=? AND type=1 AND id IN("
                + placeholders(ids.size()) + ")", args.toArray());
    }

    @Transactional
    public void delete(Long receiver, Long id) {
        if (jdbc.update("DELETE FROM notification WHERE id=? AND receiver_id=? AND type=1", id, receiver) != 1)
            throw new BizException(ErrorCode.NOT_FOUND, "通知不存在或无权删除");
    }

    private static String placeholders(int count) {
        return java.util.stream.IntStream.range(0, count).mapToObj(index -> "?").collect(Collectors.joining(","));
    }
}
