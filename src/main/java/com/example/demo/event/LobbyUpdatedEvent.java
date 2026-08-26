package com.example.demo.event;

import com.example.demo.dto.LobbyResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LobbyUpdatedEvent {
    private final String type;
    private final Long lobbyId;
    private final LobbyResponse lobby;
}