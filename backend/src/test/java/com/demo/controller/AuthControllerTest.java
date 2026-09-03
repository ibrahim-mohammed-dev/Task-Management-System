package com.demo.controller;

import com.demo.dto.RegisterRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * Integration Tests — AuthController
 *   POST /api/auth/register
 *   POST /api/auth/login
 * ============================================================
 * الإعدادات موروثة من BaseIntegrationTest:
 *   - H2 in-memory DB (توافقية PostgreSQL)
 *   - @Transactional → Rollback بعد كل test
 *   - testuser / testadmin منشئين مسبقاً في BaseIntegrationTest مع ربطهم بمجموعاتهم وصلاحياتهم
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

            // Assert DB — التأكد إن اليوزر اتحفظ فعلاً في H2
            assertThat(userRepository.existsByUsername("newuser")).isTrue();
        }

        @Test
        @DisplayName("Business Rule: يرجع 400/409 لو اليوزرنيم مكرر")
        void register_shouldReturnError_whenUsernameAlreadyTaken() throws Exception {
            // Arrange — testuser مسجّل بالفعل من BaseIntegrationTest
            RegisterRequestDto dto = new RegisterRequestDto(USER_USERNAME, "other@demo.com", "Str0ng!Pass");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Business Rule: يرجع 400/409 لو الايميل مكرر")
        void register_shouldReturnError_whenEmailAlreadyRegistered() throws Exception {
            // Arrange — testuser@demo.com مسجّل بالفعل من BaseIntegrationTest
            RegisterRequestDto dto = new RegisterRequestDto("brandnewuser", USER_EMAIL, "Str0ng!Pass");

            // Act & Assert
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
        @DisplayName("Happy Path: يرجع JWT Token صحيح لبيانات صحيحة")
        void login_shouldReturnJwtToken_whenCredentialsAreValid() throws Exception {
            // Arrange — بيانات الـ testuser المنشأ في BaseIntegrationTest
            Map<String, String> loginBody = Map.of(
                    "username", USER_USERNAME,
                    "password", USER_PASSWORD
            );

            // Act
            String responseBody = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Assert — Token لازم يكون موجود وغير فاضي
            assertThat(responseBody).isNotBlank();
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized لبيانات دخول غلط")
        void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
            // Arrange
            Map<String, String> loginBody = Map.of(
                    "username", USER_USERNAME,
                    "password", "WrongPassword!"
            );

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized ليوزر غير موجود")
        void login_shouldReturn401_whenUserDoesNotExist() throws Exception {
            // Arrange
            Map<String, String> loginBody = Map.of(
                    "username", "ghost_user",
                    "password", "DoesntMatter1"
            );

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized());
        }
    }
}