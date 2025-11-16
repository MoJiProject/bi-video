package com.moji.serve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.po.Scrolling;
import com.moji.service.ScrollingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableWebSocket
public class ScrollingServer implements WebSocketConfigurer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<WebSocketSession, Integer> sessions = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ScrollingServer.class);
    private static final Map<Integer, AtomicInteger> watchNumber = new ConcurrentHashMap<>();

    @Autowired
    private ScrollingService scrollingService;

    @PostConstruct
    public void init() {
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CustomWebSocketHandler(), "/scrolling").setAllowedOrigins("*");
    }

    private class CustomWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            try {
                String[] split = payload.split(":", 2);
                if (split.length < 2 || split[0].isEmpty()) {
                    sendMessage(session, "格式错误: 'videoId:操作'");
                    return;
                }

                Integer videoId = Integer.valueOf(split[0]);
                String operation = split[1];

                session.getAttributes().put("videoId", videoId);
                sessions.put(session, videoId);

                if ("open".equals(operation)) {
                    watchNumber.computeIfAbsent(videoId, k -> new AtomicInteger(0)).incrementAndGet();
                }

                List<Scrolling> scrollings = scrollingService.selectScrollingList(videoId);
                String jsonScrollings = objectMapper.writeValueAsString(scrollings);
                broadcastToAllSessions(jsonScrollings, videoId);

            } catch (NumberFormatException e) {
                sendMessage(session, "无效videoId");
            } catch (Exception e) {
                log.error("处理消息失败", e);
                sendMessage(session, "服务器错误");
            }
        }

        private void broadcastToAllSessions(String scrollMessage, int videoId) {
            int currentWatchNum = watchNumber.getOrDefault(videoId, new AtomicInteger(0)).get();
            String watchNumMsg = String.valueOf(currentWatchNum);

            // 复制快照避免并发修改异常
            Set<Map.Entry<WebSocketSession, Integer>> entries = Set.copyOf(sessions.entrySet());
            for (Map.Entry<WebSocketSession, Integer> entry : entries) {
                WebSocketSession session = entry.getKey();
                Integer sessionVideoId = entry.getValue();

                if (sessionVideoId != null && sessionVideoId.equals(videoId)) {
                    sendMessage(session, scrollMessage);
                    sendMessage(session, watchNumMsg);
                }
            }
        }

        private void broadcastToAllSessionsWatch(int videoId) {
            int currentWatchNum = watchNumber.getOrDefault(videoId, new AtomicInteger(0)).get();
            String watchNumMsg = String.valueOf(currentWatchNum);

            Set<Map.Entry<WebSocketSession, Integer>> entries = Set.copyOf(sessions.entrySet());
            for (Map.Entry<WebSocketSession, Integer> entry : entries) {
                WebSocketSession session = entry.getKey();
                Integer sessionVideoId = entry.getValue();

                if (sessionVideoId != null && sessionVideoId.equals(videoId)) {
                    sendMessage(session, watchNumMsg);
                }
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

            Integer videoId = (Integer) session.getAttributes().get("videoId");
            if (videoId != null) {
                watchNumber.computeIfPresent(videoId, (k, count) -> {
                    count.decrementAndGet();
                    return count;
                });
                broadcastToAllSessionsWatch(videoId);
            }
            sessions.remove(session); // 强制移除会话
        }

        /**
         * 核心优化：专门捕获会话关闭导致的发送异常
         */
        private void sendMessage(WebSocketSession session, String message) {
            // 快速检查：已关闭则直接清理
            if (!session.isOpen()) {
                sessions.remove(session);
                return;
            }

            try {
                // 尝试发送消息（此处仍可能因并发关闭抛出异常）
                session.sendMessage(new TextMessage(message));
            } catch (IllegalStateException e) {
                // 专门捕获"会话已关闭"的异常，仅记录trace级别日志（避免刷屏）
                sessions.remove(session);
            } catch (IOException e) {
                // 其他IO异常（如网络问题）
                log.error("向会话{}发送消息失败", session.getId(), e);
                sessions.remove(session);
            }
        }
    }
}
