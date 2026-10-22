package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivateMessageService {
    private final JdbcTemplate jdbc;
    private final AuthService auth;
    private final ApplicationEventPublisher events;
    private final MediaStorageService media;
    private static final String VISIBLE = "m.deleted_at IS NULL AND "
            + "CASE WHEN m.sender_id=? THEN m.sender_deleted_at ELSE m.receiver_deleted_at END IS NULL";

    public PrivateMessageService(JdbcTemplate jdbc, AuthService auth, ApplicationEventPublisher events,
                                 MediaStorageService media) {
        this.jdbc = jdbc;
        this.auth = auth;
        this.events = events;
        this.media = media;
    }

    public List<Map<String, Object>> dialogues(Long user, int page) {
        return dialogues(user, page, null);
    }

    public List<Map<String, Object>> dialogues(Long user, int page, Long peerFilter) {
        auth.requireActiveUser(user);
        int offset = offset(page);
        List<Object> args = new ArrayList<>(List.of(user, user, user, user));
        String filter = "";
        if (peerFilter != null) {
            if (peerFilter <= 0 || peerFilter.equals(user)) throw new BizException(ErrorCode.PARAM_INVALID);
            filter = " AND (c.user_a_id=? OR c.user_b_id=?)";
            args.add(peerFilter);
            args.add(peerFilter);
        }
        args.add(offset);
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT c.id,c.user_a_id,c.user_b_id,
                  CASE WHEN c.user_a_id=? THEN c.a_pinned_at ELSE c.b_pinned_at END AS pinned,
                  CASE WHEN c.user_a_id=? THEN c.a_muted ELSE c.b_muted END AS muted
                FROM conversation c
                WHERE ((c.user_a_id=? AND (c.a_deleted_at IS NULL OR c.last_message_at>c.a_deleted_at))
                   OR (c.user_b_id=? AND (c.b_deleted_at IS NULL OR c.last_message_at>c.b_deleted_at)))
                """ + filter + """
                ORDER BY pinned IS NOT NULL DESC,pinned DESC,COALESCE(c.last_message_at,c.created_at) DESC,c.id DESC
                LIMIT 20 OFFSET ?
                """, args.toArray());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long id = number(row.get("id"));
            Long peer = user.equals(number(row.get("user_a_id"))) ? number(row.get("user_b_id")) : number(row.get("user_a_id"));
            List<Map<String, Object>> latest = jdbc.queryForList(
                    "SELECT m.* FROM private_message m WHERE m.conversation_id=? AND " + VISIBLE
                            + " ORDER BY m.id DESC LIMIT 1", id, user);
            Map<String, Object> dialogue = new LinkedHashMap<>();
            dialogue.put("id", id);
            dialogue.put("dialogueId", peer);
            dialogue.put("userId", user);
            dialogue.put("sign", id.toString());
            dialogue.put("upDateTime", time(row.get("pinned")));
            dialogue.put("dnd", truth(row.get("muted")) ? 1 : 0);
            dialogue.put("newContent", latest.isEmpty() ? null : preview(latest.get(0), user));
            dialogue.put("updateTime", latest.isEmpty() ? null : time(latest.get(0).get("created_at")));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dialogue", dialogue);
            item.put("userInfo", userBrief(peer));
            item.put("notReadNumber", jdbc.queryForObject("SELECT COUNT(*) FROM private_message m "
                    + "WHERE m.conversation_id=? AND m.receiver_id=? AND m.is_read=0 AND m.revoked_at IS NULL AND "
                    + VISIBLE, Long.class, id, user, user));
            item.put("allMessageNumber", jdbc.queryForObject("SELECT COUNT(*) FROM private_message m "
                    + "WHERE m.conversation_id=? AND " + VISIBLE, Long.class, id, user));
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> messages(Long user, Long peer, int page) {
        auth.requireActiveUser(user);
        Long conversation = findConversation(user, peer);
        if (conversation == null) throw new BizException(ErrorCode.NOT_FOUND, "会话不存在");
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT m.* FROM private_message m "
                + "WHERE m.conversation_id=? AND " + VISIBLE + " ORDER BY m.id ASC LIMIT 20 OFFSET ?",
                conversation, user, offset(page));
        return Map.of("privateMessage", rows.stream().map(this::messageView).toList(),
                "userInfo", userBrief(user), "dialogueUserInfo", userBrief(peer));
    }

    public Map<String, Object> messageFor(Long user, Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT m.* FROM private_message m "
                + "WHERE m.id=? AND (m.sender_id=? OR m.receiver_id=?) AND " + VISIBLE, id, user, user, user);
        if (rows.isEmpty()) return null;
        return messageView(rows.get(0));
    }

    @Transactional
    public Long addDialogue(Long user, Long peer) {
        auth.requireActiveUser(user);
        auth.requireActiveUser(peer);
        if (user.equals(peer)) throw new BizException(ErrorCode.PARAM_INVALID, "不能给自己发私信");
        long low = Math.min(user, peer), high = Math.max(user, peer);
        jdbc.update("INSERT INTO conversation(user_a_id,user_b_id) VALUES(?,?) ON DUPLICATE KEY UPDATE id=id", low, high);
        Long id = findConversation(user, peer);
        String side = user.equals(low) ? "a" : "b";
        jdbc.update("UPDATE conversation SET " + side + "_deleted_at=NULL WHERE id=?", id);
        return id;
    }

    @Transactional
    public Map<String, Object> send(Long actor, Long receiver, int type, String content) {
        var sender = auth.requireActiveUser(actor);
        auth.requireActiveUser(receiver);
        if (content == null || content.isBlank() || content.length() > 2000 || !List.of(1, 2, 3).contains(type))
            throw new BizException(ErrorCode.PARAM_INVALID, "消息内容或类型不正确");
        if (type == 1 && content.replaceAll("<[^>]*>", "").length() > 500)
            throw new BizException(ErrorCode.PARAM_INVALID, "消息最多500字");
        if (type == 2) {
            if (!content.startsWith("/upload/image/" + actor + "_"))
                throw new BizException(ErrorCode.PARAM_INVALID, "请先上传自己的消息图片");
            media.requireStoredImage(content);
        }
        Long video = null;
        if (type == 3) {
            try { video = Long.valueOf(content); }
            catch (NumberFormatException error) { throw new BizException(ErrorCode.PARAM_INVALID, "视频ID不正确"); }
            Long available = jdbc.queryForObject("SELECT COUNT(*) FROM video WHERE id=? AND status=1 AND deleted_at IS NULL",
                    Long.class, video);
            if (available == null || available == 0) throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        Long conversation = addDialogue(actor, receiver);
        jdbc.queryForObject("SELECT id FROM conversation WHERE id=? FOR UPDATE", Long.class, conversation);
        Long mutual = jdbc.queryForObject("SELECT COUNT(*) FROM user_follow a JOIN user_follow b "
                + "ON a.follower_id=b.followee_id AND a.followee_id=b.follower_id "
                + "WHERE a.follower_id=? AND a.followee_id=?", Long.class, actor, receiver);
        if (!Integer.valueOf(1).equals(sender.getRole()) && (mutual == null || mutual == 0)) {
            Long replies = jdbc.queryForObject("SELECT COUNT(*) FROM private_message WHERE conversation_id=? "
                    + "AND sender_id=? AND deleted_at IS NULL AND revoked_at IS NULL", Long.class, conversation, receiver);
            Long sent = jdbc.queryForObject("SELECT COUNT(*) FROM private_message WHERE conversation_id=? "
                    + "AND sender_id=? AND deleted_at IS NULL AND revoked_at IS NULL", Long.class, conversation, actor);
            if ((replies == null || replies == 0) && sent != null && sent > 0)
                throw new BizException(ErrorCode.OPERATION_FAILED, "对方回复前只能发送一条消息");
        }
        int storedType = type == 3 ? 4 : type;
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("INSERT INTO private_message(conversation_id,sender_id,"
                    + "receiver_id,message_type,content) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, conversation);
            statement.setLong(2, actor);
            statement.setLong(3, receiver);
            statement.setInt(4, storedType);
            statement.setString(5, content);
            return statement;
        }, key);
        if (key.getKey() == null) throw new BizException(ErrorCode.DB_ERROR);
        Long id = key.getKey().longValue();
        jdbc.update("UPDATE conversation SET last_message_id=?,last_message_at=NOW(3) WHERE id=?", id, conversation);
        events.publishEvent(new PrivateMessageChanged(actor, receiver, conversation, id, "created", null));
        return messageFor(actor, id);
    }

    @Transactional
    public void read(Long user, Long peer) {
        auth.requireActiveUser(user);
        Long id = findConversation(user, peer);
        if (id == null) throw new BizException(ErrorCode.NOT_FOUND, "会话不存在");
        int count = jdbc.update("UPDATE private_message SET is_read=1 WHERE conversation_id=? AND receiver_id=? "
                + "AND is_read=0 AND deleted_at IS NULL AND receiver_deleted_at IS NULL", id, user);
        if (count > 0) events.publishEvent(new PrivateMessageChanged(user, peer, id, null, "read", null));
    }

    @Transactional
    public void revoke(Long user, Long id) {
        auth.requireActiveUser(user);
        Map<String, Object> row = requireMessage(user, id);
        if (!user.equals(number(row.get("sender_id")))) throw new BizException(ErrorCode.NO_PERMISSION);
        int changed = jdbc.update("UPDATE private_message SET revoked_at=NOW(3),content='',is_read=1 "
                + "WHERE id=? AND sender_id=? AND revoked_at IS NULL AND created_at>=DATE_SUB(NOW(3),INTERVAL 3 MINUTE)",
                id, user);
        if (changed != 1) throw new BizException(ErrorCode.OPERATION_FAILED, "消息已撤回或超过3分钟");
        publish(row, id, "updated", null);
    }

    @Transactional
    public void deleteMessage(Long user, Long id) {
        auth.requireActiveUser(user);
        Map<String, Object> row = requireMessage(user, id);
        boolean sender = user.equals(number(row.get("sender_id")));
        jdbc.update("UPDATE private_message SET " + (sender ? "sender" : "receiver") + "_deleted_at=NOW(3)"
                + (sender ? "" : ",is_read=1") + " WHERE id=?", id);
        publish(row, id, "deleted", user);
    }

    @Transactional
    public void updateDialogue(Long user, Long id, String action) {
        auth.requireActiveUser(user);
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM conversation WHERE id=? "
                + "AND (user_a_id=? OR user_b_id=?) FOR UPDATE", id, user, user);
        if (rows.isEmpty()) throw new BizException(ErrorCode.NOT_FOUND, "会话不存在");
        Map<String, Object> row = rows.get(0);
        String side = user.equals(number(row.get("user_a_id"))) ? "a" : "b";
        String change = switch (action) {
            case "pin" -> side + "_pinned_at=CASE WHEN " + side + "_pinned_at IS NULL THEN NOW(3) ELSE NULL END";
            case "mute" -> side + "_muted=1-" + side + "_muted";
            case "delete" -> side + "_deleted_at=NOW(3)";
            default -> throw new BizException(ErrorCode.PARAM_INVALID);
        };
        jdbc.update("UPDATE conversation SET " + change + " WHERE id=?", id);
        if ("delete".equals(action)) {
            jdbc.update("UPDATE private_message SET sender_deleted_at=NOW(3) WHERE conversation_id=? AND sender_id=?", id, user);
            jdbc.update("UPDATE private_message SET receiver_deleted_at=NOW(3),is_read=1 WHERE conversation_id=? AND receiver_id=?", id, user);
        }
        events.publishEvent(new PrivateMessageChanged(number(row.get("user_a_id")), number(row.get("user_b_id")),
                id, null, "dialogue", user));
    }

    private Map<String, Object> requireMessage(Long user, Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT m.* FROM private_message m WHERE m.id=? "
                + "AND (m.sender_id=? OR m.receiver_id=?) AND " + VISIBLE, id, user, user, user);
        if (rows.isEmpty()) throw new BizException(ErrorCode.NOT_FOUND, "消息不存在或已删除");
        return rows.get(0);
    }

    private void publish(Map<String, Object> row, Long id, String kind, Long onlyUser) {
        events.publishEvent(new PrivateMessageChanged(number(row.get("sender_id")), number(row.get("receiver_id")),
                number(row.get("conversation_id")), id, kind, onlyUser));
    }

    private Long findConversation(Long user, Long peer) {
        if (peer == null || peer <= 0 || user.equals(peer)) throw new BizException(ErrorCode.PARAM_INVALID);
        List<Long> ids = jdbc.queryForList("SELECT id FROM conversation WHERE user_a_id=? AND user_b_id=?",
                Long.class, Math.min(user, peer), Math.max(user, peer));
        return ids.isEmpty() ? null : ids.get(0);
    }

    private Map<String, Object> userBrief(Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id,nickname AS userName,avatar_url AS avatarAddress FROM `user` WHERE id=? AND deleted_at IS NULL", id);
        if (rows.isEmpty()) return Map.of("id", id, "userName", "已注销用户", "avatarAddress", "/img/avatar-default.png");
        return rows.get(0);
    }

    private Map<String, Object> messageView(Map<String, Object> row) {
        Map<String, Object> message = new LinkedHashMap<>();
        boolean revoked = row.get("revoked_at") != null;
        int type = ((Number) row.get("message_type")).intValue();
        message.put("id", row.get("id"));
        message.put("senderId", row.get("sender_id"));
        message.put("receiverId", row.get("receiver_id"));
        message.put("selectSign", row.get("conversation_id").toString());
        message.put("content", revoked ? "" : row.get("content"));
        message.put("messageType", type == 4 ? 3 : type == 3 ? 1 : type);
        message.put("status", revoked ? 2 : truth(row.get("is_read")) ? 1 : 0);
        message.put("deleteSign", 0);
        message.put("sendTime", time(row.get("created_at")));
        message.put("videos", null);
        if (type == 4 && !revoked) {
            List<Map<String, Object>> videos = jdbc.queryForList("""
                    SELECT v.id,v.title,v.cover_url AS coverAddress,v.duration_seconds AS videoTime,
                      u.nickname AS userName,COALESCE(s.danmaku_count,0) AS scrollingNumber
                    FROM video v JOIN `user` u ON u.id=v.owner_id LEFT JOIN video_stats s ON s.video_id=v.id
                    WHERE v.id=? AND v.status=1 AND v.deleted_at IS NULL
                    """, row.get("content"));
            if (!videos.isEmpty()) {
                Map<String, Object> video = videos.get(0);
                int seconds = ((Number) video.get("videoTime")).intValue();
                video.put("videoTime", "%02d:%02d".formatted(seconds / 60, seconds % 60));
                message.put("videos", video);
            }
        }
        return message;
    }

    private static String preview(Map<String, Object> row, Long viewer) {
        if (row.get("revoked_at") != null) return viewer.equals(number(row.get("sender_id"))) ? "您撤回了一条消息" : "对方撤回了一条消息";
        return switch (((Number) row.get("message_type")).intValue()) {
            case 2 -> "[图片]";
            case 4 -> "[视频]";
            default -> row.get("content").toString();
        };
    }

    private static int offset(int page) {
        if (page < 1 || page > 10000) throw new BizException(ErrorCode.PARAM_INVALID);
        return (page - 1) * 20;
    }

    private static Long number(Object value) { return ((Number) value).longValue(); }
    private static boolean truth(Object value) { return Boolean.TRUE.equals(value) || value instanceof Number n && n.intValue() != 0; }
    private static String time(Object value) {
        if (value == null) return null;
        LocalDateTime date = value instanceof Timestamp stamp ? stamp.toLocalDateTime() : (LocalDateTime) value;
        return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
