package com.example.demo.repository;

import com.example.demo.entity.Lobby;
import com.example.demo.entity.LobbyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    List<Lobby> findByStatus(LobbyStatus status);
}