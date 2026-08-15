package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/auth")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Создаёт нового пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Регистрация успешна"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    public String register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return "Регистрация успешна";
    }

    @PostMapping("/login")
    @Operation(
            summary = "Аутентификация",
            description = "Проверяет email и пароль и возвращает access и refresh токены"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Обновление токенов",
            description = "Проверяет refresh token и выполняет его rotation"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токены обновлены"),
            @ApiResponse(responseCode = "401", description = "Недействительный refresh token")
    })
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = refreshTokenService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Выход из системы",
            description = "Удаляет refresh token и завершает сессию"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Выход выполнен"),
            @ApiResponse(responseCode = "401", description = "Недействительный refresh token")
    })
    public String logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.logout(request.refreshToken());
        return "Выход выполнен";
    }
}
