package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class VideoShareService {
    private final JdbcTemplate jdbc;
    private final AuthService auth;
    private final ApplicationEventPublisher events;
    private final DailyInteractionExpService dailyInteractionExp;

    public VideoShareService(JdbcTemplate jdbc, AuthService auth, ApplicationEventPublisher events,
                             DailyInteractionExpService dailyInteractionExp) {
        this.jdbc = jdbc; this.auth = auth; this.events = events;
        this.dailyInteractionExp = dailyInteractionExp;
    }

    public Map<String, Object> recipients(Long actor, Long cursorId) {
        auth.requireActiveUser(actor);
        List<Object> args = new ArrayList<>(List.of(actor));
        String cursor = cursorId == null ? "" : " AND u.id<?";
        if (cursorId != null) args.add(cursorId);
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT u.id,u.nickname AS userName,u.avatar_url AS avatarAddress
                FROM user_follow f JOIN `user` u ON u.id=f.followee_id AND u.status=1 AND u.deleted_at IS NULL
                WHERE f.follower_id=? AND EXISTS(SELECT 1 FROM user_follow r
                  WHERE r.follower_id=f.followee_id AND r.followee_id=f.follower_id)
                """ + cursor + " ORDER BY u.id DESC LIMIT 21", args.toArray());
        boolean more = rows.size() > 20;
        List<Map<String, Object>> records = more ? rows.subList(0, 20) : rows;
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("records", records);
        result.put("hasMore", more);
        result.put("cursorId", records.isEmpty() ? null : records.get(records.size() - 1).get("id"));
        return result;
    }

    @Transactional
    public Map<String, Object> share(Long actor, Long videoId, List<Long> recipients, String content) {
        auth.requireActiveUser(actor);
        if (recipients == null || recipients.isEmpty() || recipients.size() > 10
                || recipients.stream().anyMatch(id -> id == null || id <= 0 || id.equals(actor)))
            throw new BizException(ErrorCode.PARAM_INVALID, "请选择 1 至 10 个分享对象");
        List<Long> ids = new ArrayList<>(new LinkedHashSet<>(recipients));
        Long available = jdbc.queryForObject("SELECT COUNT(*) FROM video WHERE id=? AND status=1 AND deleted_at IS NULL",
                Long.class, videoId);
        if (available == null || available == 0) throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>(List.of(actor));
        args.addAll(ids);
        Long valid = jdbc.queryForObject("""
                SELECT COUNT(*) FROM user_follow f JOIN `user` u ON u.id=f.followee_id
                WHERE f.follower_id=? AND u.status=1 AND u.deleted_at IS NULL
                  AND EXISTS(SELECT 1 FROM user_follow r WHERE r.follower_id=f.followee_id AND r.followee_id=f.follower_id)
                  AND f.followee_id IN(
                """ + placeholders + ")", Long.class, args.toArray());
        if (valid == null || valid != ids.size()) throw new BizException(ErrorCode.NO_PERMISSION, "分享对象必须是互相关注的用户");
        for (Long receiver : ids) {
            long low = Math.min(actor, receiver), high = Math.max(actor, receiver);
            jdbc.update("INSERT INTO conversation(user_a_id,user_b_id) VALUES(?,?) "
                    + "ON DUPLICATE KEY UPDATE id=id", low, high);
            Long conversation = jdbc.queryForObject("SELECT id FROM conversation WHERE user_a_id=? AND user_b_id=?",
                    Long.class, low, high);
            long last = insertMessage(conversation, actor, receiver, 4, videoId.toString());
            events.publishEvent(new PrivateMessageChanged(actor, receiver, conversation, last, "created", null));
            if (content != null && !content.isBlank()) {
                last = insertMessage(conversation, actor, receiver, 1, content);
                events.publishEvent(new PrivateMessageChanged(actor, receiver, conversation, last, "created", null));
            }
            jdbc.update("UPDATE conversation SET last_message_id=?,last_message_at=NOW(3) WHERE id=?", last, conversation);
        }
        jdbc.update("INSERT INTO video_stats(video_id,share_count) VALUES(?,?) "
                + "ON DUPLICATE KEY UPDATE share_count=share_count+VALUES(share_count)", videoId, ids.size());
        dailyInteractionExp.awardShare(actor);
        Long count = jdbc.queryForObject("SELECT share_count FROM video_stats WHERE video_id=?", Long.class, videoId);
        return Map.of("shareCount", count, "sentCount", ids.size());
    }

    private long insertMessage(Long conversation, Long actor, Long receiver, int type, String content) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("INSERT INTO private_message(conversation_id,sender_id,"
                    + "receiver_id,message_type,content) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, conversation);
            statement.setLong(2, actor);
            statement.setLong(3, receiver);
            statement.setInt(4, type);
            statement.setString(5, content);
            return statement;
        }, key);
        if (key.getKey() == null) throw new BizException(ErrorCode.DB_ERROR, "保存分享消息失败");
        return key.getKey().longValue();
    }
}
