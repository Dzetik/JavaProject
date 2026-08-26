package com.example.demo.dto;

import com.example.demo.entity.LobbyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class LobbyResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private List<LobbyPlayerResponse> players;
    private Integer maxPlayers;
    private LobbyStatus status;
}