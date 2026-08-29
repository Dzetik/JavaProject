package com.example.demo.security;

import com.example.demo.service.WebSocketSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.CloseStatus;

@Component
@RequiredArgsConstructor
public class WebSocketSessionHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    private final WebSocketSessionService webSocketSessionService;

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                webSocketSessionService.addSession(session);
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                webSocketSessionService.removeSession(session.getId());
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }
}