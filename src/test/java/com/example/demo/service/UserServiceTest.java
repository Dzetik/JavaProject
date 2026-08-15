package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @InjectMocks
    private UserService service;

    @Test
    void register_shouldCreateUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@test.com");
        request.setPassword("123456");

        when(repository.existsByEmail("test@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("hashed-password");

        service.register(request);

        verify(repository).save(argThat(user ->
                user.getName().equals("Test User")
                        && user.getEmail().equals("test@test.com")
                        && user.getPassword().equals("hashed-password")
                        && user.getRole() == Role.USER
        ));
    }

    @Test
    void register_shouldThrowConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@test.com");
        request.setPassword("123456");

        when(repository.existsByEmail("test@test.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.register(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("123456");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashed-password");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(repository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed-password"))
                .thenReturn(true);
        when(jwtService.generateToken(user))
                .thenReturn("access-token");
        when(refreshTokenService.create(user))
                .thenReturn(refreshToken);

        LoginResponse result = service.login(request);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());

        verify(jwtService).generateToken(user);
        verify(refreshTokenService).create(user);
    }

    @Test
    void login_shouldThrowWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong-password");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashed-password");

        when(repository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(request)
        );

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).create(any());
    }

    @Test
    void updateCurrentUser_shouldUpdateNameAndEmail() {
        User user = new User();
        user.setId(1L);
        user.setName("Old Name");
        user.setEmail("old@test.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setEmail("new@test.com");

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));
        when(repository.existsByEmail("new@test.com"))
                .thenReturn(false);

        UserResponse result = service.updateCurrentUser(1L, request);
        assertEquals("New Name", result.name());
        assertEquals("new@test.com", result.email());

        verify(repository).save(user);
    }

    @Test
    void changePassword_shouldChangePassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("old-hash");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                "old-password",
                "old-hash"))
                .thenReturn(true);
        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-hash");

        service.changePassword(1L, request);
        assertEquals("new-hash", user.getPassword());

        verify(passwordEncoder).encode("new-password");
        verify(repository).save(user);
    }
}