package com.example.demo.dto.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameEventResponse {
    private String type;
    private Long gameId;
    private Object data;
}