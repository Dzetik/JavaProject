package com.example.demo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GameUpdatedEvent {
    private final String type;
    private final Long gameId;
    private final Object data;
}