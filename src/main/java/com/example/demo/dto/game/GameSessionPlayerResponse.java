package com.example.demo.dto.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameSessionPlayerResponse {
    private Long id;
    private String name;
}