package com.demo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * Integration Tests — UserController
 *   GET /api/users/me
 * ============================================================
 * الـ endpoint الوحيد في UserController — يرجع بيانات
 * الـ Authenticated User الحالي من الـ @AuthenticationPrincipal.
 */
@DisplayName("UserController Integration Tests")
class UserControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("GET /api/users/me — getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("Happy Path: يرجع 200 مع بيانات اليوزر الحالي الصحيحة")
        void getCurrentUser_shouldReturn200WithUserData_whenAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USER_USERNAME))
                    .andExpect(jsonPath("$.email").value(USER_EMAIL))
                    // التأكد إن الـ password مش محصلش تسريب في الـ Response
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("Happy Path: الأدمن بيشوف بياناته هو مش بيانات حد تاني")
        void getCurrentUser_shouldReturnAdminData_whenAuthenticatedAsAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(ADMIN_USERNAME))
                    .andExpect(jsonPath("$.email").value(ADMIN_EMAIL));
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized بدون Token")
        void getCurrentUser_shouldReturn401_whenNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Security: يرجع 401 لو الـ Token غلط أو منتهي")
        void getCurrentUser_shouldReturn401_whenTokenIsInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer this.is.an.invalid.token"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
