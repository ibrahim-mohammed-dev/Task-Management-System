package com.demo.controller;

import com.demo.dto.RefreshTokenRequestDto;
import com.demo.dto.RegisterRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * Integration Tests — AuthController
 *   POST /api/auth/register
 *   POST /api/auth/login
 *   POST /api/auth/refresh
 *   POST /api/auth/logout
 * ============================================================
 */
@DisplayName("AuthController Integration Tests")
class AuthControllerTest extends BaseIntegrationTest {

    // ================================================================
    // POST /api/auth/register
    // ================================================================
    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("Happy Path: يسجّل مستخدم جديد ويرجع 201 Created")
        void register_shouldReturn201_whenDataIsValid() throws Exception {
            // Arrange
            RegisterRequestDto dto = new RegisterRequestDto("newuser", "newuser@demo.com", "Str0ng!Pass");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("User registered successfully!"));

            // Assert DB
            assertThat(userRepository.existsByUsername("newuser")).isTrue();
        }

        @Test
        @DisplayName("Business Rule: يرجع 400/409 لو اليوزرنيم مكرر")
        void register_shouldReturnError_whenUsernameAlreadyTaken() throws Exception {
            RegisterRequestDto dto = new RegisterRequestDto(USER_USERNAME, "other@demo.com", "Str0ng!Pass");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Business Rule: يرجع 400/409 لو الايميل مكرر")
        void register_shouldReturnError_whenEmailAlreadyRegistered() throws Exception {
            RegisterRequestDto dto = new RegisterRequestDto("brandnewuser", USER_EMAIL, "Str0ng!Pass");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ================================================================
    // POST /api/auth/login
    // ================================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("Happy Path: يرجع AuthResponseDto يحتوي على JWT و Refresh Token")
        void login_shouldReturnAuthResponseDto_whenCredentialsAreValid() throws Exception {
            Map<String, String> loginBody = Map.of(
                    "username", USER_USERNAME,
                    "password", USER_PASSWORD
            );

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.type").value("Bearer"));
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized لبيانات دخول غلط")
        void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
            Map<String, String> loginBody = Map.of(
                    "username", USER_USERNAME,
                    "password", "WrongPassword!"
            );

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // POST /api/auth/refresh
    // ================================================================
    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("Happy Path: يقوم بتدوير التوكين ويرجع AuthResponseDto جديد")
        void refresh_shouldRotateToken_whenValidRefreshToken() throws Exception {
            // 1. Login to obtain raw refresh token
            Map<String, String> loginBody = Map.of(
                    "username", USER_USERNAME,
                    "password", USER_PASSWORD
            );

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isOk())
                    .andReturn();

            String rawRefreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                    .get("refreshToken").asText();

            // 2. Refresh token request
            RefreshTokenRequestDto refreshDto = new RefreshTokenRequestDto(rawRefreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("Security: يرجع 403 Forbidden لو التوكين غير صحيح")
        void refresh_shouldReturn403_whenInvalidRefreshToken() throws Exception {
            RefreshTokenRequestDto refreshDto = new RefreshTokenRequestDto("invalid-refresh-token-uuid");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshDto)))
                    .andExpect(status().isForbidden());
        }
    }

    // ================================================================
    // POST /api/auth/logout
    // ================================================================
    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("Happy Path: يلغي جلسة المستخدم ويرجع 200 OK")
        void logout_shouldRevokeUserTokens_whenAuthenticated() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logged out successfully!"));
        }
    }
}