package com.example.demo.event;

import com.example.demo.service.GameWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GameEventListener {
    private final GameWebSocketService gameWebSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameUpdated(GameUpdatedEvent event) {
        gameWebSocketService.sendEvent(
                event.getType(),
                event.getGameId(),
                event.getData()
        );
    }
}