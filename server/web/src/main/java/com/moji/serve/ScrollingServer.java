package com.moji.serve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.po.Scrolling;
import com.moji.service.ScrollingService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.HashSet;
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
    private static final Map<Integer, AtomicInteger> watchNumber = new ConcurrentHashMap<>();

    @Autowired
    private ScrollingService scrollingService;

    @PostConstruct
    public void init() {
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 建议生产环境限制允许的源，而非通配符*
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

                // 1. 获取旧的videoId，处理切换场景
                Integer oldVideoId = (Integer) session.getAttributes().get("videoId");
                if (oldVideoId != null && !oldVideoId.equals(videoId)) {
                    // 旧videoId观看数减一，减到0则移除key
                    watchNumber.computeIfPresent(oldVideoId, (k, count) -> {
                        int newCount = count.decrementAndGet();
                        return newCount > 0 ? count : null;
                    });
                    // 广播旧videoId的观看数变化
                    broadcastToAllSessionsWatch(oldVideoId);
                }

                // 2. 更新当前会话的videoId属性
                session.getAttributes().put("videoId", videoId);
                sessions.put(session, videoId);

                // 3. 处理open操作，新videoId观看数加一
                if ("open".equals(operation)) {
                    watchNumber.computeIfAbsent(videoId, k -> new AtomicInteger(0)).incrementAndGet();
                }

                // 4. 获取滚动数据并广播
                List<Scrolling> scrollings = scrollingService.selectScrollingList(videoId);
                String jsonScrollings = objectMapper.writeValueAsString(scrollings);
                broadcastToAllSessions(jsonScrollings, videoId);

            } catch (NumberFormatException e) {
                sendMessage(session, "无效videoId");
            } catch (Exception e) {
                sendMessage(session, "服务器错误");
            }
        }

        /**
         * 广播滚动消息和当前观看数
         */
        private void broadcastToAllSessions(String scrollMessage, int videoId) {
            int currentWatchNum = watchNumber.getOrDefault(videoId, new AtomicInteger(0)).get();
            String watchNumMsg = String.valueOf(currentWatchNum);

            // 创建快照避免并发修改异常
            Set<Map.Entry<WebSocketSession, Integer>> entries = new HashSet<>(sessions.entrySet());
            for (Map.Entry<WebSocketSession, Integer> entry : entries) {
                WebSocketSession session = entry.getKey();
                Integer sessionVideoId = entry.getValue();

                if (sessionVideoId != null && sessionVideoId.equals(videoId) && session.isOpen()) {
                    sendMessage(session, scrollMessage);
                    sendMessage(session, watchNumMsg);
                }
            }
        }

        /**
         * 仅广播观看数变化
         */
        private void broadcastToAllSessionsWatch(int videoId) {
            int currentWatchNum = watchNumber.getOrDefault(videoId, new AtomicInteger(0)).get();
            String watchNumMsg = String.valueOf(currentWatchNum);

            Set<Map.Entry<WebSocketSession, Integer>> entries = new HashSet<>(sessions.entrySet());
            for (Map.Entry<WebSocketSession, Integer> entry : entries) {
                WebSocketSession session = entry.getKey();
                Integer sessionVideoId = entry.getValue();

                if (sessionVideoId != null && sessionVideoId.equals(videoId) && session.isOpen()) {
                    sendMessage(session, watchNumMsg);
                }
            }
        }

        /**
         * 会话关闭时：先移除会话，再处理观看数，避免广播时包含已关闭会话
         */
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            // 关键：先从sessions中移除，避免后续广播包含该会话
            Integer videoId = sessions.remove(session);
            if (videoId == null) {
                videoId = (Integer) session.getAttributes().get("videoId");
            }

            // 处理观看数
            if (videoId != null) {
                watchNumber.computeIfPresent(videoId, (k, count) -> {
                    int newCount = count.decrementAndGet();
                    return newCount > 0 ? count : null;
                });
                // 广播观看数变化
                broadcastToAllSessionsWatch(videoId);
            }

            // 清理属性
            session.getAttributes().remove("videoId");
        }

        /**
         * 健壮的消息发送方法：处理竞态条件，精准捕获异常
         */
        private void sendMessage(WebSocketSession session, String message) {
            try {
                // 双重检查（非原子，但可减少无效发送）
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                // 精准捕获会话关闭类异常，仅记录trace级别
                String errorMsg = e.getMessage();
                if (errorMsg != null && (errorMsg.contains("transformer has been closed")
                        || errorMsg.contains("Session is closed")
                        || errorMsg.contains("IllegalStateException"))) {
                } else {
                }
                // 确保清理无效会话
                sessions.remove(session);
            }
        }

        /**
         * 会话出错时清理资源
         */
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            sessions.remove(session);
            session.getAttributes().clear();
        }
    }
}
