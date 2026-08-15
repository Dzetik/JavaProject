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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-must-be-at-least-32-characters-long"
})
@AutoConfigureMockMvc
class UserIntegrationTest {
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
    void me_shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Me User",
                                    "email": "me@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "me@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken =
                JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Me User"))
                .andExpect(jsonPath("$.email").value("me@test.com"));
    }

    @Test
    void me_shouldRejectUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/user/me")).andExpect(status().isForbidden());
    }

    @Test
    void updateMe_shouldUpdateUser() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Old Name",
                                    "email": "update@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "update@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken =
                JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(patch("/user/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "New Name",
                                    "email": "new@test.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void changePassword_shouldChangePassword() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Password User",
                                    "email": "password@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "password@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken =
                JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(post("/user/me/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "currentPassword": "123456",
                                    "newPassword": "654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Пароль изменён"));
    }

    @Test
    void adminEndpoint_shouldRejectUserWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Regular User",
                                    "email": "role@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "role@test.com",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken =
                JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(get("/user/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }
}