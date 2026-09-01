package com.example.demo.service;

import com.example.demo.dto.GameSessionPlayerResponse;
import com.example.demo.dto.GameSessionResponse;
import com.example.demo.entity.GameSession;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameSessionService {
    private final GameSessionAccessService gameSessionAccessService;

    @Transactional(readOnly = true)
    public GameSessionResponse getGameSessionById(Long gameSessionId, Long userId) {
        GameSession gameSession = gameSessionAccessService.getGameSessionForPlayer(gameSessionId, userId);
        return toResponse(gameSession);
    }

    public GameSessionResponse toResponse(GameSession gameSession) {
        List<GameSessionPlayerResponse> players = gameSession.getPlayers()
                .stream()
                .map(player -> new GameSessionPlayerResponse(
                        player.getId(),
                        player.getName()
                ))
                .toList();

        return new GameSessionResponse(
                gameSession.getId(),
                gameSession.getLobby().getId(),
                players,
                gameSession.getStatus(),
                gameSession.getStartedAt()
        );
    }
}