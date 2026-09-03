package com.demo.controller;

import com.demo.dto.TaskRequestDto;
import com.demo.dto.TaskResponseDto;
import com.demo.model.User;
import com.demo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. إنشاء مهمة جديدة
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_TASK')")
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            @AuthenticationPrincipal UserDetails currentUser) {

        TaskResponseDto createdTask = taskService.createTask(taskRequestDto, currentUser);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }


    // 2. جلب جميع مهام المستخدم الحالي (مع Pagination وترتيب)
    @PreAuthorize("hasAuthority('VIEW_TASKS')")
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<TaskResponseDto> tasks = taskService.getAllTasksForCurrentUser(currentUser, page, size, sortBy, sortDir);
        return ResponseEntity.ok(tasks);
    }

    // 3. جلب مهمة واحدة بالـ ID
    @PreAuthorize("hasAuthority('VIEW_TASKS')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(taskService.getTaskById(id, currentUser));
    }

    // 4. تعديل عنوان ووصف المهمة (PUT)
    @PreAuthorize("hasAuthority('EDIT_TASK')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(taskService.updateTask(id, taskRequestDto, currentUser));
    }

    // 5. تغيير حالة الإنجاز فقط (PATCH)
    @PreAuthorize("hasAuthority('EDIT_TASK')")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TaskResponseDto> toggleTaskStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(taskService.toggleTaskStatus(id, currentUser));
    }

    // 6. حذف مهمة
    @PreAuthorize("hasAuthority('DELETE_TASK')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

