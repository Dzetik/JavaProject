package com.example.demo.controller;

import com.example.demo.dto.CreateLobbyRequest;
import com.example.demo.dto.LobbyResponse;
import com.example.demo.service.LobbyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lobbies")
@RequiredArgsConstructor
public class LobbyController {
    private final LobbyService lobbyService;

    @GetMapping
    public ResponseEntity<List<LobbyResponse>> getAvailableLobbies() {
        return ResponseEntity.ok(lobbyService.getAvailableLobbies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LobbyResponse> getLobbyById(@PathVariable Long id) {
        return ResponseEntity.ok(lobbyService.getLobbyById(id));
    }

    @PostMapping
    public ResponseEntity<LobbyResponse> createLobby(@Valid @RequestBody CreateLobbyRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lobbyService.createLobby(request, userId));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<LobbyResponse> joinLobby(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(lobbyService.joinLobby(id, userId));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<LobbyResponse> leaveLobby(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(lobbyService.leaveLobby(id, userId));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<LobbyResponse> startGame(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(lobbyService.startGame(id, userId));
    }
}
