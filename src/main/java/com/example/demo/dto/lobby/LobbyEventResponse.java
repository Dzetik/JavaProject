package com.example.demo.dto.lobby;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LobbyEventResponse {
    private String type;
    private LobbyResponse lobby;
}