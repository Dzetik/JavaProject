package com.example.demo.repository;

import com.example.demo.entity.Lobby;
import com.example.demo.entity.LobbyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    // выдает все лобби по статусу
    List<Lobby> findByStatus(LobbyStatus status);

    // выдает лобби конкретного пользователя
    @Query("SELECT l FROM Lobby l JOIN l.players p WHERE p.id = :userId")
    List<Lobby> findByPlayerId(@Param("userId") Long userId);

    // блокирует возможность подключения двух пользователей в 1 слот
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lobby l WHERE l.id = :id")
    Optional<Lobby> findByIdForUpdate(@Param("id") Long id);
}