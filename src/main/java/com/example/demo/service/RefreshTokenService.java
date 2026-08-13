package com.example.demo.service;

import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    public RefreshToken create(User user) {
        RefreshToken refreshToken = new RefreshToken();
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);

        refreshToken.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60L * 60 * 24 * 30));

        return repository.save(refreshToken);
    }

    @Transactional
    public LoginResponse refresh(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new InvalidCredentialsException("Недействительный refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new InvalidCredentialsException(
                    "Refresh token истёк");
        }

        User user = refreshToken.getUser();
        repository.delete(refreshToken);

        RefreshToken newRefreshToken = create(user);
        String newAccessToken = jwtService.generateToken(user);

        return new LoginResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String token) {
        repository.deleteByToken(token);
    }
}