package com.example.demo.event;

import com.example.demo.dto.GameSessionResponse;
import com.example.demo.service.GameWebSocketService;
import com.example.demo.service.LobbyWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GameEventListener {
    private final GameWebSocketService gameWebSocketService;
    private final LobbyWebSocketService lobbyWebSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameUpdated(GameUpdatedEvent event) {
        gameWebSocketService.sendEvent(
                event.getType(),
                event.getGameId(),
                event.getData()
        );

        if ("GAME_STARTED".equals(event.getType())) {
            GameSessionResponse game = (GameSessionResponse) event.getData();
            lobbyWebSocketService.sendGameStarted(
                    game.getLobbyId(),
                    event.getGameId(),
                    event.getData()
            );
        }
    }
}