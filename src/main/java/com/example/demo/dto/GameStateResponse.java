package com.example.demo.dto;

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
