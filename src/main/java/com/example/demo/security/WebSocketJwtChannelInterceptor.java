package com.example.demo.security;

import com.example.demo.service.JwtService;
import com.example.demo.service.WebSocketSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketJwtChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final WebSocketSessionService webSocketSessionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new IllegalArgumentException("JWT token is required");
            }

            String token = authorization.substring(7);
            if (!jwtService.isValid(token)) {
                throw new IllegalArgumentException("Invalid JWT token");
            }

            Long userId = jwtService.extractUserId(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);

            // Сохранение Authentication в attributes STOMP-сессии
            accessor.getSessionAttributes().put("USER_AUTHENTICATION", authentication);

            String sessionId = accessor.getSessionId();
            webSocketSessionService.registerSession(sessionId, jwtService.extractExpiration(token));
        }

        return message;
    }
}