package com.demo.service;

import com.demo.dto.GroupRequestDto;
import com.demo.dto.GroupResponseDto;
import com.demo.dto.PermissionRequestDto;
import com.demo.dto.PermissionResponseDto;
import com.demo.exception.DuplicateResourceException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.GroupMapper;
import com.demo.mapper.PermissionMapper;
import com.demo.model.Group;
import com.demo.model.Permission;
import com.demo.model.User;
import com.demo.repository.GroupRepository;
import com.demo.repository.PermissionRepository;
import com.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupService
{
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final GroupMapper groupMapper;
    private final PermissionMapper permissionMapper;

    @Transactional
    public GroupResponseDto createGroup(GroupRequestDto groupRequestDto){
        if (groupRepository.existsByName(groupRequestDto.name())) {
            throw new DuplicateResourceException("Group name already exists");
        }
        Group group = groupMapper.toEntity(groupRequestDto);
        Group saved = groupRepository.save(group);
        return groupMapper.toResponseDto(saved);
    }
    @Transactional
    public PermissionResponseDto createPermission(PermissionRequestDto dto){
        if (permissionRepository.existsByName(dto.name())){
            throw new DuplicateResourceException("Permission Name already exist");
        }
        Permission permission = permissionMapper.toEntity(dto);
        permissionRepository.save(permission);
        return permissionMapper.toResponseDto(permission);
    }
    @Transactional
    public GroupResponseDto assignUserToGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (groupRepository.existsByIdAndUsersId(groupId, userId)) {
            throw new DuplicateResourceException("User already assigned to this group");
        }
        group.getUsers().add(user);
        return groupMapper.toResponseDto(group);
    }
    @Transactional
    public void removeUserFromGroup(Long userId, Long groupId){
        User user =userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        group.getUsers().remove(user);
    }
    @Transactional
    public GroupResponseDto addPermissionToGroup(Long perId, Long groupId) {
        Permission permission = permissionRepository.findById(perId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + perId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (groupRepository.existsByIdAndPermissionsId(groupId, perId)) {
            throw new DuplicateResourceException("Permission already assigned to this group");
        }

        group.getPermissions().add(permission);
        return groupMapper.toResponseDto(group);
    }
    //method for showing all groups
    public Page<GroupResponseDto> getAllGroups(Pageable pageable){
        Page<Group> groups =groupRepository.findAll(pageable);
        return groups.map(groupMapper :: toResponseDto);
    }
    //method for showing all permissions
    public Page<PermissionResponseDto> getAllpermissions(Pageable pageable){
        Page<Permission> permissions =permissionRepository.findAll(pageable);
        return permissions.map(permissionMapper :: toResponseDto);
    }
}
