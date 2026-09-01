package com.example.demo.controller;

import com.example.demo.dto.GameSessionResponse;
import com.example.demo.service.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @ApiResponse(responseCode = "404", description = "Игровая сессия не найдена")
    })
    public ResponseEntity<GameSessionResponse> getGameSession(
            @Parameter(description = "ID игровой сессии", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(gameSessionService.getGameSessionById(id));
    }
}