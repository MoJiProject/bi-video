package com.qingmang.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.application.DanmakuChanged;
import com.qingmang.domain.video.Video;
import com.qingmang.domain.video.mapper.VideoMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.ServletWebSocketHandlerRegistry;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.SessionLimitExceededException;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSocket
@EnableScheduling
public class VideoLiveSocketConfig implements WebSocketConfigurer {
    private static final Logger log = LoggerFactory.getLogger(VideoLiveSocketConfig.class);
    private final Map<Long, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ObjectMapper json;
    private final VideoMapper videos;

    public VideoLiveSocketConfig(ObjectMapper json, VideoMapper videos) {
        this.json = json;
        this.videos = videos;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (registry instanceof ServletWebSocketHandlerRegistry servletRegistry) servletRegistry.setOrder(-1);
        registry.addHandler(new Handler(), "/api/video/live").setAllowedOriginPatterns("*");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void changed(DanmakuChanged event) {
        Map<String, Object> message = event.danmaku() == null
                ? Map.of("type", "danmaku.deleted", "id", event.removedId(), "count", event.count())
                : Map.of("type", "danmaku.created", "danmaku", event.danmaku(), "count", event.count());
        broadcast(event.videoId(), message);
    }

    private class Handler extends TextWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession raw) throws IOException {
            String value = raw.getUri() == null ? null : UriComponentsBuilder.fromUri(raw.getUri())
                    .build().getQueryParams().getFirst("videoId");
            Long videoId;
            try {
                videoId = Long.valueOf(value == null ? "" : value);
            } catch (NumberFormatException error) {
                raw.close(CloseStatus.BAD_DATA.withReason("视频ID不正确"));
                return;
            }
            Video video = videos.selectById(videoId);
            if (video == null || !Integer.valueOf(1).equals(video.getStatus())) {
                raw.close(CloseStatus.POLICY_VIOLATION.withReason("视频不存在或未发布"));
                return;
            }
            raw.setTextMessageSizeLimit(1024);
            raw.getAttributes().put("videoId", videoId);
            raw.getAttributes().put("lastSeen", System.currentTimeMillis());
            WebSocketSession session = new ConcurrentWebSocketSessionDecorator(raw, 5000, 512 * 1024);
            rooms.compute(videoId, (id, members) -> {
                Map<String, WebSocketSession> current = members == null ? new ConcurrentHashMap<>() : members;
                current.put(session.getId(), session);
                return current;
            });
            broadcastPresence(videoId);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
            if (!"{\"type\":\"ping\"}".equals(message.getPayload())) {
                session.close(CloseStatus.BAD_DATA.withReason("仅支持心跳消息"));
                return;
            }
            session.getAttributes().put("lastSeen", System.currentTimeMillis());
            Long videoId = (Long) session.getAttributes().get("videoId");
            if (videoId == null) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            Map<String, WebSocketSession> members = rooms.get(videoId);
            if (members != null) {
                WebSocketSession decorated = members.get(session.getId());
                if (decorated != null && !send(decorated, new TextMessage("{\"type\":\"pong\"}")))
                    broadcastPresence(videoId);
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            remove(session);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable error) {
            log.warn("Video live transport failed: {}", session.getId(), error);
            close(session);
            remove(session);
        }
    }

    @Scheduled(fixedDelay = 15000)
    public void expireConnections() {
        long cutoff = System.currentTimeMillis() - Duration.ofSeconds(90).toMillis();
        for (Map<String, WebSocketSession> members : rooms.values()) {
            for (WebSocketSession session : members.values()) {
                Long lastSeen = (Long) session.getAttributes().get("lastSeen");
                if (!session.isOpen() || lastSeen == null || lastSeen < cutoff) {
                    close(session);
                    remove(session);
                }
            }
        }
    }

    private void remove(WebSocketSession session) {
        if (detach(session)) broadcastPresence((Long) session.getAttributes().get("videoId"));
    }

    private boolean detach(WebSocketSession session) {
        Long videoId = (Long) session.getAttributes().get("videoId");
        if (videoId == null) return false;
        boolean[] removed = {false};
        rooms.computeIfPresent(videoId, (id, members) -> {
            removed[0] = members.remove(session.getId()) != null;
            return members.isEmpty() ? null : members;
        });
        return removed[0];
    }

    private void broadcastPresence(Long videoId) {
        boolean changed;
        do {
            changed = false;
            Map<String, WebSocketSession> members = rooms.get(videoId);
            if (members == null) return;
            TextMessage payload = encode(videoId, Map.of("type", "presence", "count", members.size()));
            if (payload == null) return;
            for (WebSocketSession session : members.values()) if (!send(session, payload)) changed = true;
        } while (changed);
    }

    private void broadcast(Long videoId, Map<String, Object> message) {
        Map<String, WebSocketSession> members = rooms.get(videoId);
        if (members == null) return;
        TextMessage payload = encode(videoId, message);
        if (payload == null) return;
        boolean changed = false;
        for (WebSocketSession session : members.values()) if (!send(session, payload)) changed = true;
        if (changed) broadcastPresence(videoId);
    }

    private TextMessage encode(Long videoId, Map<String, Object> message) {
        try {
            return new TextMessage(json.writeValueAsString(message));
        } catch (com.fasterxml.jackson.core.JsonProcessingException error) {
            log.error("Cannot encode video live message for video {}", videoId, error);
            Map<String, WebSocketSession> members = rooms.remove(videoId);
            if (members != null) members.values().forEach(this::close);
            return null;
        }
    }

    private boolean send(WebSocketSession session, TextMessage payload) {
        try {
            if (!session.isOpen()) { detach(session); return false; }
            session.sendMessage(payload);
            return true;
        } catch (IOException | IllegalStateException | SessionLimitExceededException error) {
            log.warn("Video live delivery failed: {}", session.getId(), error);
            detach(session);
            close(session);
            return false;
        }
    }

    private void close(WebSocketSession session) {
        try { session.close(CloseStatus.SESSION_NOT_RELIABLE); }
        catch (IOException | IllegalStateException error) { log.warn("Cannot close video live session: {}", session.getId(), error); }
    }
}
