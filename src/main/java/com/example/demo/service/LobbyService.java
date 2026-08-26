package com.example.demo.service;

import com.example.demo.dto.CreateLobbyRequest;
import com.example.demo.dto.LobbyPlayerResponse;
import com.example.demo.dto.LobbyResponse;
import com.example.demo.entity.Lobby;
import com.example.demo.entity.LobbyStatus;
import com.example.demo.entity.User;
import com.example.demo.event.LobbyUpdatedEvent;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LobbyRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private final LobbyWebSocketService lobbyWebSocketService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LobbyResponse createLobby(
            CreateLobbyRequest request,
            Long userId
    ) {
        User owner = getUserById(userId);

        Lobby lobby = new Lobby(
                request.getName(),
                owner,
                request.getMaxPlayers()
        );

        Lobby savedLobby = lobbyRepository.save(lobby);

        LobbyResponse response = toLobbyResponse(savedLobby);

        eventPublisher.publishEvent(
                new LobbyUpdatedEvent(
                        "LOBBY_UPDATED",
                        savedLobby.getId(),
                        response
                )
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<LobbyResponse> getAvailableLobbies() {
        return lobbyRepository.findByStatus(LobbyStatus.WAITING)
                .stream()
                .map(this::toLobbyResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LobbyResponse getLobbyById(Long lobbyId) {
        return toLobbyResponse(
                getLobbyEntityById(lobbyId)
        );
    }

    @Transactional
    public LobbyResponse joinLobby(
            Long lobbyId,
            Long userId
    ) {
        Lobby lobby = getLobbyEntityById(lobbyId);
        User user = getUserById(userId);

        if (lobby.getStatus() == LobbyStatus.STARTED) {
            throw new ConflictException(
                    "Игра в этом лобби уже началась"
            );
        }

        boolean alreadyJoined = lobby.getPlayers()
                .stream()
                .anyMatch(player ->
                        player.getId().equals(userId)
                );

        if (alreadyJoined) {
            throw new ConflictException(
                    "Пользователь уже находится в лобби"
            );
        }

        if (lobby.getPlayers().size() >= lobby.getMaxPlayers()) {
            throw new ConflictException(
                    "Лобби заполнено"
            );
        }

        lobby.getPlayers().add(user);

        Lobby savedLobby = lobbyRepository.save(lobby);

        LobbyResponse response = toLobbyResponse(savedLobby);

        eventPublisher.publishEvent(
                new LobbyUpdatedEvent(
                        "LOBBY_UPDATED",
                        savedLobby.getId(),
                        response
                )
        );

        return response;
    }

    @Transactional
    public LobbyResponse leaveLobby(
            Long lobbyId,
            Long userId
    ) {
        Lobby lobby = getLobbyEntityById(lobbyId);

        User player = lobby.getPlayers()
                .stream()
                .filter(user ->
                        user.getId().equals(userId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ConflictException(
                                "Пользователь не находится в этом лобби"
                        )
                );

        if (lobby.getOwner().getId().equals(userId)) {
            throw new ConflictException(
                    "Владелец лобби не может покинуть его"
            );
        }

        lobby.getPlayers().remove(player);

        Lobby savedLobby = lobbyRepository.save(lobby);

        LobbyResponse response = toLobbyResponse(savedLobby);

        eventPublisher.publishEvent(
                new LobbyUpdatedEvent(
                        "LOBBY_UPDATED",
                        savedLobby.getId(),
                        response
                )
        );

        return response;
    }

    @Transactional
    public LobbyResponse startGame(
            Long lobbyId,
            Long userId
    ) {
        Lobby lobby = getLobbyEntityById(lobbyId);

        if (!lobby.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Только владелец лобби может начать игру"
            );
        }

        if (lobby.getStatus() == LobbyStatus.STARTED) {
            throw new ConflictException(
                    "Игра уже запущена"
            );
        }

        lobby.setStatus(LobbyStatus.STARTED);

        Lobby savedLobby = lobbyRepository.save(lobby);

        LobbyResponse response = toLobbyResponse(savedLobby);

        eventPublisher.publishEvent(
                new LobbyUpdatedEvent(
                        "GAME_STARTED",
                        savedLobby.getId(),
                        response
                )
        );

        return response;
    }

    private Lobby getLobbyEntityById(Long lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Лобби с id " + lobbyId + " не найдено"
                        )
                );
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Пользователь с id " + userId + " не найден"
                        )
                );
    }

    private LobbyResponse toLobbyResponse(Lobby lobby) {

        List<LobbyPlayerResponse> players = lobby.getPlayers()
                .stream()
                .map(user -> new LobbyPlayerResponse(
                        user.getId(),
                        user.getName()
                ))
                .toList();

        return new LobbyResponse(
                lobby.getId(),
                lobby.getName(),
                lobby.getOwner().getId(),
                players,
                lobby.getMaxPlayers(),
                lobby.getStatus()
        );
    }
}