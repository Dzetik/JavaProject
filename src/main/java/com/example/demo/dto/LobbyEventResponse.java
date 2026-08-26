package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LobbyEventResponse {
    private String type;
    private LobbyResponse lobby;
}