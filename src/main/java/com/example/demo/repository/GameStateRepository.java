package com.example.demo.repository;

import com.example.demo.entity.GameState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameStateRepository extends JpaRepository<GameState, Long> {
    Optional<GameState> findByGameSessionId(Long gameSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT state
        FROM GameState state
        WHERE state.gameSession.id = :gameSessionId
        """)
    Optional<GameState> findByGameSessionIdForUpdate(
            @Param("gameSessionId") Long gameSessionId
    );
}
