package com.demo.controller;

import com.demo.dto.GroupRequestDto;
import com.demo.model.Group;
import com.demo.model.Permission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * Integration Tests — GroupController
 *   POST   /api/groups
 *   POST   /api/groups/{groupId}/users/{userId}
 *   DELETE /api/groups/{groupId}/users/{userId}
 *   POST   /api/groups/{groupId}/permission/{perId}
 * ============================================================
 * الإعدادات موروثة من BaseIntegrationTest:
 *   - H2 in-memory DB (توافقية PostgreSQL)
 *   - @Transactional → Rollback بعد كل test
 *   - الـ groupRepository و permissionRepository و adminToken جاهزون للاستخدام
 */
@DisplayName("GroupController Integration Tests")
class GroupControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("Create Group Endpoint Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("Should create group successfully and return 201 Created")
        void createGroup_Success() throws Exception {
            GroupRequestDto requestDto = new GroupRequestDto("MANAGERS");

            mockMvc.perform(post("/api/groups")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("MANAGERS"));
        }

        @Test
        @DisplayName("Should return 409 Conflict when group name already exists")
        void createGroup_DuplicateName_ReturnsConflict() throws Exception {
            // حفظ جروب مسبقاً بنفس الاسم (مع تجنب التعارض مع جروبات BaseIntegrationTest عبر استخدام اسم فريد هنا)
            Group group = new Group();
            group.setName("MODERATORS");
            groupRepository.save(group);

            GroupRequestDto requestDto = new GroupRequestDto("MODERATORS");

            mockMvc.perform(post("/api/groups")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when group name is blank")
        void createGroup_BlankName_ReturnsBadRequest() throws Exception {
            GroupRequestDto requestDto = new GroupRequestDto("");

            mockMvc.perform(post("/api/groups")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Assign User To Group Endpoint Tests")
    class AssignUserTests {

        @Test
        @DisplayName("Should assign user to group successfully and return 201 Created")
        void assignUserToGroup_Success() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            Group savedGroup = groupRepository.save(group);

            mockMvc.perform(post("/api/groups/{groupId}/users/{userId}", savedGroup.getId(), savedUser.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(savedGroup.getId()));
        }

        @Test
        @DisplayName("Should return 404 Not Found when user does not exist")
        void assignUserToGroup_UserNotFound_ReturnsNotFound() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            Group savedGroup = groupRepository.save(group);

            long nonExistentUserId = 999L;

            mockMvc.perform(post("/api/groups/{groupId}/users/{userId}", savedGroup.getId(), nonExistentUserId)
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when group does not exist")
        void assignUserToGroup_GroupNotFound_ReturnsNotFound() throws Exception {
            long nonExistentGroupId = 999L;

            mockMvc.perform(post("/api/groups/{groupId}/users/{userId}", nonExistentGroupId, savedUser.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 Conflict when user is already assigned to group")
        void assignUserToGroup_DuplicateUser_ReturnsConflict() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            group.getUsers().add(savedUser); // اليوزر موجود بالفعل في الجروب
            Group savedGroup = groupRepository.save(group);

            mockMvc.perform(post("/api/groups/{groupId}/users/{userId}", savedGroup.getId(), savedUser.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Remove User From Group Endpoint Tests")
    class RemoveUserTests {

        @Test
        @DisplayName("Should remove user from group successfully and return 204 No Content")
        void removeUserFromGroup_Success() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            group.getUsers().add(savedUser);
            Group savedGroup = groupRepository.save(group);

            mockMvc.perform(delete("/api/groups/{groupId}/users/{userId}", savedGroup.getId(), savedUser.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 Not Found when removing non-existent user from group")
        void removeUserFromGroup_UserNotFound_ReturnsNotFound() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            Group savedGroup = groupRepository.save(group);

            mockMvc.perform(delete("/api/groups/{groupId}/users/{userId}", savedGroup.getId(), 999L)
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Add Permission To Group Endpoint Tests")
    class AddPermissionTests {

        @Test
        @DisplayName("Should add permission to group successfully and return 201 Created")
        void addPermissionToGroup_Success() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            Group savedGroup = groupRepository.save(group);

            Permission permission = new Permission();
            permission.setName("CUSTOM_PERMISSION");
            Permission savedPermission = permissionRepository.save(permission);

            mockMvc.perform(post("/api/groups/{groupId}/permission/{perId}", savedGroup.getId(), savedPermission.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(savedGroup.getId()));
        }

        @Test
        @DisplayName("Should return 404 Not Found when permission does not exist")
        void addPermissionToGroup_PermissionNotFound_ReturnsNotFound() throws Exception {
            Group group = new Group();
            group.setName("DEVS");
            Group savedGroup = groupRepository.save(group);

            mockMvc.perform(post("/api/groups/{groupId}/permission/{perId}", savedGroup.getId(), 999L)
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 Conflict when permission is already assigned to group")
        void addPermissionToGroup_DuplicatePermission_ReturnsConflict() throws Exception {
            Permission permission = new Permission();
            permission.setName("ANOTHER_CUSTOM_PERMISSION");
            Permission savedPermission = permissionRepository.save(permission);

            Group group = new Group();
            group.setName("DEVS");
            group.getPermissions().add(savedPermission); // الصلاحية موجودة مسبقاً
            Group savedGroup = groupRepository.save(group);

            mockMvc.perform(post("/api/groups/{groupId}/permission/{perId}", savedGroup.getId(), savedPermission.getId())
                            .header("Authorization", bearerToken(adminToken)))
                    .andExpect(status().isConflict());
        }
    }
}