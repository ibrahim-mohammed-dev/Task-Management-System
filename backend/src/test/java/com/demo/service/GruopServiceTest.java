package com.demo.service;

import com.demo.dto.GroupRequestDto;
import com.demo.dto.GroupResponseDto;
import com.demo.exception.DuplicateResourceException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.GroupMapper;
import com.demo.model.Group;
import com.demo.model.Permission;
import com.demo.model.User;
import com.demo.repository.GroupRepository;
import com.demo.repository.PermissionRepository;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupService groupService;

    @Nested
    @DisplayName("Create Group Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("Should create group successfully")
        void createGroup_Success() {
            GroupRequestDto dto = new GroupRequestDto("ADMINS");
            Group group = new Group();
            group.setName("ADMINS");

            Group savedGroup = new Group();
            savedGroup.setId(1L);
            savedGroup.setName("ADMINS");

            GroupResponseDto expectedResponse = new GroupResponseDto(1L, "ADMINS");

            when(groupRepository.existsByName("ADMINS")).thenReturn(false);
            when(groupMapper.toEntity(dto)).thenReturn(group);
            when(groupRepository.save(group)).thenReturn(savedGroup);
            when(groupMapper.toResponseDto(savedGroup)).thenReturn(expectedResponse);

            GroupResponseDto result = groupService.createGroup(dto);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("ADMINS", result.name());

            verify(groupRepository, times(1)).save(group);
        }
        @Test
        @DisplayName("Should throw DuplicateResourceException when group name already exists")
        void createGroup_DuplicateName_ThrowsException() {
            GroupRequestDto dto = new GroupRequestDto("ADMINS");

            // 2. محاكاة وجود الاسم بالفعل
            when(groupRepository.existsByName("ADMINS")).thenReturn(true);

            // التأكد من إطلاق الاستثناء
            assertThrows(DuplicateResourceException.class,
                    () -> groupService.createGroup(dto));

            // التأكد من أن الحفظ لم يتم أبداً
            verify(groupRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Assign User To Group Tests")
    class AssignUserToGroupTests {

        @Test
        @DisplayName("Should assign user to group successfully")
        void assignUserToGroup_Success() {
            Long userId = 1L;
            Long groupId = 10L;

            User user = new User();
            user.setId(userId);

            Group group = new Group();
            group.setId(groupId);
            group.setName("ADMINS");

            GroupResponseDto expectedResponse = new GroupResponseDto(groupId, "ADMINS");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(groupRepository.existsByIdAndUsersId(groupId, userId)).thenReturn(false);
            when(groupMapper.toResponseDto(group)).thenReturn(expectedResponse);

            GroupResponseDto result = groupService.assignUserToGroup(userId, groupId);

            assertNotNull(result);
            assertEquals(groupId, result.id());
            assertEquals("ADMINS", result.name());
            assertTrue(group.getUsers().contains(user));
            verify(groupRepository).existsByIdAndUsersId(groupId, userId);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when user is already in group")
        void assignUserToGroup_DuplicateUser_ThrowsException() {
            Long userId = 1L;
            Long groupId = 10L;

            User user = new User();
            Group group = new Group();

            // استخدام any() يمنع أي فشل محتمل بسبب تداخل الـ IDs أو الـ Context
            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(groupRepository.findById(any())).thenReturn(Optional.of(group));
            when(groupRepository.existsByIdAndUsersId(any(), any())).thenReturn(true);

            assertThrows(DuplicateResourceException.class,
                    () -> groupService.assignUserToGroup(userId, groupId));

            verify(groupMapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void assignUserToGroup_UserNotFound_ThrowsException() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> groupService.assignUserToGroup(1L, 10L));
        }
        @Test
        @DisplayName("Should throw ResourceNotFoundException when group does not exist")
        void assignUserToGroup_GroupNotFound_ThrowsException() {
            Long userId = 1L;
            User user = new User();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(groupRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> groupService.assignUserToGroup(userId, 10L));
        }
    }

    @Nested
    @DisplayName("Remove User From Group Tests")
    class RemoveUserFromGroupTests {

        @Test
        @DisplayName("Should remove user from group successfully")
        void removeUserFromGroup_Success() {
            Long userId = 1L;
            Long groupId = 10L;

            User user = new User();
            user.setId(userId);

            Group group = new Group();
            group.setId(groupId);
            group.getUsers().add(user);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

            groupService.removeUserFromGroup(userId, groupId);

            assertFalse(group.getUsers().contains(user));
        }
    }

    @Nested
    @DisplayName("Add Permission To Group Tests")
    class AddPermissionToGroupTests {

        @Test
        @DisplayName("Should add permission to group successfully")
        void addPermissionToGroup_Success() {
            Long perId = 5L;
            Long groupId = 10L;

            Permission permission = new Permission();
            permission.setId(perId);

            Group group = new Group();
            group.setId(groupId);

            GroupResponseDto expectedResponse = new GroupResponseDto(groupId, "ADMINS");

            when(permissionRepository.findById(perId)).thenReturn(Optional.of(permission));
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(groupRepository.existsByIdAndPermissionsId(groupId, perId)).thenReturn(false);
            when(groupMapper.toResponseDto(group)).thenReturn(expectedResponse);

            GroupResponseDto result = groupService.addPermissionToGroup(perId, groupId);

            assertNotNull(result);
            assertTrue(group.getPermissions().contains(permission));
        }
        @Test
        @DisplayName("Should throw DuplicateResourceException when permission is already in group")
        void addPermissionToGroup_DuplicatePermission_ThrowsException() {
            Long perId = 1L;
            Long groupId = 10L;

            Permission permission =new Permission();
            Group group = new Group();

            when(permissionRepository.findById(perId)).thenReturn(Optional.of(permission));
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(groupRepository.existsByIdAndPermissionsId(groupId, perId)).thenReturn(true);

            assertThrows(DuplicateResourceException.class,
                    () -> groupService.addPermissionToGroup(perId, groupId));

            verify(groupMapper, never()).toResponseDto(any());
        }
    }
}