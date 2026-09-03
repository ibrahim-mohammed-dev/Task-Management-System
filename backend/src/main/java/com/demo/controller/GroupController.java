package com.demo.controller;

import com.demo.dto.*;
import com.demo.service.GroupService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_GROUPS')")
public class GroupController
{
    private final GroupService groupService;
    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@Valid @RequestBody GroupRequestDto dto)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(dto));
    }
    @PostMapping("/permissions")
    public ResponseEntity<PermissionResponseDto> createPermission(@Valid @RequestBody PermissionRequestDto dto){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createPermission(dto));
    }
    @PostMapping("/{groupId}/users/{userId}")
    public ResponseEntity<GroupResponseDto> assignUserToGroup(@PathVariable Long userId,@PathVariable Long groupId){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                groupService.assignUserToGroup(userId, groupId)
        );    }
    @DeleteMapping("/{groupId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromGroup(@PathVariable Long userId,@PathVariable Long groupId){
        groupService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{groupId}/permission/{perId}")
    public ResponseEntity<GroupResponseDto> addPermissionToGroup(@PathVariable Long perId,@PathVariable Long groupId){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                groupService.addPermissionToGroup(perId, groupId)
        );    }
    @GetMapping("/groups")
    public ResponseEntity<Page<GroupResponseDto>> showAllGroups(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(groupService.getAllGroups(pageable));
    }
    @GetMapping("/permissions")
    public ResponseEntity<Page<PermissionResponseDto>> showAllPermissions(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(groupService.getAllpermissions(pageable));
    }
}
