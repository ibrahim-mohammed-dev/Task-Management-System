package com.demo.controller;

import com.demo.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * Integration Tests — AdminController
 *   GET    /api/admin/users
 *   GET    /api/admin/tasks
 *   DELETE /api/admin/tasks/{id}
 * ============================================================
 *
 * محور الـ Tests هنا هو:
 *   1. الأدمن (الذي يملك صلاحيات ADMINS) يقدر يعمل كل حاجة ✅
 *   2. اليوزر العادي يُمنع بـ 403 Forbidden من كل الـ Admin Endpoints 🚫
 *   3. أي طلب بدون Token يُمنع بـ 401 Unauthorized 🔒
 */
@DisplayName("AdminController Integration Tests")
class AdminControllerTest extends BaseIntegrationTest {

    private Task task1;
    private Task task2;

    @BeforeEach
    void seedTasks() {
        // Arrange مشترك: نهيّئ بعض التاسكات في H2
        task1 = new Task();
        task1.setTitle("User Task");
        task1.setUser(savedUser);
        task1 = taskRepository.save(task1);

        task2 = new Task();
        task2.setTitle("Admin Task");
        task2.setUser(savedAdmin);
        task2 = taskRepository.save(task2);
    }

    // ================================================================
    // GET /api/admin/users
    // ================================================================
    @Nested
    @DisplayName("GET /api/admin/users — getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("Happy Path: الأدمن يجيب قائمة بكل اليوزرز ويرجع 200")
        void getAllUsers_shouldReturn200WithAllUsers_whenAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2)))); // user + admin
        }

        @Test
        @DisplayName("Security: اليوزر العادي يُمنع ويرجع 403 Forbidden")
        void getAllUsers_shouldReturn403_whenNormalUser() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized بدون Token")
        void getAllUsers_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // GET /api/admin/tasks
    // ================================================================
    @Nested
    @DisplayName("GET /api/admin/tasks — getAllTasks")
    class GetAllTasks {

        @Test
        @DisplayName("Happy Path: الأدمن يشوف كل المهام لجميع اليوزرز ويرجع 200")
        void getAllTasks_shouldReturnAllTasks_whenAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/admin/tasks")
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    // التحقق إن مهام كل اليوزرز موجودة (مش محجوبة بالـ Ownership)
                    .andExpect(jsonPath("$.content[*].title",
                            containsInAnyOrder("User Task", "Admin Task")));
        }

        @Test
        @DisplayName("Security: اليوزر العادي يُمنع ويرجع 403 Forbidden")
        void getAllTasks_shouldReturn403_whenNormalUser() throws Exception {
            mockMvc.perform(get("/api/admin/tasks")
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized بدون Token")
        void getAllTasks_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/tasks"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // DELETE /api/admin/tasks/{id}
    // ================================================================
    @Nested
    @DisplayName("DELETE /api/admin/tasks/{id} — deleteTask (Admin)")
    class DeleteTaskAsAdmin {

        @Test
        @DisplayName("Happy Path: الأدمن يحذف أي تاسك ويرجع 204 — حتى لو مش ملكه")
        void deleteTask_shouldReturn204_whenAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/admin/tasks/{id}", task1.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNoContent());

            // Assert DB — اتمسحت فعلاً
            assertThat(taskRepository.findById(task1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Not Found: الأدمن يحاول يحذف ID مش موجود — يرجع 404")
        void deleteTask_shouldReturn404_whenIdDoesNotExist() throws Exception {
            mockMvc.perform(delete("/api/admin/tasks/{id}", 99999L)
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Security: اليوزر العادي يُمنع ويرجع 403 — حتى لو بيحاول يحذف تاسكته")
        void deleteTask_shouldReturn403_whenNormalUser() throws Exception {
            // ملاحظة: هذا الـ Endpoint خاص بصلاحية DELETE_ANY_TASK — اليوزر العادي يُمنع
            mockMvc.perform(delete("/api/admin/tasks/{id}", task1.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isForbidden());

            // Assert DB — التاسك لسه موجودة (ما اتحذفتش)
            assertThat(taskRepository.findById(task1.getId())).isPresent();
        }
    }
}