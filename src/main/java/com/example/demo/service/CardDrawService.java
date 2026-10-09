package com.example.demo.service;

import com.example.demo.dto.game.CardDrawResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardDrawService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final GameSessionAccessService gameSessionAccessService;

    @Transactional
    public CardDrawResponse drawCard(Long gameId, Long userId) {
        GameSession gameSession = gameSessionAccessService.getGameSessionForPlayer(gameId, userId);

        Deck deck = deckRepository.findByGameSessionId(gameSession.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Колода для игровой сессии с id " + gameId + " не найдена"
                        )
                );

        Card card = cardRepository.findRandomCardForUpdate(deck.getId(), CardLocation.DECK.name())
                .orElseThrow(() ->
                        new ConflictException("В колоде не осталось карт")
                );

        User player = gameSession.getPlayers().stream()
                .filter(p -> p.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "Пользователь не является участником этой игры"
                ));

        card.setLocation(CardLocation.HAND);
        card.setOwner(player);

        return new CardDrawResponse(
                card.getId(),
                card.getRecipe().getId(),
                card.getLocation()
        );
    }
}
