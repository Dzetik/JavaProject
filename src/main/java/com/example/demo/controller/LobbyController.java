package com.example.demo.controller;

import com.example.demo.dto.CreateLobbyRequest;
import com.example.demo.dto.LobbyResponse;
import com.example.demo.service.LobbyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lobbies", description = "Управление игровыми лобби")
public class LobbyController {
    private final LobbyService lobbyService;

    @Operation(
            summary = "Получить доступные лобби",
            description = "Возвращает все лобби, ожидающие игроков"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список доступных лобби получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = LobbyResponse.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
    })
    @GetMapping
    public ResponseEntity<List<LobbyResponse>> getAvailableLobbies() {
        return ResponseEntity.ok(lobbyService.getAvailableLobbies());
    }

    @Operation(
            summary = "Получить лобби",
            description = "Возвращает информацию о конкретном лобби"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Лобби найдено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LobbyResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "404", description = "Лобби не найдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LobbyResponse> getLobbyById(
            @Parameter(description = "ID лобби", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(lobbyService.getLobbyById(id));
    }

    @Operation(
            summary = "Создать лобби",
            description = "Создаёт новое лобби. Авторизованный пользователь становится владельцем и первым игроком"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Лобби успешно создано"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
    })
    @PostMapping
    public ResponseEntity<LobbyResponse> createLobby(@Valid @RequestBody CreateLobbyRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lobbyService.createLobby(request, userId));
    }

    @Operation(
            summary = "Войти в лобби",
            description = "Добавляет авторизованного пользователя в лобби"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Лобби или пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Лобби заполнено, игра уже началась или пользователь уже находится в лобби")
    })
    @PostMapping("/{id}/join")
    public ResponseEntity<LobbyResponse> joinLobby(
            @Parameter(description = "ID лобби", example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(lobbyService.joinLobby(id, userId));
    }

    @Operation(
            summary = "Выйти из лобби",
            description = "Удаляет авторизованного пользователя из лобби"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь вышел из лобби",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = LobbyResponse.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "404", description = "Лобби не найдено"),
            @ApiResponse(responseCode = "409", description = "Пользователь не находится в лобби")
    })
    @PostMapping("/{id}/leave")
    public ResponseEntity<LobbyResponse> leaveLobby(
            @Parameter(description = "ID лобби", example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(lobbyService.leaveLobby(id, userId));
    }

    @Operation(
            summary = "Запустить игру",
            description = "Запускает игру. Выполнить операцию может только владелец лобби"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Игра успешно запущена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LobbyResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "403", description = "Только владелец лобби может запустить игру"),
            @ApiResponse(responseCode = "404", description = "Лобби не найдено"),
            @ApiResponse(responseCode = "409", description = "Игра уже запущена или недостаточно игроков")
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<LobbyResponse> startGame(
            @Parameter(description = "ID лобби", example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(lobbyService.startGame(id, userId));
    }
}
