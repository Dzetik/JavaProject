package com.example.demo.dto;

import com.example.demo.entity.GameSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GameSessionResponse {
    private Long id;
    private Long lobbyId;
    private List<GameSessionPlayerResponse> players;
    private GameSessionStatus status;
    private LocalDateTime startedAt;
}