package com.example.demo.service;

import com.example.demo.entity.Deck;
import com.example.demo.entity.GameSession;
import com.example.demo.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeckService {
    private final DeckRepository deckRepository;
    private final DeckCompositionService deckCompositionService;

    @Transactional
    public Deck createDeck(GameSession gameSession) {
        Deck deck = new Deck();
        deck.setGameSession(gameSession);

        Deck savedDeck = deckRepository.save(deck);

        deckCompositionService.populateDeck(savedDeck);

        return savedDeck;
    }
}