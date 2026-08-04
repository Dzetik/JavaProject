package com.example.demo.controller;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        service.register(request);
        return "Регистрация успешна";
    }
}
