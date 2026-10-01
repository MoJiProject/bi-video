package com.moji.serve;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.dto.WatchMessage;
import com.moji.dto.WatchParticipant;
import com.moji.service.WatchRoomService;
import com.moji.vo.WatchRoom;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class WatchTogetherServer implements WebSocketConfigurer {

    private final ObjectMapper objectMapper;
    private final WatchRoomService watchRoomService;
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public WatchTogetherServer(ObjectMapper objectMapper, WatchRoomService watchRoomService) {
        this.objectMapper = objectMapper;
        this.watchRoomService = watchRoomService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WatchHandler(), "/watch-together/socket").setAllowedOrigins("*");
    }

    private class WatchHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String token = queryParameter(session, "Authorization");
            Object loginId = token == null ? null : StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("登录已失效"));
                return;
            }
            session.getAttributes().put("userId", Integer.valueOf(loginId.toString()));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId == null) return;

            try {
                WatchMessage message = objectMapper.readValue(textMessage.getPayload(), WatchMessage.class);
                if (message.getType() == null || message.getType().isBlank()) {
                    throw new IllegalArgumentException("房间操作类型不能为空");
                }
                switch (message.getType()) {
                    case "join" -> join(session, userId, message);
                    case "leave" -> leave(session, message);
                    case "play", "pause", "seek", "rate" -> playback(session, userId, message);
                    case "grant_admin" -> changeAdmin(session, userId, message, true);
                    case "revoke_admin" -> changeAdmin(session, userId, message, false);
                    case "kick" -> kick(session, userId, message);
                    case "transfer_owner" -> transferOwner(session, userId, message);
                    case "switch_video" -> switchVideo(session, userId, message);
                    case "blacklist" -> changeBlacklist(session, userId, message, true);
                    case "unblacklist" -> changeBlacklist(session, userId, message, false);
                    case "ping" -> send(session, Map.of("type", "pong"));
                    default -> sendError(session, "不支持的房间操作");
                }
            } catch (JsonProcessingException e) {
                sendError(session, "消息格式不正确");
            } catch (IllegalArgumentException | SecurityException e) {
                sendError(session, e.getMessage());
            }
        }

        private void join(WebSocketSession session, Integer userId, WatchMessage message) {
            WatchRoom existingRoom = watchRoomService.findRoom(message.getRoomId());
            if (existingRoom == null) throw new IllegalArgumentException("房间不存在或已过期");
            if (message.getVideoId() != null && !existingRoom.getVideoId().equals(message.getVideoId())) {
                // 房间已切换过视频，让客户端直接跳到新视频再加入。
                send(session, Map.of("type", "video_switched", "room", existingRoom));
                return;
            }

            String currentRoomId = (String) session.getAttributes().get("roomId");
            if (currentRoomId != null && !currentRoomId.equals(message.getRoomId())) {
                leaveCurrentRoom(session, true);
            }

            WatchRoom room;
            try {
                room = watchRoomService.join(message.getRoomId(), userId);
            } catch (SecurityException e) {
                send(session, Map.of("type", "join_denied", "message", e.getMessage() == null ? "无法加入房间" : e.getMessage()));
                return;
            }
            session.getAttributes().put("roomId", room.getRoomId());
            roomSessions.compute(room.getRoomId(), (ignored, sessions) -> {
                Set<WebSocketSession> currentSessions = sessions == null
                        ? ConcurrentHashMap.newKeySet()
                        : sessions;
                currentSessions.add(session);
                return currentSessions;
            });
            broadcast(room.getRoomId(), Map.of("type", "state", "room", room));
        }

        private void playback(WebSocketSession session, Integer userId, WatchMessage message) {
            String roomId = requireJoinedRoom(session, message.getRoomId());
            if ("play".equals(message.getType())) message.setPaused(false);
            else if ("pause".equals(message.getType())) message.setPaused(true);
            else message.setPaused(null);
            WatchRoom room = watchRoomService.updatePlayback(roomId, userId, message);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", message.getType());
            payload.put("actorUserId", userId);
            payload.put("currentTime", room.getCurrentTime());
            payload.put("playbackRate", room.getPlaybackRate());
            payload.put("paused", room.isPaused());
            broadcastExcept(roomId, payload, session);
        }

        private void leave(WebSocketSession session, WatchMessage message) {
            requireJoinedRoom(session, message.getRoomId());
            leaveCurrentRoom(session, true);
            send(session, Map.of("type", "left"));
        }

        private void changeAdmin(WebSocketSession session, Integer userId, WatchMessage message, boolean grant) {
            String roomId = requireJoinedRoom(session, message.getRoomId());
            WatchRoom room = watchRoomService.setAdmin(roomId, userId, message.getTargetUserId(), grant);
            broadcast(roomId, Map.of("type", "state", "room", room));
        }

        private void kick(WebSocketSession session, Integer userId, WatchMessage message) {
            String roomId = requireJoinedRoom(session, message.getRoomId());
            WatchRoom room = watchRoomService.kick(roomId, userId, message.getTargetUserId());

            for (WebSocketSession targetSession : Set.copyOf(roomSessions.getOrDefault(roomId, Set.of()))) {
                if (message.getTargetUserId().equals(targetSession.getAttributes().get("userId"))) {
                    targetSession.getAttributes().put("kicked", true);
                    send(targetSession, Map.of("type", "kicked", "roomId", roomId));
                    close(targetSession, CloseStatus.POLICY_VIOLATION.withReason("已被移出房间"));
                }
            }
            broadcast(roomId, Map.of("type", "state", "room", room));
        }

        private void transferOwner(WebSocketSession session, Integer userId, WatchMessage message) {
            String roomId = requireJoinedRoom(session, message.getRoomId());
            WatchRoom room = watchRoomService.transferOwner(roomId, userId, message.getTargetUserId());
            send(session, Map.of("type", "owner_transferred", "ownerName", room.getOwnerName()));
            broadcast(roomId, Map.of("type", "state", "room", room));
        }

        private void switchVideo(WebSocketSession session, Integer userId, WatchMessage message) {
            String roomId = requireJoinedRoom(session, message.getRoomId());
            WatchRoom room = watchRoomService.switchVideo(roomId, userId, message.getVideoId());
            // 需要包含操作者本人，所有端都要跳转到新视频。
            broadcast(roomId, Map.of("type", "video_switched", "room", room));
        }

        private void changeBlacklist(WebSocketSession session, Integer userId, WatchMessage message, boolean blocked) {
            String roomId = requireJoinedRoom(session, message.getRoomId());
            WatchRoom room = watchRoomService.setBlacklist(roomId, userId, message.getTargetUserId(), blocked);

            if (blocked) {
                for (WebSocketSession targetSession : Set.copyOf(roomSessions.getOrDefault(roomId, Set.of()))) {
                    if (message.getTargetUserId().equals(targetSession.getAttributes().get("userId"))) {
                        targetSession.getAttributes().put("kicked", true);
                        send(targetSession, Map.of("type", "blacklisted", "roomId", roomId));
                        close(targetSession, CloseStatus.POLICY_VIOLATION.withReason("已被拉黑"));
                    }
                }
            }
            broadcast(roomId, Map.of("type", "state", "room", room));
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            leaveCurrentRoom(session, true);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            close(session, CloseStatus.SERVER_ERROR);
        }

        private void leaveCurrentRoom(WebSocketSession session, boolean notify) {
            String roomId = (String) session.getAttributes().remove("roomId");
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (roomId == null || userId == null) return;

            roomSessions.computeIfPresent(roomId, (ignored, sessions) -> {
                sessions.remove(session);
                return sessions.isEmpty() ? null : sessions;
            });

            boolean hasAnotherSession = roomSessions.getOrDefault(roomId, Set.of()).stream()
                    .anyMatch(other -> userId.equals(other.getAttributes().get("userId")) && other.isOpen());
            if (hasAnotherSession || Boolean.TRUE.equals(session.getAttributes().get("kicked"))) return;

            WatchRoom room = watchRoomService.leave(roomId, userId);
            if (notify && room != null) {
                String userName = room.getParticipants().stream()
                        .filter(participant -> participant.getUserId().equals(userId))
                        .map(WatchParticipant::getUserName)
                        .findFirst()
                        .orElse("成员");
                broadcast(roomId, Map.of(
                        "type", "user_left",
                        "userId", userId,
                        "userName", userName,
                        "room", room
                ));
            }
        }

        private String requireJoinedRoom(WebSocketSession session, String requestedRoomId) {
            String roomId = (String) session.getAttributes().get("roomId");
            if (roomId == null || !roomId.equals(requestedRoomId)) {
                throw new SecurityException("尚未加入该房间");
            }
            return roomId;
        }

        private void broadcast(String roomId, Object payload) {
            broadcastExcept(roomId, payload, null);
        }

        private void broadcastExcept(String roomId, Object payload, WebSocketSession excluded) {
            for (WebSocketSession target : Set.copyOf(roomSessions.getOrDefault(roomId, Set.of()))) {
                if (target != excluded) send(target, payload);
            }
        }

        private void sendError(WebSocketSession session, String message) {
            send(session, Map.of("type", "error", "message", message == null ? "操作失败" : message));
        }

        private void send(WebSocketSession session, Object payload) {
            if (!session.isOpen()) return;
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                }
            } catch (Exception e) {
                close(session, CloseStatus.SERVER_ERROR);
            }
        }

        private void close(WebSocketSession session, CloseStatus status) {
            try {
                if (session.isOpen()) session.close(status);
            } catch (Exception ignored) {
            }
        }

        private String queryParameter(WebSocketSession session, String name) {
            String query = session.getUri() == null ? null : session.getUri().getRawQuery();
            if (query == null) return null;
            for (String part : query.split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2 && pair[0].equals(name)) {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
            return null;
        }
    }
}
