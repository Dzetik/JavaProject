package com.example.demo.repository;

import com.example.demo.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    // поиск игровой сессии по id лобби
    Optional<GameSession> findByLobbyId(Long lobbyId);
}