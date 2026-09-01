package com.example.demo.security;

import com.example.demo.service.GameSessionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameSubscriptionInterceptor implements ChannelInterceptor {
    private static final String GAME_TOPIC_PREFIX = "/topic/game/";
    private final GameSessionAccessService gameSessionAccessService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateGameSubscription(accessor);
        }

        return message;
    }

    private void validateGameSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(GAME_TOPIC_PREFIX)) {
            return;
        }

        Authentication authentication = (Authentication) accessor
                        .getSessionAttributes()
                        .get("USER_AUTHENTICATION");

        if (authentication == null) {
            throw new IllegalArgumentException("WebSocket authentication is required");
        }

        try {
        accessor.setUser(authentication);
        
        Long gameId = extractGameId(destination);
        Long userId = (Long) authentication.getPrincipal();

        gameSessionAccessService.getGameSessionForPlayer(gameId, userId);

        }
        catch (Exception e) {
            System.out.println(
                    "GAME SUBSCRIBE FAILED: " +
                            e.getClass().getName() +
                            ": " +
                            e.getMessage()
            );

            e.printStackTrace();

            throw e;
        }
    }

    private Long extractGameId(String destination) {
        String gameId = destination.substring(GAME_TOPIC_PREFIX.length());

        try {
            return Long.valueOf(gameId);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid game id");
        }
    }
}