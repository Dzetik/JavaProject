package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameEventResponse {
    private String type;
    private Long gameId;
    private Object data;
}