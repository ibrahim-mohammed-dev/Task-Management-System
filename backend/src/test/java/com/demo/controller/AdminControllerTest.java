package com.demo.controller;

import com.demo.dto.RoleRequestDto;
import com.demo.model.Role;
import com.demo.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

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
 *   PUT    /api/admin/users/{id}/role
 * ============================================================
 *
 * محور الـ Tests هنا هو:
 *   1. الأدمن يقدر يعمل كل حاجة ✅
 *   2. اليوزر العادي يُمنع بـ 403 Forbidden من كل الـ Admin Endpoints 🚫
 *   3. أي طلب بدون Token يُمنع بـ 401 Unauthorized 🔒
 *
 * ⚠️ افتراض: RoleRequestDto عندها حقل اسمه "role" من نوع String أو Enum
 *            وـ AdminService.updateUserRole لا تسمح للأدمن يغيّر role نفسه
 *            (Business Rule — لو مش موجودة عندك اشرح ذلك في التعليق)
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
            // ملاحظة: هذا الـ Endpoint خاص بالأدمن فقط — اليوزر يُمنع حتى لو كانت تاسكته
            mockMvc.perform(delete("/api/admin/tasks/{id}", task1.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isForbidden());

            // Assert DB — التاسك لسه موجودة (ما اتحذفتش)
            assertThat(taskRepository.findById(task1.getId())).isPresent();
        }
    }

    // ================================================================
    // PUT /api/admin/users/{id}/role
    // ================================================================
    @Nested
    @DisplayName("PUT /api/admin/users/{id}/role — changeUserRole")
    class ChangeUserRole {

        @Test
        @DisplayName("Happy Path: الأدمن يرفّع يوزر عادي لـ ADMIN ويرجع 200")
        void changeUserRole_shouldReturn200WithUpdatedRole_whenAdmin() throws Exception {
            // Arrange
            RoleRequestDto dto = new RoleRequestDto(Role.ADMIN);

            // Act & Assert
            mockMvc.perform(put("/api/admin/users/{id}/role", savedUser.getId())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            // Assert DB
            assertThat(userRepository.findById(savedUser.getId())
                    .orElseThrow().getRole())
                    .isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Business Rule: الأدمن لا يقدر يغيّر دور نفسه — يرجع 400")
        void changeUserRole_shouldReturn400_whenAdminTriesToChangeOwnRole() throws Exception {
            // Arrange — الأدمن يحاول يغيّر role نفسه
            RoleRequestDto dto = new RoleRequestDto(Role.USER);

            // Act & Assert
            // ⚠️ لو AdminService عندك مش بيتحقق من هذا الـ Business Rule،
            //    ابدأ بإضافة الـ check فيه ثم شغّل الـ Test
            mockMvc.perform(put("/api/admin/users/{id}/role", savedAdmin.getId())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Not Found: يرجع 404 لو الـ User ID مش موجود")
        void changeUserRole_shouldReturn404_whenUserDoesNotExist() throws Exception {
            // Arrange
            RoleRequestDto dto = new RoleRequestDto(Role.ADMIN);

            // Act & Assert
            mockMvc.perform(put("/api/admin/users/{id}/role", 99999L)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Validation: يرجع 400 لو جاء body فاضي أو role قيمة غلط")
        void changeUserRole_shouldReturn400_whenRoleIsInvalid() throws Exception {
            // Arrange — JSON بقيمة role غير صحيحة
            String invalidBody = "{\"role\": \"SUPERUSER\"}";

            // Act & Assert
            mockMvc.perform(put("/api/admin/users/{id}/role", savedUser.getId())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Security: اليوزر العادي يُمنع من تغيير الأدوار ويرجع 403")
        void changeUserRole_shouldReturn403_whenNormalUser() throws Exception {
            // Arrange
            RoleRequestDto dto = new RoleRequestDto(Role.ADMIN);

            // Act & Assert
            mockMvc.perform(put("/api/admin/users/{id}/role", savedUser.getId())
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }
}
