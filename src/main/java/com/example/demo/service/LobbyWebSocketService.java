package com.example.demo.service;

import com.example.demo.dto.GameEventResponse;
import com.example.demo.dto.LobbyEventResponse;
import com.example.demo.dto.LobbyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LobbyWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendEvent(String type, Long lobbyId, LobbyResponse lobby) {
        System.out.println("SENDING WEBSOCKET EVENT TO: /topic/lobbies/" + lobbyId);
        LobbyEventResponse event = new LobbyEventResponse(type, lobby);
        messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyId, event);
    }

    public void sendGameStarted(Long lobbyId, Long gameId, Object data) {
        GameEventResponse event = new GameEventResponse("GAME_STARTED", gameId, data);
        messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyId, event);
    }
}