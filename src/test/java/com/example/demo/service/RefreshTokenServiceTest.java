package com.example.demo.service;

import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository repository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private RefreshTokenService service;

    @Test
    void create_shouldSaveRefreshToken() {
        User user = new User("Nikita", "nik@mail.com", "125555");
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RefreshToken result = service.create(user);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertNotNull(result.getExpiresAt());

        verify(repository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_shouldRotateRefreshToken() {
        User user = new User("Nikita", "nik@mail.com", "125555");

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUser(user);
        oldToken.setExpiresAt(Instant.now().plusSeconds(3600));

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-token");
        newToken.setUser(user);
        newToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(repository.findByToken("old-token"))
                .thenReturn(Optional.of(oldToken));
        when(repository.save(any(RefreshToken.class)))
                .thenReturn(newToken);
        when(jwtService.generateToken(user))
                .thenReturn("new-access-token");

        LoginResponse result = service.refresh("old-token");
        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-token", result.refreshToken());

        verify(repository).delete(oldToken);
        verify(repository).save(any(RefreshToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    void refresh_shouldThrowWhenTokenNotFound() {
        when(repository.findByToken("invalid-token")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> service.refresh("invalid-token"));
        verify(repository, never()).delete(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void logout_shouldDeleteRefreshToken() {
        service.logout("refresh-token");
        verify(repository).deleteByToken("refresh-token");
    }
}