package com.demo.controller;

import com.demo.dto.TaskRequestDto;
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
 * Integration Tests — TaskController
 *   POST   /api/tasks
 *   GET    /api/tasks
 *   GET    /api/tasks/{id}
 *   PUT    /api/tasks/{id}
 *   PATCH  /api/tasks/{id}/toggle
 *   DELETE /api/tasks/{id}
 * ============================================================
 * كل الـ tests بتشتغل على H2 (بوضع توافقية PostgreSQL) وبيتراجع فيها الـ Rollback تلقائياً بفضل @Transactional.
 * اليوزر والأدمن والـ Tokens والصلاحيات جاهزون مسبقاً من BaseIntegrationTest.
 */
@DisplayName("TaskController Integration Tests")
class TaskControllerTest extends BaseIntegrationTest {

    private Task existingTask;     // تاسك تخص testuser — تُنشأ قبل كل test

    @BeforeEach
    void seedTask() {
        // Arrange مشترك: ننشئ تاسك تخص اليوزر العادي تُستخدم في معظم الـ tests
        Task task = new Task();
        task.setTitle("My First Task");
        task.setDescription("Initial description");
        task.setCompleted(false);
        task.setUser(savedUser);
        existingTask = taskRepository.save(task);
    }

    // ================================================================
    // POST /api/tasks
    // ================================================================
    @Nested
    @DisplayName("POST /api/tasks — createTask")
    class CreateTask {

        @Test
        @DisplayName("Happy Path: ينشئ تاسك جديدة ويرجع 201 مع بيانات التاسك")
        void createTask_shouldReturn201WithBody_whenAuthenticated() throws Exception {
            // Arrange
            TaskRequestDto dto = new TaskRequestDto("New Task", "New Description");

            // Act & Assert
            mockMvc.perform(post("/api/tasks")
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("New Task"))
                    .andExpect(jsonPath("$.description").value("New Description"))
                    .andExpect(jsonPath("$.completed").value(false));

            // Assert DB — التاسك اتضافت فعلاً
            assertThat(taskRepository.count()).isEqualTo(2); // existingTask + الجديدة
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized لو مفيش Token")
        void createTask_shouldReturn401_whenNotAuthenticated() throws Exception {
            // Arrange
            TaskRequestDto dto = new TaskRequestDto("Unauthorized Task", "Description");

            // Act & Assert
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // GET /api/tasks
    // ================================================================
    @Nested
    @DisplayName("GET /api/tasks — getAllTasksForCurrentUser")
    class GetAllTasks {

        @Test
        @DisplayName("Happy Path: يرجع صفحة تحتوي مهام اليوزر الحالي فقط")
        void getAllTasks_shouldReturnOnlyCurrentUserTasks() throws Exception {
            // Arrange — نضيف تاسك تانية لليوزر + تاسك للأدمن (المفروض ما تظهرش)
            Task anotherUserTask = new Task();
            anotherUserTask.setTitle("Admin's Task");
            anotherUserTask.setDescription("Should not be visible to user");
            anotherUserTask.setUser(savedAdmin);
            taskRepository.save(anotherUserTask);

            // Act & Assert
            mockMvc.perform(get("/api/tasks")
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))           // مهمة واحدة فقط
                    .andExpect(jsonPath("$.content[0].title").value("My First Task")); // بتاعة اليوزر
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized لو مفيش Token")
        void getAllTasks_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // GET /api/tasks/{id}
    // ================================================================
    @Nested
    @DisplayName("GET /api/tasks/{id} — getTaskById")
    class GetTaskById {

        @Test
        @DisplayName("Happy Path: يرجع التاسك الصحيحة لو ملك اليوزر الحالي")
        void getTaskById_shouldReturn200WithTask_whenOwned() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/tasks/{id}", existingTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(existingTask.getId()))
                    .andExpect(jsonPath("$.title").value("My First Task"));
        }

        @Test
        @DisplayName("Not Found / Business Rule (ملكية): يرجع 404 لو التاسك مش ملك اليوزر")
        void getTaskById_shouldReturn404_whenTaskBelongsToAnotherUser() throws Exception {
            // Arrange — تاسك تخص الأدمن
            Task adminTask = new Task();
            adminTask.setTitle("Admin Only Task");
            adminTask.setUser(savedAdmin);
            Task savedAdminTask = taskRepository.save(adminTask);

            // Act & Assert — اليوزر العادي يحاول يجيب تاسك الأدمن
            mockMvc.perform(get("/api/tasks/{id}", savedAdminTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Not Found: يرجع 404 لو الـ ID مش موجود خالص")
        void getTaskById_shouldReturn404_whenTaskDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/tasks/{id}", 99999L)
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // PUT /api/tasks/{id}
    // ================================================================
    @Nested
    @DisplayName("PUT /api/tasks/{id} — updateTask")
    class UpdateTask {

        @Test
        @DisplayName("Happy Path: يعدّل التاسك ويرجع 200 مع البيانات الجديدة")
        void updateTask_shouldReturn200WithUpdatedData_whenOwned() throws Exception {
            // Arrange
            TaskRequestDto updateDto = new TaskRequestDto("Updated Title", "Updated Description");

            // Act & Assert
            mockMvc.perform(put("/api/tasks/{id}", existingTask.getId())
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.description").value("Updated Description"));

            // Assert DB — التغيير اتحفظ في H2 فعلاً
            Task reloaded = taskRepository.findById(existingTask.getId()).orElseThrow();
            assertThat(reloaded.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("Business Rule (ملكية): يرجع 404 لو حاول يعدّل تاسك شخص تاني")
        void updateTask_shouldReturn404_whenTaskNotOwnedByCurrentUser() throws Exception {
            // Arrange
            Task adminTask = new Task();
            adminTask.setTitle("Admin Task");
            adminTask.setUser(savedAdmin);
            Task savedAdminTask = taskRepository.save(adminTask);

            TaskRequestDto updateDto = new TaskRequestDto("Hacked Title", "Hacked Desc");

            // Act & Assert
            mockMvc.perform(put("/api/tasks/{id}", savedAdminTask.getId())
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // PATCH /api/tasks/{id}/toggle
    // ================================================================
    @Nested
    @DisplayName("PATCH /api/tasks/{id}/toggle — toggleTaskStatus")
    class ToggleTaskStatus {

        @Test
        @DisplayName("Happy Path: يعكس حالة التاسك من false لـ true ويرجع 200")
        void toggleTask_shouldFlipCompletedStatus_whenOwned() throws Exception {
            // Act & Assert
            mockMvc.perform(patch("/api/tasks/{id}/toggle", existingTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true));

            // Assert DB
            Task reloaded = taskRepository.findById(existingTask.getId()).orElseThrow();
            assertThat(reloaded.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("Edge Case: Toggle مرتين يرجع الحالة لـ false")
        void toggleTask_shouldReturnToFalse_whenToggledTwice() throws Exception {
            // Act — Toggle أول مرة
            mockMvc.perform(patch("/api/tasks/{id}/toggle", existingTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true));

            // Act — Toggle تانية مرة
            mockMvc.perform(patch("/api/tasks/{id}/toggle", existingTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(false));
        }

        @Test
        @DisplayName("Business Rule (ملكية): يرجع 404 لو حاول يعمل Toggle لتاسك شخص تاني")
        void toggleTask_shouldReturn404_whenTaskNotOwned() throws Exception {
            // Arrange
            Task adminTask = new Task();
            adminTask.setTitle("Admin Task");
            adminTask.setUser(savedAdmin);
            Task savedAdminTask = taskRepository.save(adminTask);

            // Act & Assert
            mockMvc.perform(patch("/api/tasks/{id}/toggle", savedAdminTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // DELETE /api/tasks/{id}
    // ================================================================
    @Nested
    @DisplayName("DELETE /api/tasks/{id} — deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("Happy Path: يحذف التاسك ويرجع 204 No Content")
        void deleteTask_shouldReturn204_whenOwned() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/tasks/{id}", existingTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isNoContent());

            // Assert DB — التاسك اتمسحت فعلاً من H2
            assertThat(taskRepository.findById(existingTask.getId())).isEmpty();
        }

        @Test
        @DisplayName("Security: يرجع 401 Unauthorized لو مفيش Token")
        void deleteTask_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/tasks/{id}", existingTask.getId()))
                    .andExpect(status().isUnauthorized());

            // Assert DB — التاسك لسه موجودة
            assertThat(taskRepository.findById(existingTask.getId())).isPresent();
        }

        @Test
        @DisplayName("Business Rule (ملكية): يرجع 404 ولا يحذف تاسك شخص تاني")
        void deleteTask_shouldReturn404_whenTaskNotOwnedByCurrentUser() throws Exception {
            // Arrange
            Task adminTask = new Task();
            adminTask.setTitle("Admin Task");
            adminTask.setUser(savedAdmin);
            Task savedAdminTask = taskRepository.save(adminTask);

            // Act & Assert
            mockMvc.perform(delete("/api/tasks/{id}", savedAdminTask.getId())
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isNotFound());

            // Assert DB — تاسك الأدمن لسه موجودة
            assertThat(taskRepository.findById(savedAdminTask.getId())).isPresent();
        }

        @Test
        @DisplayName("Not Found: يرجع 404 لو الـ ID مش موجود أصلاً")
        void deleteTask_shouldReturn404_whenTaskDoesNotExist() throws Exception {
            mockMvc.perform(delete("/api/tasks/{id}", 99999L)
                            .header("Authorization", bearerToken(userToken)))
                    .andExpect(status().isNotFound());
        }
    }
}