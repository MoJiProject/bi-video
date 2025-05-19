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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableWebSocket
public class ScrollingServer implements WebSocketConfigurer {

    // 创建 ObjectMapper 实例 转json
    private final ObjectMapper objectMapper = new ObjectMapper();
    //第二个是视频id 根据视频id进行群发到所有网页这个id的弹幕进行更新
    private static final Map<WebSocketSession, Integer> sessions = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ScrollingServer.class);

    private static AtomicInteger[] watchNumber = new AtomicInteger[100000];

    // 静态初始化代码块，初始化数组中的每个元素
    static {
        for (int i = 0; i < watchNumber.length; i++) {
            watchNumber[i] = new AtomicInteger(0);  // 初始化每个 AtomicInteger 对象
        }
    }

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
                String[] split = payload.split(":");
                Integer videoId= Integer.valueOf(split[0]);
                String open= split[1];
                if (split[0].isEmpty()) {
                    return;
                }
                sessions.put(session,videoId);
                if(open.equals("open"))
                    watchNumber[sessions.get(session)].incrementAndGet();
                // 查询逻辑
                List<Scrolling> scrollings = scrollingService.selectScrollingList(videoId);

                String jsonScrollings = objectMapper.writeValueAsString(scrollings);
                broadcastToAllSessions(jsonScrollings,videoId);
            } catch (NumberFormatException e) {
                sendMessage(session, "Error: Invalid videoId format");
            }
        }

        /**
         * 群发 更新弹幕和观看人数（发送弹幕和初始化时调用）
         * @param message
         * @param vid
         */
        private void broadcastToAllSessions(String message,int vid) {
            for (Map.Entry<WebSocketSession, Integer> entry : sessions.entrySet()) {
                WebSocketSession session = entry.getKey();
                Integer videoId = entry.getValue();
                // 发送消息
                if (videoId!=null&&session.isOpen()&&videoId==vid) {  // 检查 session 是否仍然打开
                    sendMessage(session, message);
                    sendMessage(session, String.valueOf(watchNumber[sessions.get(session)].get()));
                }else {
                    sessions.remove(session);
                }
            }
        }

        /**
         * 群发 更新观看人数(网页关闭时调用)
         * @param sessionN 关闭的websocket
         * @param vid
         */
        private void broadcastToAllSessionsWatch(WebSocketSession sessionN,int vid) {
            for (Map.Entry<WebSocketSession, Integer> entry : sessions.entrySet()) {
                WebSocketSession session = entry.getKey();
                int videoId = entry.getValue();
                // 发送消息
                if (session.isOpen()&&videoId==vid && session!=sessionN) {  // 检查 session 是否仍然打开
                    sendMessage(session, String.valueOf(watchNumber[vid].get()));
                }else {
                    sessions.remove(session);
                }
            }
        }


        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            Integer videoId=sessions.get(session);
            if(videoId==null)
                return;
            watchNumber[videoId].decrementAndGet();
            broadcastToAllSessionsWatch(session,videoId);
            sessions.remove(session);

        }

        private void sendMessage(WebSocketSession session, String message) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Error sending message", e);
            }
        }
    }
}
