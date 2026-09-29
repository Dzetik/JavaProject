package com.example.demo.dto.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GameStateResponse {
    private Long id;
    private Long gameSessionId;
    private long revision;
    private LocalDateTime createdAt;
}
