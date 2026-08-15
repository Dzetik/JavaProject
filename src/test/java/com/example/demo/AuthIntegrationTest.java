package com.example.demo;

import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-must-be-at-least-32-characters-long"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateUser() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Test User",
                            "email": "integration@test.com",
                            "password": "123456"
                        }
                        """))
                .andExpect(status().isOk());

        var user = userRepository
                .findByEmail("integration@test.com")
                .orElseThrow();

        assertEquals("Test User", user.getName());
        assertEquals("integration@test.com", user.getEmail());
    }

    @Test
    void login_shouldReturnTokens() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Test User",
                            "email": "login@test.com",
                            "password": "123456"
                        }
                        """))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "login@test.com",
                            "password": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("accessToken"));
        assertTrue(response.contains("refreshToken"));

        assertEquals(1, refreshTokenRepository.count());
    }

    @Test
    void refresh_shouldRotateTokens() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Refresh User",
                            "email": "refresh@test.com",
                            "password": "123456"
                        }
                        """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "refresh@test.com",
                            "password": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefreshToken =
                JsonPath.read(loginResponse, "$.refreshToken");

        String refreshResponse = mockMvc.perform(post("/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "refreshToken": "%s"
                        }
                        """.formatted(oldRefreshToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken =
                JsonPath.read(refreshResponse, "$.refreshToken");

        assertNotEquals(oldRefreshToken, newRefreshToken);
        assertEquals(1, refreshTokenRepository.count());
    }

    @Test
    void refresh_shouldRejectOldRefreshToken() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Rotation User",
                        "email": "rotation@test.com",
                        "password": "123456"
                    }
                    """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "rotation@test.com",
                        "password": "123456"
                    }
                    """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefreshToken =
                JsonPath.read(loginResponse, "$.refreshToken");

        // Первый refresh — rotation
        mockMvc.perform(post("/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "refreshToken": "%s"
                    }
                    """.formatted(oldRefreshToken)))
                .andExpect(status().isOk());

        // Старый token больше использовать нельзя
        mockMvc.perform(post("/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "refreshToken": "%s"
                    }
                    """.formatted(oldRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldInvalidateRefreshToken() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Logout User",
                        "email": "logout@test.com",
                        "password": "123456"
                    }
                    """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "logout@test.com",
                        "password": "123456"
                    }
                    """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken =
                JsonPath.read(loginResponse, "$.refreshToken");

        mockMvc.perform(post("/public/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "refreshToken": "%s"
                    }
                    """.formatted(refreshToken)))
                .andExpect(status().isOk());

        assertEquals(0, refreshTokenRepository.count());

        // После logout token больше нельзя использовать
        mockMvc.perform(post("/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "refreshToken": "%s"
                    }
                    """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }
}