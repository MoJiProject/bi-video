package com.qingmang.interfaces;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.application.AuthService;
import com.qingmang.application.PrivateMessageChanged;
import com.qingmang.application.PrivateMessageService;
import com.qingmang.common.exception.BizException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.SessionLimitExceededException;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSocket
public class ChatSocketConfig implements WebSocketConfigurer {
    private static final Logger log = LoggerFactory.getLogger(ChatSocketConfig.class);
    private final Map<Long, Map<String, WebSocketSession>> clients = new ConcurrentHashMap<>();
    private final ObjectMapper json;
    private final AuthService auth;
    private final PrivateMessageService messages;

    public ChatSocketConfig(ObjectMapper json, AuthService auth, PrivateMessageService messages) {
        this.json = json;
        this.auth = auth;
        this.messages = messages;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new Handler(), "/api/chat").setAllowedOriginPatterns("*");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void changed(PrivateMessageChanged event) {
        notifyUser(event.senderId(), event.receiverId(), event);
        if (!event.senderId().equals(event.receiverId())) notifyUser(event.receiverId(), event.senderId(), event);
    }

    private void notifyUser(Long user, Long peer, PrivateMessageChanged event) {
        if (event.onlyUserId() != null && !event.onlyUserId().equals(user)) return;
        Map<String, WebSocketSession> sessions = clients.get(user);
        if (sessions == null) return;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "messages.changed");
        payload.put("kind", event.kind());
        payload.put("peerId", peer);
        payload.put("actorId", event.senderId());
        payload.put("conversationId", event.conversationId());
        payload.put("messageId", event.messageId());
        payload.put("message", event.messageId() == null ? null : messages.messageFor(user, event.messageId()));
        try {
            TextMessage text = new TextMessage(json.writeValueAsString(payload));
            for (WebSocketSession session : sessions.values()) {
                if (authorized(session)) send(session, text);
                else close(session, CloseStatus.POLICY_VIOLATION);
            }
        } catch (JsonProcessingException error) {
            log.error("Cannot encode chat event for conversation {}", event.conversationId(), error);
            sessions.values().forEach(session -> close(session, CloseStatus.SERVER_ERROR));
        }
    }

    private class Handler extends TextWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession raw) throws IOException {
            String token = raw.getUri() == null ? null : UriComponentsBuilder.fromUri(raw.getUri())
                    .build().getQueryParams().getFirst("token");
            Object login = token == null ? null : StpUtil.getLoginIdByToken(token);
            if (login == null) {
                raw.close(CloseStatus.POLICY_VIOLATION.withReason("请先登录"));
                return;
            }
            Long user = Long.valueOf(login.toString());
            try { auth.requireActiveUser(user); }
            catch (BizException error) {
                log.warn("Chat connection rejected for user {}: {}", user, error.getMessage());
                raw.close(CloseStatus.POLICY_VIOLATION.withReason("账号不可用"));
                return;
            }
            raw.getAttributes().put("userId", user);
            raw.getAttributes().put("token", token);
            raw.setTextMessageSizeLimit(128);
            WebSocketSession session = new ConcurrentWebSocketSessionDecorator(raw, 5000, 256 * 1024);
            clients.computeIfAbsent(user, ignored -> new ConcurrentHashMap<>()).put(session.getId(), session);
            send(session, new TextMessage("{\"type\":\"ready\"}"));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage text) {
            if (!authorized(session)) { close(session, CloseStatus.POLICY_VIOLATION); return; }
            if (!"{\"type\":\"ping\"}".equals(text.getPayload())) {
                close(session, CloseStatus.BAD_DATA);
                return;
            }
            Long user = (Long) session.getAttributes().get("userId");
            Map<String, WebSocketSession> sessions = clients.get(user);
            if (sessions != null && sessions.get(session.getId()) != null)
                send(sessions.get(session.getId()), new TextMessage("{\"type\":\"pong\"}"));
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) { detach(session); }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable error) {
            log.warn("Chat transport failed: {}", session.getId(), error);
            close(session, CloseStatus.SERVER_ERROR);
        }
    }

    private boolean authorized(WebSocketSession session) {
        Object login = StpUtil.getLoginIdByToken((String) session.getAttributes().get("token"));
        Long user = (Long) session.getAttributes().get("userId");
        if (login == null || user == null || !user.toString().equals(login.toString())) return false;
        try { auth.requireActiveUser(user); return true; }
        catch (BizException error) { log.warn("Chat authorization expired for user {}: {}", user, error.getMessage()); return false; }
    }

    private void send(WebSocketSession session, TextMessage text) {
        try {
            if (session.isOpen()) session.sendMessage(text);
            else detach(session);
        } catch (IOException | IllegalStateException | SessionLimitExceededException error) {
            log.warn("Chat delivery failed: {}", session.getId(), error);
            close(session, CloseStatus.SESSION_NOT_RELIABLE);
        }
    }

    private void detach(WebSocketSession session) {
        Long user = (Long) session.getAttributes().get("userId");
        if (user != null) clients.computeIfPresent(user, (id, sessions) -> {
            sessions.remove(session.getId());
            return sessions.isEmpty() ? null : sessions;
        });
    }

    private void close(WebSocketSession session, CloseStatus status) {
        detach(session);
        try { session.close(status); }
        catch (IOException | IllegalStateException error) { log.warn("Cannot close chat session: {}", session.getId(), error); }
    }
}
