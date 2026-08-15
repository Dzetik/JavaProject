package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Получить текущего пользователя",
            description = "Возвращает данные пользователя из JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public UserResponse me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return service.getCurrentUser(userId);
    }

    @PatchMapping("/me")
    @Operation(
            summary = "Изменить профиль",
            description = "Изменяет имя и/или email текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "409", description = "Email уже используется")
    })
    public UserResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateUserRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return service.updateCurrentUser(userId, request);
    }

    @PostMapping("/me/password")
    @Operation(
            summary = "Изменить пароль",
            description = "Изменяет пароль текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль изменён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Неверный текущий пароль или требуется авторизация")
    })
    public String changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        service.changePassword(userId, request);
        return "Пароль изменён";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Административный endpoint",
            description = "Доступен только пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Доступ разрешён"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    public String admin() {
        return "Только для администратора";
    }
}