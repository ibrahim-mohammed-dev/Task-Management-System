package com.demo.controller;

import com.demo.dto.RoleRequestDto;
import com.demo.dto.TaskResponseDto;
import com.demo.dto.UserResponseDto;
import com.demo.model.User;
import com.demo.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController
{
    private final AdminService adminService;
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> showAllUsers(
            @PageableDefault
                    (page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }
    @GetMapping("/tasks")
    public ResponseEntity<Page<TaskResponseDto>> showAllTasks(
            @PageableDefault
                    (page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(adminService.getAllTasks(pageable));
    }
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id)
    {
        adminService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponseDto> changeUserRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleRequestDto roleDto,
            Authentication authentication) { // 👈 استخراج بيانات الأدمن الحالي
        User currentAdmin = (User) authentication.getPrincipal();
        return ResponseEntity.ok(adminService.updateUserRole(id, roleDto, currentAdmin.getId()));
    }

}
