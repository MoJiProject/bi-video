package com.moji.serve;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.mapper.PrivateMessageMapper;
import com.moji.po.PrivateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class ChatServer implements WebSocketConfigurer {

    //标识 integer是userId
    private static final Map<WebSocketSession, Integer> sessions = new ConcurrentHashMap<>();

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(new CustomWebSocketHandler(), "/chat").setAllowedOrigins("*");

    }

    private class CustomWebSocketHandler extends TextWebSocketHandler {


        @Override
        public void afterConnectionEstablished(WebSocketSession session) {


        }

        /**
         * 获取接收的标识返回新增的消息
         *
         * @param session
         * @param message
         * @throws Exception
         */
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

            String messageContent = message.getPayload();
            String[] split = messageContent.split(":");
            //判断消息处理方式
            if (split.length == 3 && split[0].equals("send")) {
                //查找最新一条
                LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
                privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSenderId, split[1])
                        .eq(PrivateMessage::getReceiverId, split[2])
                        .orderByDesc(PrivateMessage::getSendTime)
                        .last("LIMIT 1");
                PrivateMessage privateMessage = privateMessageMapper.selectOne(privateMessageLambdaQueryWrapper);
                if(privateMessage!=null){
                    for (Map.Entry<WebSocketSession, Integer> entry : sessions.entrySet()) {
                        WebSocketSession userSession = entry.getKey();
                        Integer userId=entry.getValue();
                        if (userSession.isOpen() &&
                                (userId.equals(privateMessage.getReceiverId()) || userId.equals(privateMessage.getSenderId())))
                            sendMessage(userSession, objectMapper.writeValueAsString(privateMessage));
                    }
                }
            } else if (split.length == 2 && split[0].equals("open")) {
                sessions.put(session, Integer.valueOf(split[1]));
            } else if (split.length == 2 && split[0].equals("put")) {
                PrivateMessage privateMessage = privateMessageMapper.selectById(split[1]);
                if (privateMessage != null) {
                    for (Map.Entry<WebSocketSession, Integer> entry : sessions.entrySet()) {
                        WebSocketSession userSession = entry.getKey();
                        Integer userId=entry.getValue();
                        if (userSession.isOpen() &&
                                (userId.equals(privateMessage.getReceiverId()) || userId.equals(privateMessage.getSenderId())))
                            sendMessage(userSession, objectMapper.writeValueAsString(privateMessage));
                    }
                }
            }
        }


        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            sessions.remove(session);
        }

        private void sendMessage(WebSocketSession session, String message) {

            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
