package com.demo.service;

import com.demo.dto.RoleRequestDto;
import com.demo.dto.UserResponseDto;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.TaskMapper;
import com.demo.mapper.UserMapper;
import com.demo.model.Role;
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

    // ==========================================
    // 2. اختبارات ميثود updateUserRole
    // ==========================================

    @Test
    void shouldUpdateUserRole_Successfully() {
        // Arrange (التحضير)
        Long userId = 2L;
        Long adminId = 1L; // مختلف عن الـ userId
        RoleRequestDto roleDto = new RoleRequestDto(Role.ADMIN);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setRole(Role.USER);

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setRole(Role.ADMIN);

        UserResponseDto expectedResponse = new UserResponseDto(userId, "ziad@example.com",Role.ADMIN,"ziad");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(expectedResponse);

        // Act (التنفيذ)
        UserResponseDto response = adminService.updateUserRole(userId, roleDto, adminId);

        // Assert (التحقق)
        assertNotNull(response);
        verify(userRepository, times(1)).save(existingUser);
        assertEquals(Role.ADMIN, existingUser.getRole());
    }

    @Test
    void shouldThrowException_WhenAdminTriesToChangeOwnRole() {
        // Arrange (التحضير)
        Long adminId = 1L;
        RoleRequestDto roleDto = new RoleRequestDto(Role.USER);

        // Act & Assert (التنفيذ والتحقق من قاعدة الـ Business)
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adminService.updateUserRole(adminId, roleDto, adminId); // نفس الـ ID
        });

        assertEquals("400 BAD_REQUEST \"You cannot change your own role!\"", exception.getMessage());

        // التأكد من أن الداتابيز لم يتم استدعاؤها نهائياً لأن الطلب رُفض مسبقاً
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenUserToUpdateNotFound() {
        // Arrange (التحضير)
        Long userId = 50L;
        Long adminId = 1L;
        RoleRequestDto roleDto = new RoleRequestDto(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (التنفيذ والتحقق من عدم وجود المستخدم)
        assertThrows(ResourceNotFoundException.class, () -> {
            adminService.updateUserRole(userId, roleDto, adminId);
        });

        verify(userRepository, never()).save(any());
    }
}