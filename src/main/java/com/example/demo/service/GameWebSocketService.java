package com.example.demo.service;

import com.example.demo.dto.GameEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendEvent(String type, Long gameId, Object data) {
        GameEventResponse event = new GameEventResponse(type, gameId, data);
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                event
        );
    }
}