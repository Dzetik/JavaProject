package com.example.demo.repository;

import com.example.demo.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional<Deck> findByGameSessionId(Long gameSessionId);
}