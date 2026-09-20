package com.demo.controller;

import com.demo.dto.TaskRequestDto;
import com.demo.dto.TaskResponseDto;
import com.demo.security.AppPermission;
import com.demo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.demo.security.AppPermission.Names.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GROUP_SUPER_ADMIN') or hasAuthority('" + WRITE_TASK + "')")
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            @AuthenticationPrincipal UserDetails currentUser) {

        TaskResponseDto createdTask = taskService.createTask(taskRequestDto, currentUser);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GROUP_SUPER_ADMIN') or hasAuthority('" + READ_TASK + "')")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(taskService.getAllTasksForCurrentUser(currentUser, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GROUP_SUPER_ADMIN') or hasAuthority('" + READ_TASK + "')")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(taskService.getTaskById(id, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GROUP_SUPER_ADMIN') or hasAuthority('" + WRITE_TASK + "')")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(taskService.updateTask(id, taskRequestDto, currentUser));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('GROUP_SUPER_ADMIN') or hasAuthority('" + WRITE_TASK + "')")
    public ResponseEntity<TaskResponseDto> toggleTaskStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(taskService.toggleTaskStatus(id, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GROUP_SUPER_ADMIN') or hasAuthority('" + WRITE_TASK + "')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
