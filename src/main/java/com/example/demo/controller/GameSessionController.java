package com.example.demo.controller;

import com.example.demo.dto.GameSessionResponse;
import com.example.demo.entity.GameSessionStatus;
import com.example.demo.service.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GameSessionController {
    private final GameSessionService gameSessionService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить игровую сессию",
            description = "Возвращает информацию об игровой сессии и её игроках"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Игровая сессия найдена"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником игры"),
            @ApiResponse(responseCode = "404", description = "Игровая сессия не найдена")
    })
    public ResponseEntity<GameSessionResponse> getGameSession(
            @Parameter(description = "ID игровой сессии", example = "1")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(gameSessionService.getGameSessionById(id, userId));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Изменить статус игровой сессии",
            description = "Тестовая операция для проверки GAME_UPDATED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус изменён"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником игры"),
            @ApiResponse(responseCode = "404", description = "Игровая сессия не найдена")
    })
    public ResponseEntity<GameSessionResponse> updateStatus(
            @Parameter(description = "ID игровой сессии", example = "1")
            @PathVariable Long id,
            @RequestParam GameSessionStatus status,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(gameSessionService.updateStatus(id, userId, status));
    }

    @PatchMapping("/{id}/finish")
    @Operation(
            summary = "Завершить игровую сессию",
            description = "Завершает игровую сессию и отправляет GAME_FINISHED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Игра завершена"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником игры"),
            @ApiResponse(responseCode = "404", description = "Игровая сессия не найдена"),
            @ApiResponse(responseCode = "409", description = "Игра уже завершена")
    })
    public ResponseEntity<GameSessionResponse> finishGame(
            @Parameter(description = "ID игровой сессии", example = "1")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(gameSessionService.finishGame(id, userId));
    }
}