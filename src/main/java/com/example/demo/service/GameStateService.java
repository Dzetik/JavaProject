package com.example.demo.service;

import com.example.demo.dto.GameStateResponse;
import com.example.demo.entity.GameSession;
import com.example.demo.entity.GameSessionStatus;
import com.example.demo.entity.GameState;
import com.example.demo.event.GameUpdatedEvent;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.GameStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameStateService {
    private final GameStateRepository gameStateRepository;
    private final GameSessionAccessService gameSessionAccessService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GameState createInitialState(GameSession gameSession) {
        GameState gameState = new GameState();
        gameState.setGameSession(gameSession);
        gameState.setCreatedAt(LocalDateTime.now());

        return gameStateRepository.save(gameState);
    }

    @Transactional(readOnly = true)
    public GameStateResponse getState(Long gameSessionId, Long userId) {
        gameSessionAccessService.getGameSessionForPlayer(gameSessionId, userId);

        GameState gameState = gameStateRepository.findByGameSessionId(gameSessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Состояние игры для gameSession " + gameSessionId + " не найдено")
                );

        return toResponse(gameState);
    }

    @Transactional
    public GameStateResponse updateState(Long gameSessionId, Long userId) {
        GameSession gameSession = gameSessionAccessService.getGameSessionForPlayer(gameSessionId, userId);
        if (gameSession.getStatus() == GameSessionStatus.FINISHED) {
            throw new ConflictException("Нельзя изменять состояние завершённой игры");
        }

        GameState gameState = gameStateRepository
                .findByGameSessionIdForUpdate(gameSessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Состояние игры для gameSession " + gameSessionId + " не найдено")
                );

        gameState.setRevision(gameState.getRevision() + 1);

        GameStateResponse response = toResponse(gameState);
        eventPublisher.publishEvent(
                new GameUpdatedEvent(
                        "GAME_STATE_UPDATED",
                        gameSessionId,
                        response
                )
        );

        return response;
    }

    private GameStateResponse toResponse(GameState gameState) {
        return new GameStateResponse(
                gameState.getId(),
                gameState.getGameSession().getId(),
                gameState.getRevision(),
                gameState.getCreatedAt()
        );
    }
}
