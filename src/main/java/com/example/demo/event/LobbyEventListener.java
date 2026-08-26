package com.example.demo.event;

import com.example.demo.service.LobbyWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LobbyEventListener {

    private final LobbyWebSocketService lobbyWebSocketService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleLobbyUpdated(
            LobbyUpdatedEvent event
    ) {
        System.out.println(
                "LOBBY EVENT AFTER COMMIT: "
                        + event.getType()
                        + ", lobbyId=" + event.getLobbyId()
        );

        lobbyWebSocketService.sendEvent(
                event.getType(),
                event.getLobbyId(),
                event.getLobby()
        );
    }
}