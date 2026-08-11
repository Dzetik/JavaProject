package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        repository.save(user);
    }

    public String login(LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Неверный email или пароль");
        }

        return jwtService.generateToken(user);
    }

    public UserResponse getCurrentUser(Long userId) {

        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        return UserResponse.from(user);
    }

    public UserResponse updateCurrentUser(Long userId, UpdateUserRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null &&
                !user.getEmail().equals(request.getEmail())) {

            if (repository.existsByEmail(request.getEmail())) {
                throw new ConflictException(
                        "Пользователь с таким email уже существует"
                );
            }

            user.setEmail(request.getEmail());
        }
        repository.save(user);

        return UserResponse.from(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new InvalidCredentialsException("Неверный текущий пароль");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }
}
