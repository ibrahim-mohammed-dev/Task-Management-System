package com.demo.controller;

import com.demo.dto.TaskResponseDto;
import com.demo.dto.UserResponseDto;
import com.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController
{
    private final AdminService adminService;

    @PreAuthorize("hasAuthority('VIEW_ALL_USERS')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> showAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PreAuthorize("hasAuthority('VIEW_ALL_TASKS')")
    @GetMapping("/tasks")
    public ResponseEntity<Page<TaskResponseDto>> showAllTasks(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(adminService.getAllTasks(pageable));
    }

    @PreAuthorize("hasAuthority('DELETE_ANY_TASK')")
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id)
    {
        adminService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}