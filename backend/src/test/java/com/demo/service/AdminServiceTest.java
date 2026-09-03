package com.demo.service;

import com.demo.dto.UserResponseDto;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.TaskMapper;
import com.demo.mapper.UserMapper;
import com.demo.model.Task;
import com.demo.model.User;
import com.demo.repository.TaskRepository;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private AdminService adminService;

    // ==========================================
    // 1. اختبارات ميثود deleteTask
    // ==========================================

    @Test
    void shouldDeleteTask_WhenTaskExists() {
        // Arrange (التحضير)
        Long taskId = 10L;
        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act (التنفيذ)
        adminService.deleteTask(taskId);

        // Assert (التحقق)
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void shouldThrowException_WhenTaskNotFound() {
        // Arrange (التحضير)
        Long taskId = 99L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert (التنفيذ والتحقق من الاستثناء)
        assertThrows(ResourceNotFoundException.class, () -> {
            adminService.deleteTask(taskId);
        });

        // التأكد من أن دالة الحذف لم يتم استدعاؤها نهائياً
        verify(taskRepository, never()).delete(any());
    }
}