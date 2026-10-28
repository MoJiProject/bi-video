package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.interfaces.dto.AdminCommand;
import com.qingmang.interfaces.dto.AdminQuery;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminManagementService {
    private final JdbcTemplate jdbc;
    private final BannerService permissions;
    private final AdminAuditService audit;
    private final PrivateMessageService messages;
    private final TransactionTemplate transaction;
    private final CacheManager caches;
    private final ApplicationEventPublisher events;

    public AdminManagementService(JdbcTemplate jdbc, BannerService permissions, AdminAuditService audit,
                                  PrivateMessageService messages, PlatformTransactionManager transactions,
                                  CacheManager caches, ApplicationEventPublisher events) {
        this.jdbc = jdbc;
        this.permissions = permissions;
        this.audit = audit;
        this.messages = messages;
        this.transaction = new TransactionTemplate(transactions);
        this.caches = caches;
        this.events = events;
    }

    public List<String> categories(Long actor) {
        permissions.requireAdmin(actor);
        return jdbc.queryForList("SELECT DISTINCT name FROM category WHERE is_enabled=1 ORDER BY name", String.class);
    }

    public Map<String, Object> overview(Long actor) {
        permissions.requireAdmin(actor);
        Map<String, Object> result = new LinkedHashMap<>();
        for (String[] item : List.of(new String[]{"user", "user"}, new String[]{"video", "video"},
                new String[]{"comment", "comment"}, new String[]{"dynamic", "user_post"},
                new String[]{"message", "private_message"})) {
            result.put(item[0] + "Number", count("SELECT COUNT(*) FROM `" + item[1] + "` WHERE deleted_at IS NULL"));
        }
        result.put("adminNumber", count("SELECT COUNT(*) FROM `user` WHERE role=1 AND status=1 AND deleted_at IS NULL"));
        result.put("banNumber", count("SELECT COUNT(*) FROM `user` WHERE status=0 AND deleted_at IS NULL"));
        result.put("pendingVideoNumber", count("SELECT COUNT(*) FROM video WHERE status IN(0,2) AND deleted_at IS NULL"));
        result.put("keyWordNumber", count("SELECT COUNT(*) FROM search_keyword"));
        result.put("totalPlayNumber", count("SELECT COALESCE(SUM(s.play_count),0) FROM video_stats s"
                + " JOIN video v ON v.id=s.video_id WHERE v.deleted_at IS NULL"));
        result.put("totalLikeNumber", count("SELECT COALESCE(SUM(s.like_count),0) FROM video_stats s"
                + " JOIN video v ON v.id=s.video_id WHERE v.deleted_at IS NULL"));
        for (String table : List.of("user", "video", "comment")) {
            result.put("today" + Character.toUpperCase(table.charAt(0)) + table.substring(1) + "Number",
                    count("SELECT COUNT(*) FROM `" + table + "` WHERE deleted_at IS NULL AND created_at>=CURRENT_DATE"));
            result.put(table + "Trend", trend(table));
        }
        result.put("hotVideos", rows(videoColumns() + videoFrom()
                + " WHERE v.deleted_at IS NULL AND v.status=1 ORDER BY COALESCE(s.play_count,0) DESC,v.id DESC LIMIT 10"));
        result.put("hotKeyWords", rows("SELECT keyword AS id,keyword AS word,search_count AS count"
                + " FROM search_keyword ORDER BY search_count DESC,keyword LIMIT 10"));
        return result;
    }

    public Map<String, Object> search(Long actor, String module, AdminQuery query) {
        permissions.requireAdmin(actor);
        require(query != null && query.getPageNum() != null && query.getPageNum() >= 1
                && query.getPageNum() <= 100000, "页码不正确");
        String keyword = text(query.getKeyword(), 200);
        Filter f = new Filter();
        return switch (module) {
            case "user" -> {
                choice(query.getType(), -1, 0, 1, 2);
                choice(query.getLoginFlag(), -1, 0, 1);
                f.sql.append(" WHERE u.deleted_at IS NULL");
                f.like(keyword, "(u.username LIKE ? OR u.nickname LIKE ? OR u.phone LIKE ?)", 3);
                if (query.getType() == 0 || query.getType() == 1) f.add(" AND u.role=?", query.getType());
                if (query.getType() == 2) f.sql.append(" AND u.status=0");
                if (query.getLoginFlag() == 1) f.sql.append(" AND u.last_login_at>=NOW()-INTERVAL 7 DAY");
                if (query.getLoginFlag() == 0)
                    f.sql.append(" AND (u.last_login_at IS NULL OR u.last_login_at<NOW()-INTERVAL 7 DAY)");
                yield page("SELECT u.id,u.nickname AS userName,u.username,u.phone,u.avatar_url AS avatarAddress,"
                        + "u.created_at AS createTime,u.last_login_at AS loginDateTime,"
                        + "CASE WHEN u.role=1 THEN 1 ELSE 0 END AS adminFlag,"
                        + "CASE WHEN u.status=0 THEN 1 ELSE 0 END AS banFlag,"
                        + "COALESCE(s.video_count,0) AS videoNumber,COALESCE(s.follower_count,0) AS fansNumber,"
                        + "COALESCE(s.like_received,0) AS likeNumber",
                        " FROM `user` u LEFT JOIN user_stats s ON s.user_id=u.id", f, "u.id DESC", query);
            }
            case "ban" -> {
                f.sql.append(" WHERE 1=1");
                f.like(keyword, "(u.nickname LIKE ? OR b.reason LIKE ?)", 2);
                yield page("SELECT b.id,b.user_id AS userId,u.nickname AS userName,b.reason,b.status,"
                        + "o.nickname AS operatorName,b.banned_at AS banTime,b.released_at AS unbanTime",
                        " FROM user_ban b JOIN `user` u ON u.id=b.user_id"
                                + " LEFT JOIN `user` o ON o.id=b.operator_id", f, "b.id DESC", query);
            }
            case "video" -> searchVideos(query, keyword);
            case "comment" -> {
                choice(query.getType(), -1, 0, 1);
                f.sql.append(" WHERE c.deleted_at IS NULL");
                f.like(keyword, "c.content LIKE ?", 1);
                f.person(query.getUserId(), "c.author_id", "u.nickname");
                f.person(query.getVideoId(), "c.video_id", "v.title");
                if (query.getType() == 0) f.sql.append(" AND c.root_id IS NULL");
                if (query.getType() == 1) f.sql.append(" AND c.root_id IS NOT NULL");
                yield page("SELECT c.id,c.author_id AS userId,u.nickname AS userName,u.avatar_url AS userAvatar,"
                        + "c.video_id AS videoId,c.post_id AS dynamicId,c.root_id AS mainCommentId,c.content,"
                        + "c.image_urls AS imgAddress,r.nickname AS replyUserName,v.title AS videoTitle,"
                        + "c.like_count AS likeCommentNumber,c.created_at AS commentTime,0 AS deleteSign,"
                        + "CASE WHEN c.status=1 THEN 0 ELSE 1 END AS status",
                        " FROM `comment` c JOIN `user` u ON u.id=c.author_id LEFT JOIN video v ON v.id=c.video_id"
                                + " LEFT JOIN `user` r ON r.id=c.reply_to_user_id", f, "c.id DESC", query);
            }
            case "dynamic" -> {
                choice(query.getType(), -1, 0, 2);
                f.sql.append(" WHERE p.deleted_at IS NULL");
                f.like(keyword, "(p.title LIKE ? OR p.content LIKE ? OR u.nickname LIKE ? OR v.title LIKE ?)", 4);
                f.person(query.getUserId(), "p.author_id", "u.nickname");
                if (query.getType() == 0) f.sql.append(" AND p.video_id IS NOT NULL");
                if (query.getType() == 2) f.sql.append(" AND p.video_id IS NULL");
                yield page("SELECT p.id,p.author_id AS followId,u.nickname AS followUserName,"
                        + "u.avatar_url AS followUserAvatar,p.video_id AS videoId,v.title AS videoTitle,p.title,"
                        + "p.content,p.created_at AS publishTime,p.like_count AS likeNumber,"
                        + "p.comment_count AS commentNumber,CASE WHEN p.status=1 THEN 0 ELSE 1 END AS status,"
                        + "CASE WHEN p.video_id IS NULL THEN 2 ELSE 0 END AS dynamicFlag",
                        " FROM user_post p JOIN `user` u ON u.id=p.author_id LEFT JOIN video v ON v.id=p.video_id",
                        f, "p.id DESC", query);
            }
            case "message" -> {
                f.sql.append(" WHERE m.deleted_at IS NULL");
                f.like(keyword, "m.content LIKE ?", 1);
                f.person(query.getUserId(), "m.sender_id", "s.nickname");
                f.person(query.getReceiverId(), "m.receiver_id", "r.nickname");
                yield page("SELECT m.id,m.sender_id AS senderId,s.nickname AS senderName,s.avatar_url AS senderAvatar,"
                        + "m.receiver_id AS receiverId,r.nickname AS receiverName,r.avatar_url AS receiverAvatar,"
                        + "m.content,m.message_type AS messageType,m.created_at AS sendTime,"
                        + "CASE WHEN m.revoked_at IS NOT NULL THEN 2 WHEN m.is_read=1 THEN 1 ELSE 0 END AS status",
                        " FROM private_message m JOIN `user` s ON s.id=m.sender_id JOIN `user` r ON r.id=m.receiver_id",
                        f, "m.id DESC", query);
            }
            case "keyWord" -> {
                f.sql.append(" WHERE 1=1");
                f.like(text(keyword, 64), "keyword LIKE ?", 1);
                yield page("SELECT keyword AS id,keyword AS word,search_count AS count,last_searched_at AS createTime",
                        " FROM search_keyword", f, "search_count DESC,keyword", query);
            }
            case "log" -> {
                String filter = text(query.getModule(), 32);
                require(filter.isEmpty() || Set.of("user", "video", "comment", "dynamic", "message", "keyWord", "banner")
                        .contains(filter), "日志模块不正确");
                if (query.getSuccess() != null) choice(query.getSuccess(), 0, 1);
                f.sql.append(" WHERE 1=1");
                if (!filter.isEmpty()) f.add(" AND l.module=?", filter);
                if (query.getSuccess() != null) f.add(" AND l.success=?", query.getSuccess());
                yield page("SELECT l.id,l.operator_id AS operatorId,l.operator_name AS operatorName,"
                        + "u.avatar_url AS operatorAvatar,l.module,l.action,l.target_id AS targetId,"
                        + "COALESCE(CONCAT(l.target_type,' #',l.target_id),l.target_type) AS targetName,"
                        + "l.detail,CASE WHEN l.success=1 THEN 1 ELSE 0 END AS success,l.ip,l.created_at AS createTime",
                        " FROM operation_log l LEFT JOIN `user` u ON u.id=l.operator_id", f, "l.id DESC", query);
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID);
        };
    }

    private Map<String, Object> searchVideos(AdminQuery query, String keyword) {
        choice(query.getStatus(), -1, 0, 1, 2);
        choice(query.getSortWay(), 0, 1, 2);
        Filter f = new Filter();
        f.sql.append(" WHERE v.deleted_at IS NULL");
        f.like(keyword, "(v.title LIKE ? OR u.nickname LIKE ?)", 2);
        f.person(query.getUserId(), "v.owner_id", "u.nickname");
        String category = text(query.getSubZoneKey(), 32);
        if (!category.isEmpty()) f.add(" AND c.name=?", category);
        Map<String, Object> numbers = jdbc.queryForMap("SELECT COALESCE(SUM(v.status IN(0,2)),0) AS waitNumber,"
                + "COALESCE(SUM(v.status=1),0) AS passNumber,COALESCE(SUM(v.status=3),0) AS rejectNumber"
                + videoFrom() + f.sql, f.args.toArray());
        if (query.getStatus() == 0) f.sql.append(" AND v.status IN(0,2)");
        if (query.getStatus() == 1) f.sql.append(" AND v.status=1");
        if (query.getStatus() == 2) f.sql.append(" AND v.status=3");
        String order = switch (query.getSortWay()) {
            case 1 -> "COALESCE(s.play_count,0) DESC,v.id DESC";
            case 2 -> "COALESCE(s.like_count,0) DESC,v.id DESC";
            default -> "COALESCE(v.published_at,v.created_at) DESC,v.id DESC";
        };
        Map<String, Object> result = page(videoColumns(), videoFrom(), f, order, query);
        result.putAll(numbers);
        return result;
    }

    public Object write(Long actor, String module, String action, Long target, AdminCommand command,
                        String keyword, String word, Number value, String ip) {
        try {
            Object result = transaction.execute(status -> {
                permissions.requireAdmin(actor);
                Map<String, Object> admin = locked("user", actor);
                requirePermission(number(admin.get("role")) == 1 && number(admin.get("status")) == 1);
                require(command != null, "请求内容不能为空");
                String reason = text(command.getReason(), 255);
                Object response = switch (module) {
                    case "user" -> changeUser(actor, action, target, command, reason);
                    case "video" -> action.equals("deleteVideo") ? deleteVideos(command)
                            : moderateVideo(actor, action, target, reason, ip);
                    case "comment" -> changeComments(command, action.equals("deleteComment"), value);
                    case "dynamic" -> changePosts(command, action.equals("deleteDynamic"), value);
                    case "message" -> action.equals("sendPrivateMessage") ? sendMessage(actor, command)
                            : deleteMessages(command);
                    case "keyWord" -> changeKeyword(action, command, keyword, word, value);
                    default -> throw new BizException(ErrorCode.PARAM_INVALID);
                };
                boolean batch = action.startsWith("delete") || action.startsWith("switch");
                String detail = "对象=" + (target != null ? target : keyword != null ? keyword
                        : batch ? command.getIds() : command.getReceiverId())
                        + (word == null ? "" : "；搜索词=" + word)
                        + (value == null ? "" : "；值=" + value)
                        + (command.getAdminFlag() == null ? "" : "；管理员=" + command.getAdminFlag())
                        + (reason.isEmpty() ? "" : "；原因=" + reason);
                if (batch && !module.equals("keyWord")) {
                    for (Long id : numericIds(command)) audit.success(actor, module, action, id, detail, ip);
                } else {
                    Long auditTarget = action.equals("sendPrivateMessage") && response instanceof Map<?, ?> map
                            && map.get("id") instanceof Number id ? id.longValue() : target;
                    audit.success(actor, module, action, auditTarget, detail, ip);
                }
                if (module.equals("keyWord")) {
                    var cache = caches.getCache("hotKeyword");
                    if (cache != null) cache.clear();
                }
                return response;
            });
            return result;
        } catch (RuntimeException failure) {
            // The write transaction has already rolled back; a rejected operation must survive that rollback.
            String failedTarget = target != null ? target.toString() : keyword != null ? keyword
                    : command != null && command.getIds() != null ? command.getIds().toString() : "";
            try { audit.failure(actor, module, action, target, "对象=" + failedTarget + "；"
                    + (failure instanceof BizException ? failure.getMessage() : "操作失败"), ip); }
            catch (RuntimeException logFailure) { failure.addSuppressed(logFailure); }
            if (failure instanceof DuplicateKeyException)
                throw new BizException(ErrorCode.PARAM_INVALID, "搜索词已存在");
            throw failure;
        }
    }

    private Object changeUser(Long actor, String action, Long target, AdminCommand command, String reason) {
        Map<String, Object> user = locked("user", target);
        requirePermission(!Objects.equals(actor, target));
        if (action.equals("putAdmin")) {
            require(command.getAdminFlag() != null, "请明确指定管理员身份");
            if (command.getAdminFlag()) require(number(user.get("status")) == 1, "已封禁用户不能设为管理员");
            if (!command.getAdminFlag() && number(user.get("role")) == 1) protectLastAdmin();
            jdbc.update("UPDATE `user` SET role=?,updated_at=NOW(3) WHERE id=?", command.getAdminFlag() ? 1 : 0, target);
        } else if (action.equals("banUser")) {
            require(!reason.isEmpty(), "请填写封禁原因");
            require(number(user.get("status")) == 1, "用户已被封禁");
            if (number(user.get("role")) == 1) protectLastAdmin();
            jdbc.update("UPDATE `user` SET status=0,updated_at=NOW(3) WHERE id=?", target);
            jdbc.update("INSERT INTO user_ban(user_id,reason,operator_id) VALUES(?,?,?)", target, reason, actor);
        } else {
            require(number(user.get("status")) == 0, "用户尚未被封禁");
            jdbc.update("UPDATE `user` SET status=1,updated_at=NOW(3) WHERE id=?", target);
            jdbc.update("UPDATE user_ban SET status=0,released_at=NOW(3) WHERE user_id=? AND status=1", target);
        }
        return "操作成功";
    }

    private void protectLastAdmin() {
        List<Long> admins = jdbc.queryForList("SELECT id FROM `user` WHERE role=1 AND status=1"
                + " AND deleted_at IS NULL ORDER BY id FOR UPDATE", Long.class);
        require(admins.size() > 1, "不能移除最后一名有效管理员");
    }

    private Object moderateVideo(Long actor, String action, Long id, String reason, String ip) {
        Map<String, Object> video = locked("video", id);
        int before = number(video.get("status"));
        int next = switch (action) {
            case "examineVideo" -> {
                require(before == 0 || before == 2, "只有待审核视频可以通过审核");
                yield 1;
            }
            case "restoreVideo" -> {
                require(before == 3, "只有已退回或下架的视频可以上架");
                yield 1;
            }
            case "rejectVideo" -> {
                require(before == 0 || before == 2, "只有待审核视频可以退回");
                require(!reason.isEmpty(), "请填写退回原因");
                yield 3;
            }
            case "takeDownVideo" -> {
                require(before == 1, "只有已通过的视频可以下架");
                require(!reason.isEmpty(), "请填写下架原因");
                yield 3;
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID);
        };
        jdbc.update("UPDATE video SET status=?,reject_reason=?,published_at="
                        + (next == 1 ? "NOW(3)" : "published_at") + ",updated_at=NOW(3) WHERE id=?",
                next, next == 1 ? null : reason, id);
        jdbc.update("INSERT INTO video_status_log(video_id,from_status,to_status,reason,operator_id)"
                + " VALUES(?,?,?,?,?)", id, before, next, reason, actor);
        long owner = numberLong(video.get("owner_id"));
        if (next == 1) {
            // Product feeds share a single author post; no per-follower copies are created.
            if (count("SELECT COUNT(*) FROM user_post WHERE video_id=? AND author_id=? AND deleted_at IS NULL", id, owner) == 0) {
                jdbc.update("INSERT INTO user_post(author_id,video_id,content,status) VALUES(?,?,?,1)",
                        owner, id, video.get("title"));
            }
        }
        List<Map<String, Object>> posts = jdbc.queryForList("SELECT id,status,author_id FROM user_post WHERE video_id=?"
                + " AND deleted_at IS NULL ORDER BY id FOR UPDATE", id);
        for (Map<String, Object> post : posts) {
            long postId = numberLong(post.get("id"));
            if (next != 1 && number(post.get("status")) == 1) {
                jdbc.update("UPDATE user_post SET status=2,updated_at=NOW(3) WHERE id=?", postId);
                audit.success(actor, "dynamic", "videoTakeDownPost", postId, "关联视频下架 #" + id, ip);
            } else if (next == 1 && number(post.get("status")) == 2) {
                List<String> lastAction = jdbc.queryForList("SELECT action FROM operation_log"
                                + " WHERE target_type='dynamic' AND target_id=? AND success=1 ORDER BY id DESC LIMIT 1",
                        String.class, postId);
                if (!lastAction.isEmpty() && lastAction.get(0).equals("videoTakeDownPost")) {
                    jdbc.update("UPDATE user_post SET status=1,updated_at=NOW(3) WHERE id=?", postId);
                    audit.success(actor, "dynamic", "videoRestorePost", postId, "关联视频重新上架 #" + id, ip);
                }
            }
        }
        posts.stream().map(post -> numberLong(post.get("author_id")))
                .distinct().filter(author -> author != owner).forEach(this::refreshAuthor);
        refreshAuthor(owner);
        jdbc.update("INSERT INTO notification(receiver_id,actor_id,type,target_type,target_id,content)"
                        + " VALUES(?,?,7,1,?,?)", owner, actor, id,
                next == 1 ? "视频「" + video.get("title") + "」已通过审核"
                        : "视频「" + video.get("title") + "」未通过审核/已下架：" + reason);
        return "操作成功";
    }

    private Object changeComments(AdminCommand command, boolean delete, Number value) {
        List<Long> ids = numericIds(command);
        List<Map<String, Object>> selected = lockedBatch("comment", ids);
        if (delete) {
            Set<Long> all = new LinkedHashSet<>(ids);
            for (Map<String, Object> comment : selected) {
                if (comment.get("root_id") == null) {
                    all.addAll(jdbc.queryForList("SELECT id FROM `comment` WHERE root_id=? ORDER BY id FOR UPDATE",
                            Long.class, comment.get("id")));
                }
            }
            selected = jdbc.queryForList("SELECT * FROM `comment` WHERE id IN(" + marks(all.size()) + ")", all.toArray());
            removeCommentNotifications(all);
            jdbc.update("DELETE FROM `comment` WHERE id IN(" + marks(all.size()) + ")", all.toArray());
        } else {
            int status = offShelf(value) ? 2 : 1;
            jdbc.update("UPDATE `comment` SET status=?,updated_at=NOW(3) WHERE id IN(" + marks(ids.size()) + ")",
                    prepend(status, ids));
        }
        refreshCommentCounts(selected);
        return delete ? Map.of("deleteNumber", selected.size()) : "操作成功";
    }

    private Object changePosts(AdminCommand command, boolean delete, Number value) {
        List<Long> ids = numericIds(command);
        List<Map<String, Object>> selected = lockedBatch("user_post", ids);
        if (delete) {
            deletePostRows(ids);
        } else {
            if (!offShelf(value)) {
                for (Map<String, Object> post : selected) {
                    if (post.get("video_id") != null)
                        require(count("SELECT COUNT(*) FROM video WHERE id=? AND status=1 AND deleted_at IS NULL",
                                post.get("video_id")) == 1, "关联视频未上架，不能单独恢复该视频动态");
                }
            }
            jdbc.update("UPDATE user_post SET status=?,updated_at=NOW(3) WHERE id IN(" + marks(ids.size()) + ")",
                    prepend(offShelf(value) ? 2 : 1, ids));
        }
        selected.stream().map(row -> numberLong(row.get("author_id"))).distinct().forEach(this::refreshAuthor);
        return delete ? Map.of("deleteNumber", ids.size()) : "操作成功";
    }

    private void deletePostRows(List<Long> ids) {
        if (ids.isEmpty()) return;
        String in = marks(ids.size());
        List<Long> comments = jdbc.queryForList("SELECT id FROM `comment` WHERE post_id IN(" + in + ") FOR UPDATE",
                Long.class, ids.toArray());
        removeCommentNotifications(comments);
        jdbc.update("DELETE FROM `comment` WHERE post_id IN(" + in + ")", ids.toArray());
        jdbc.update("DELETE FROM notification WHERE target_type=3 AND target_id IN(" + in + ")", ids.toArray());
        jdbc.update("DELETE FROM user_post WHERE id IN(" + in + ")", ids.toArray());
    }

    private Object deleteVideos(AdminCommand command) {
        List<Long> ids = numericIds(command);
        List<Map<String, Object>> selected = lockedBatch("video", ids);
        Set<Long> authors = new LinkedHashSet<>();
        for (Map<String, Object> video : selected) {
            Long id = numberLong(video.get("id"));
            authors.add(numberLong(video.get("owner_id")));
            List<Map<String, Object>> posts = jdbc.queryForList("SELECT id,author_id FROM user_post WHERE video_id=?"
                    + " ORDER BY id FOR UPDATE", id);
            posts.forEach(post -> authors.add(numberLong(post.get("author_id"))));
            deletePostRows(posts.stream().map(post -> numberLong(post.get("id"))).toList());
            List<Long> folders = jdbc.queryForList("SELECT DISTINCT folder_id FROM favorite_item WHERE video_id=?", Long.class, id);
            List<Long> collectors = jdbc.queryForList("SELECT DISTINCT user_id FROM favorite_item WHERE video_id=?", Long.class, id);
            List<Long> comments = jdbc.queryForList("SELECT id FROM `comment` WHERE video_id=?", Long.class, id);
            removeCommentNotifications(comments);
            jdbc.update("DELETE FROM notification WHERE target_type=1 AND target_id=?", id);
            jdbc.update("DELETE FROM danmaku WHERE video_id=?", id);
            jdbc.update("DELETE FROM watch_history WHERE video_id=?", id);
            jdbc.update("UPDATE watch_room SET video_id=NULL,updated_at=NOW(3) WHERE video_id=?", id);
            jdbc.update("DELETE FROM video WHERE id=?", id);
            for (Long folder : folders)
                jdbc.update("UPDATE favorite_folder SET item_count=(SELECT COUNT(*) FROM favorite_item WHERE folder_id=?)"
                        + " WHERE id=?", folder, folder);
            for (Long collector : collectors)
                jdbc.update("UPDATE user_stats SET favorite_count=(SELECT COUNT(*) FROM favorite_item WHERE user_id=?)"
                        + " WHERE user_id=?", collector, collector);
        }
        authors.forEach(this::refreshAuthor);
        return Map.of("deleteNumber", ids.size());
    }

    private Object sendMessage(Long actor, AdminCommand command) {
        require(command.getReceiverId() != null && command.getReceiverId() > 0, "接收人不正确");
        require(Integer.valueOf(1).equals(command.getMessageType()), "后台只支持发送文字私信");
        String content = text(command.getContent(), 2000);
        require(!content.isEmpty(), "私信内容不能为空");
        return messages.send(actor, command.getReceiverId(), 1, content);
    }

    private Object deleteMessages(AdminCommand command) {
        List<Long> ids = numericIds(command);
        // Match the messaging service's conversation-before-message lock order.
        List<Long> conversations = jdbc.queryForList("SELECT DISTINCT conversation_id FROM private_message WHERE id IN("
                + marks(ids.size()) + ") ORDER BY conversation_id", Long.class, ids.toArray());
        for (Long conversation : conversations)
            jdbc.queryForList("SELECT id FROM conversation WHERE id=? FOR UPDATE", conversation);
        List<Map<String, Object>> selected = lockedBatch("private_message", ids);
        jdbc.update("UPDATE private_message SET deleted_at=NOW(3) WHERE id IN(" + marks(ids.size()) + ")", ids.toArray());
        for (Long conversation : conversations) {
            List<Map<String, Object>> last = jdbc.queryForList("SELECT id,created_at FROM private_message"
                    + " WHERE conversation_id=? AND deleted_at IS NULL ORDER BY id DESC LIMIT 1", conversation);
            jdbc.update("UPDATE conversation SET last_message_id=?,last_message_at=? WHERE id=?",
                    last.isEmpty() ? null : last.get(0).get("id"),
                    last.isEmpty() ? null : last.get(0).get("created_at"), conversation);
        }
        for (Map<String, Object> message : selected)
            events.publishEvent(new PrivateMessageChanged(numberLong(message.get("sender_id")),
                    numberLong(message.get("receiver_id")), numberLong(message.get("conversation_id")),
                    numberLong(message.get("id")), "deleted", null));
        return Map.of("deleteNumber", ids.size());
    }

    private Object changeKeyword(String action, AdminCommand command, String key, String word, Number value) {
        if (action.equals("addKeyWord")) {
            word = keyword(word);
            jdbc.update("INSERT INTO search_keyword(keyword,search_count) VALUES(?,0)", word);
        } else if (action.equals("deleteKeyWord")) {
            List<String> ids = stringIds(command);
            for (String id : ids) lockKeyword(id);
            jdbc.update("DELETE FROM search_keyword_daily WHERE keyword IN(" + marks(ids.size()) + ")", ids.toArray());
            jdbc.update("DELETE FROM search_keyword WHERE keyword IN(" + marks(ids.size()) + ")", ids.toArray());
            return Map.of("deleteNumber", ids.size());
        } else {
            key = keyword(key);
            lockKeyword(key);
            if (action.equals("putKeyWord")) {
                word = keyword(word);
                jdbc.update("UPDATE search_keyword SET keyword=? WHERE keyword=?", word, key);
                if (!key.equals(word)) {
                    List<Map<String, Object>> daily = jdbc.queryForList("SELECT stat_date,search_count"
                            + " FROM search_keyword_daily WHERE keyword=? FOR UPDATE", key);
                    jdbc.update("DELETE FROM search_keyword_daily WHERE keyword=?", key);
                    for (Map<String, Object> row : daily)
                        jdbc.update("INSERT INTO search_keyword_daily(stat_date,keyword,search_count) VALUES(?,?,?)"
                                        + " ON DUPLICATE KEY UPDATE search_count=search_keyword_daily.search_count+VALUES(search_count)",
                                row.get("stat_date"), word, row.get("search_count"));
                }
            } else {
                require(value != null && value.longValue() >= 0 && value.longValue() <= 4294967295L, "搜索次数超出范围");
                jdbc.update("UPDATE search_keyword SET search_count=? WHERE keyword=?", value.longValue(), key);
            }
        }
        return "操作成功";
    }

    private void lockKeyword(String key) {
        require(!jdbc.queryForList("SELECT keyword FROM search_keyword WHERE keyword=? FOR UPDATE", key).isEmpty(),
                "搜索词不存在或已被删除");
    }

    private void refreshAuthor(Long author) {
        jdbc.update("UPDATE user_stats SET video_count=(SELECT COUNT(*) FROM video WHERE owner_id=?"
                        + " AND deleted_at IS NULL AND status=1),post_count=(SELECT COUNT(*) FROM user_post"
                        + " WHERE author_id=? AND deleted_at IS NULL AND status=1),"
                        + "like_received=(SELECT COALESCE(SUM(s.like_count),0) FROM video v JOIN video_stats s ON s.video_id=v.id"
                        + " WHERE v.owner_id=? AND v.deleted_at IS NULL),"
                        + "play_total=(SELECT COALESCE(SUM(s.play_count),0) FROM video v JOIN video_stats s ON s.video_id=v.id"
                        + " WHERE v.owner_id=? AND v.deleted_at IS NULL) WHERE user_id=?",
                author, author, author, author, author);
    }

    private void refreshCommentCounts(List<Map<String, Object>> comments) {
        Set<Long> videos = new LinkedHashSet<>(), posts = new LinkedHashSet<>(), roots = new LinkedHashSet<>();
        for (Map<String, Object> comment : comments) {
            if (comment.get("video_id") != null) videos.add(numberLong(comment.get("video_id")));
            if (comment.get("post_id") != null) posts.add(numberLong(comment.get("post_id")));
            roots.add(numberLong(comment.get("root_id") == null ? comment.get("id") : comment.get("root_id")));
        }
        String visible = " FROM `comment` c LEFT JOIN `comment` r ON r.id=c.root_id WHERE c.deleted_at IS NULL"
                + " AND c.status=1 AND (c.root_id IS NULL OR (r.deleted_at IS NULL AND r.status=1))";
        for (Long video : videos)
            jdbc.update("UPDATE video_stats SET comment_count=(SELECT COUNT(*)" + visible + " AND c.video_id=?) WHERE video_id=?",
                    video, video);
        for (Long post : posts)
            jdbc.update("UPDATE user_post SET comment_count=(SELECT COUNT(*)" + visible + " AND c.post_id=?) WHERE id=?", post, post);
        // MySQL cannot select the update target in a scalar subquery; compute reply counts first.
        for (Long root : roots) {
            long count = count("SELECT COUNT(*) FROM `comment` WHERE root_id=? AND deleted_at IS NULL AND status=1", root);
            jdbc.update("UPDATE `comment` SET reply_count=? WHERE id=?", count, root);
        }
    }

    private void removeCommentNotifications(Iterable<Long> ids) {
        List<Long> values = new ArrayList<>();
        ids.forEach(values::add);
        if (!values.isEmpty())
            jdbc.update("DELETE FROM notification WHERE target_type=2 AND target_id IN(" + marks(values.size()) + ")",
                    values.toArray());
    }

    private Map<String, Object> locked(String table, Long id) {
        require(id != null && id > 0, "对象ID不正确");
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM `" + table + "` WHERE id=?"
                + " AND deleted_at IS NULL FOR UPDATE", id);
        if (rows.isEmpty()) throw new BizException(ErrorCode.NOT_FOUND, "对象不存在或已被删除");
        return rows.get(0);
    }

    private List<Map<String, Object>> lockedBatch(String table, List<Long> ids) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM `" + table + "` WHERE id IN("
                + marks(ids.size()) + ") AND deleted_at IS NULL ORDER BY id FOR UPDATE", ids.toArray());
        require(rows.size() == ids.size(), "部分对象不存在或已被删除，请刷新后重试");
        return rows;
    }

    private List<Long> numericIds(AdminCommand command) {
        List<Long> ids = new ArrayList<>();
        for (String value : stringIds(command)) {
            try {
                long id = Long.parseLong(value);
                require(id > 0, "对象ID不正确");
                ids.add(id);
            } catch (NumberFormatException e) { throw new BizException(ErrorCode.PARAM_INVALID, "对象ID不正确"); }
        }
        require(new LinkedHashSet<>(ids).size() == ids.size(), "对象ID不能重复");
        return ids.stream().sorted().toList();
    }

    private List<String> stringIds(AdminCommand command) {
        require(command.getIds() != null && !command.getIds().isEmpty() && command.getIds().size() <= 100,
                "每次请选择1至100个对象");
        List<String> ids = command.getIds().stream().map(id -> text(id, 64)).sorted().toList();
        require(ids.stream().noneMatch(String::isEmpty) && new LinkedHashSet<>(ids).size() == ids.size(),
                "对象ID不能为空或重复");
        return ids;
    }

    private String keyword(String value) {
        String result = text(value, 64);
        require(!result.isEmpty(), "搜索词不能为空");
        return result;
    }

    private boolean offShelf(Number value) {
        require(value != null && (value.intValue() == 0 || value.intValue() == 1), "上下架参数不正确");
        return value.intValue() == 1;
    }

    private Map<String, Object> page(String select, String from, Filter filter, String order, AdminQuery query) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", count("SELECT COUNT(*)" + from + filter.sql, filter.args.toArray()));
        List<Object> args = new ArrayList<>(filter.args);
        args.add(10);
        args.add((query.getPageNum() - 1) * 10);
        result.put("records", rows(select + from + filter.sql + " ORDER BY " + order + " LIMIT ? OFFSET ?", args.toArray()));
        result.put("pageNum", query.getPageNum());
        result.put("pageSize", 10);
        return result;
    }

    private List<Map<String, Object>> trend(String table) {
        LocalDate today = Objects.requireNonNull(jdbc.queryForObject("SELECT CURRENT_DATE", java.sql.Date.class)).toLocalDate();
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbc.queryForList("SELECT DATE(created_at) AS day,COUNT(*) AS count FROM `"
                + table + "` WHERE deleted_at IS NULL AND created_at>=CURRENT_DATE-INTERVAL 6 DAY GROUP BY DATE(created_at)"))
            totals.put(row.get("day").toString(), numberLong(row.get("count")));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            result.add(Map.of("date", date, "number", totals.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<Map<String, Object>> rows(String sql, Object... args) {
        List<Map<String, Object>> result = jdbc.queryForList(sql, args);
        result.forEach(row -> row.replaceAll((key, value) ->
                value instanceof Timestamp || value instanceof java.sql.Date ? value.toString() : value));
        return result;
    }

    private long count(String sql, Object... args) {
        Long result = jdbc.queryForObject(sql, Long.class, args);
        return result == null ? 0 : result;
    }

    private String videoColumns() {
        return "SELECT v.id,v.owner_id AS userId,u.nickname AS userName,u.avatar_url AS userAvatar,v.title,"
                + "v.cover_url AS coverAddress,c.name AS subZoneKey,v.sub_category_name AS subZoneValue,"
                + "v.original_type AS type,CONCAT(CASE WHEN v.duration_seconds<600"
                + " THEN CONCAT('0',FLOOR(v.duration_seconds/60)) ELSE FLOOR(v.duration_seconds/60) END,':',"
                + "LPAD(MOD(v.duration_seconds,60),2,'0')) AS videoTime,"
                + "CASE WHEN v.status=1 THEN 1 WHEN v.status=3 THEN 2 ELSE 0 END AS status,"
                + "v.reject_reason AS examineFiledMessage,COALESCE(v.published_at,v.created_at) AS createTime,"
                + "COALESCE(s.play_count,0) AS playNumber,COALESCE(s.like_count,0) AS likeNumber,"
                + "COALESCE(s.comment_count,0) AS commentNumber,COALESCE(s.favorite_count,0) AS collectNumber";
    }

    private String videoFrom() {
        return " FROM video v JOIN `user` u ON u.id=v.owner_id LEFT JOIN category c ON c.id=v.category_id"
                + " LEFT JOIN video_stats s ON s.video_id=v.id";
    }

    private static String marks(int size) { return String.join(",", java.util.Collections.nCopies(size, "?")); }
    private static Object[] prepend(Object value, List<Long> ids) {
        List<Object> args = new ArrayList<>();
        args.add(value);
        args.addAll(ids);
        return args.toArray();
    }
    private static int number(Object value) { return (int) numberLong(value); }
    private static long numberLong(Object value) {
        return value instanceof Number n ? n.longValue() : Boolean.TRUE.equals(value) ? 1 : 0;
    }
    private static String text(String value, int max) {
        String result = value == null ? "" : value.trim();
        require(result.length() <= max, "参数内容过长");
        return result;
    }
    private static void choice(Integer value, Integer... allowed) {
        require(value != null && List.of(allowed).contains(value), "筛选参数不正确");
    }
    private static void require(boolean valid, String message) {
        if (!valid) throw new BizException(ErrorCode.PARAM_INVALID, message);
    }
    private static void requirePermission(boolean valid) {
        if (!valid) throw new BizException(ErrorCode.NO_PERMISSION);
    }

    private static class Filter {
        final StringBuilder sql = new StringBuilder();
        final List<Object> args = new ArrayList<>();
        void add(String clause, Object value) { sql.append(clause); args.add(value); }
        void like(String value, String clause, int copies) {
            if (value.isEmpty()) return;
            sql.append(" AND ").append(clause);
            for (int i = 0; i < copies; i++) args.add("%" + value + "%");
        }
        void person(String input, String id, String name) {
            String value = text(input, 200);
            if (value.isEmpty()) return;
            String numeric = value.startsWith("BV") ? value.substring(2) : value;
            if (numeric.matches("[0-9]+")) {
                try {
                    long parsed = Long.parseLong(numeric);
                    require(parsed > 0, "筛选ID不正确");
                    add(" AND " + id + "=?", parsed);
                } catch (NumberFormatException e) { throw new BizException(ErrorCode.PARAM_INVALID, "筛选ID不正确"); }
            } else like(value, name + " LIKE ?", 1);
        }
    }
}
