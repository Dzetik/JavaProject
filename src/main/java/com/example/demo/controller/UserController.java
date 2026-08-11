package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return service.getCurrentUser(userId);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateUserRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return service.updateCurrentUser(userId, request);
    }

    @PostMapping("/me/password")
    public String changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        service.changePassword(userId, request);
        return "Пароль изменён";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "Только для администратора";
    }
}