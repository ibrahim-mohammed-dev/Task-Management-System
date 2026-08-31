package com.demo.service;

import com.demo.dto.TaskRequestDto;
import com.demo.dto.TaskResponseDto;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.TaskMapper;
import com.demo.model.Task;
import com.demo.model.User;
import com.demo.repository.TaskRepository;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * عزل تام: لا يوجد @SpringBootTest ولا اتصال حقيقي بقاعدة بيانات.
 * كل الـ Dependencies (Repositories / Mapper) تم عمل Mock لها بالكامل.
 *
 * ملاحظة/افتراض: افترضت أن Task و User عندهم Default Constructor + Setters عادية،
 * وأن getId() موجودة على User. لو الـ Entities عندك مختلفة (مثلاً Builder pattern)
 * عدّل فقط أسطر بناء الكائنات (Arrange) بدون المساس بمنطق الـ Test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User currentUser;
    private Task task;
    private TaskResponseDto taskResponseDto;

    private static final Long USER_ID = 1L;
    private static final Long TASK_ID = 10L;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(USER_ID);

        task = new Task();
        task.setId(TASK_ID);
        task.setTitle("Original Title");
        task.setDescription("Original Description");
        task.setCompleted(false);
        task.setUser(currentUser);

        taskResponseDto = new TaskResponseDto(TASK_ID, "Original Title", "Original Description", false);
    }

    // ============================================================
    // 1. createTask
    // ============================================================
    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("Happy Path: ينشئ التاسك ويربطها باليوزر الحالي بنجاح")
        void createTask_shouldSaveAndReturnDto_whenUserExists() {
            // Arrange
            TaskRequestDto requestDto = new TaskRequestDto("New Task", "New Description");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(currentUser));
            when(taskMapper.toEntity(requestDto)).thenReturn(task);
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

            // Act
            TaskResponseDto result = taskService.createTask(requestDto, currentUser);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(taskResponseDto);
            assertThat(task.getUser()).isEqualTo(currentUser); // تأكيد الربط بالمالك
            verify(taskRepository, times(1)).save(task);
            verify(userRepository, times(1)).findById(USER_ID);
        }

        @Test
        @DisplayName("Not Found: يرمي ResourceNotFoundException عند عدم وجود اليوزر في الداتابيز")
        void createTask_shouldThrowException_whenUserNotFound() {
            // Arrange
            TaskRequestDto requestDto = new TaskRequestDto("New Task", "New Description");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> taskService.createTask(requestDto, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    // ============================================================
    // 2. getAllTasksForCurrentUser
    // ============================================================
    @Nested
    @DisplayName("getAllTasksForCurrentUser")
    class GetAllTasks {

        @Test
        @DisplayName("Happy Path: يرجع صفحة مهام تخص اليوزر الحالي فقط")
        void getAllTasks_shouldReturnPagedTasks_forCurrentUser() {
            // Arrange
            Page<Task> taskPage = new PageImpl<>(List.of(task));
            when(taskRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(taskPage);
            when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

            // Act
            Page<TaskResponseDto> result = taskService.getAllTasksForCurrentUser(
                    currentUser, 0, 10, "id", "ASC");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(taskResponseDto);
            verify(taskRepository, times(1)).findByUserId(eq(USER_ID), any(Pageable.class));
        }

        @Test
        @DisplayName("Edge Case: يرجع صفحة فاضية لو اليوزر مش عنده مهام")
        void getAllTasks_shouldReturnEmptyPage_whenUserHasNoTasks() {
            // Arrange
            Page<Task> emptyPage = new PageImpl<>(List.of());
            when(taskRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<TaskResponseDto> result = taskService.getAllTasksForCurrentUser(
                    currentUser, 0, 10, "id", "DESC");

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(taskMapper, never()).toResponseDto(any(Task.class));
        }
    }

    // ============================================================
    // 3. getTaskById
    // ============================================================
    @Nested
    @DisplayName("getTaskById")
    class GetTaskById {

        @Test
        @DisplayName("Happy Path: يرجع التاسك لو موجودة وملك اليوزر الحالي")
        void getTaskById_shouldReturnDto_whenTaskBelongsToUser() {
            // Arrange
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
            when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

            // Act
            TaskResponseDto result = taskService.getTaskById(TASK_ID, currentUser);

            // Assert
            assertThat(result).isEqualTo(taskResponseDto);
        }

        @Test
        @DisplayName("Not Found (Business Rule: ملكية): يرمي Exception لو التاسك مش موجودة أو مش ملك اليوزر")
        void getTaskById_shouldThrowException_whenTaskNotFoundOrNotOwned() {
            // Arrange
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> taskService.getTaskById(TASK_ID, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found with id: " + TASK_ID);
        }
    }

    // ============================================================
    // 4. updateTask
    // ============================================================
    @Nested
    @DisplayName("updateTask")
    class UpdateTask {

        @Test
        @DisplayName("Happy Path: يعدل بيانات التاسك ويحفظها بنجاح")
        void updateTask_shouldUpdateFieldsAndSave_whenTaskBelongsToUser() {
            // Arrange
            TaskRequestDto updateDto = new TaskRequestDto("Updated Title", "Updated Description");
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponseDto(task)).thenReturn(
                    new TaskResponseDto(TASK_ID, "Updated Title", "Updated Description", false));

            // Act
            TaskResponseDto result = taskService.updateTask(TASK_ID, updateDto, currentUser);

            // Assert
            assertThat(task.getTitle()).isEqualTo("Updated Title");
            assertThat(task.getDescription()).isEqualTo("Updated Description");
            assertThat(result.title()).isEqualTo("Updated Title");
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("Not Found (Business Rule: ملكية): يرمي Exception ولا يحفظ أي تعديل لو التاسك مش ملك اليوزر")
        void updateTask_shouldThrowException_whenTaskNotOwnedByUser() {
            // Arrange
            TaskRequestDto updateDto = new TaskRequestDto("Hacked Title", "Hacked Description");
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> taskService.updateTask(TASK_ID, updateDto, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    // ============================================================
    // 5. deleteTask
    // ============================================================
    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("Happy Path: يحذف التاسك بنجاح لو ملك اليوزر الحالي")
        void deleteTask_shouldDeleteTask_whenTaskBelongsToUser() {
            // Arrange
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));

            // Act
            taskService.deleteTask(TASK_ID, currentUser);

            // Assert
            verify(taskRepository, times(1)).delete(task);
        }

        @Test
        @DisplayName("Not Found (Business Rule: ملكية): يرمي Exception ولا يستدعي delete لو التاسك مش ملك اليوزر")
        void deleteTask_shouldThrowException_whenTaskNotOwnedByUser() {
            // Arrange
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> taskService.deleteTask(TASK_ID, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any(Task.class));
        }
    }

    // ============================================================
    // 6. toggleTaskStatus
    // ============================================================
    @Nested
    @DisplayName("toggleTaskStatus")
    class ToggleTaskStatus {

        @Test
        @DisplayName("Happy Path: يعكس حالة التاسك من false إلى true")
        void toggleTaskStatus_shouldFlipCompletedFromFalseToTrue() {
            // Arrange
            task.setCompleted(false);
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponseDto(task)).thenReturn(
                    new TaskResponseDto(TASK_ID, task.getTitle(), task.getDescription(), true));

            // Act
            TaskResponseDto result = taskService.toggleTaskStatus(TASK_ID, currentUser);

            // Assert
            assertThat(task.isCompleted()).isTrue();
            assertThat(result.completed()).isTrue();
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("Edge Case: يعكس حالة التاسك من true إلى false")
        void toggleTaskStatus_shouldFlipCompletedFromTrueToFalse() {
            // Arrange
            task.setCompleted(true);
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponseDto(task)).thenReturn(
                    new TaskResponseDto(TASK_ID, task.getTitle(), task.getDescription(), false));

            // Act
            taskService.toggleTaskStatus(TASK_ID, currentUser);

            // Assert
            assertThat(task.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("Not Found (Business Rule: ملكية): يرمي Exception لو التاسك مش ملك اليوزر")
        void toggleTaskStatus_shouldThrowException_whenTaskNotOwnedByUser() {
            // Arrange
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> taskService.toggleTaskStatus(TASK_ID, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }
    }
}
