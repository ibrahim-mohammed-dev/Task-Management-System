package com.demo.controller;

import com.demo.model.Group;
import com.demo.model.Permission;
import com.demo.model.User;
import com.demo.repository.GroupRepository;
import com.demo.repository.PermissionRepository;
import com.demo.repository.RefreshTokenRepository;
import com.demo.repository.TaskRepository;
import com.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    protected static final String USER_USERNAME  = "testuser";
    protected static final String USER_EMAIL     = "testuser@demo.com";
    protected static final String USER_PASSWORD  = "P@ssw0rd1";

    protected static final String ADMIN_USERNAME = "testadmin";
    protected static final String ADMIN_EMAIL    = "testadmin@demo.com";
    protected static final String ADMIN_PASSWORD = "P@ssw0rd2";

    @Autowired private WebApplicationContext webApplicationContext;
    protected ObjectMapper objectMapper = new ObjectMapper();
    @Autowired protected UserRepository        userRepository;
    @Autowired protected TaskRepository        taskRepository;
    @Autowired protected GroupRepository       groupRepository;
    @Autowired protected PermissionRepository  permissionRepository;
    @Autowired protected RefreshTokenRepository refreshTokenRepository;
    @Autowired protected PasswordEncoder       passwordEncoder;

    protected MockMvc mockMvc;
    protected User    savedUser;
    protected User    savedAdmin;
    protected String  userToken;
    protected String  adminToken;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        taskRepository.deleteAll();
        groupRepository.deleteAll();   // يحذف user_groups و group_permissions تلقائياً (owning side)
        userRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @BeforeEach
    void setUpAll() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 1. إنشاء الصلاحيات الأساسية للنظام بأمان
        Permission p1 = createPermission("VIEW_ALL_USERS");
        Permission p2 = createPermission("VIEW_ALL_TASKS");
        Permission p3 = createPermission("DELETE_ANY_TASK");
        Permission p4 = createPermission("MANAGE_GROUPS");
        Permission p5 = createPermission("CREATE_TASK");
        Permission p6 = createPermission("VIEW_TASKS");
        Permission p7 = createPermission("EDIT_TASK");
        Permission p8 = createPermission("DELETE_TASK");
        Permission p9 = createPermission("VIEW_PROFILE");

        // 2. إنشاء مجموعة الأدمن وربطها بكل الصلاحيات
        Group adminGroup = new Group();
        adminGroup.setName("ADMINS");
        adminGroup.getPermissions().addAll(Set.of(p1, p2, p3, p4, p5, p6, p7, p8, p9));
        groupRepository.save(adminGroup);

        // 3. إنشاء مجموعة المستخدمين العاديين وربطها بصلاحيات المهام والبروفايل
        Group userGroup = new Group();
        userGroup.setName("USERS");
        userGroup.getPermissions().addAll(Set.of(p5, p6, p7, p8, p9));
        groupRepository.save(userGroup);

        // 4. إنشاء المستخدم العادي وربطه بـ USERS Group (owning side)
        User user = new User();
        user.setUsername(USER_USERNAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        savedUser = userRepository.save(user);
        userGroup.getUsers().add(savedUser);
        groupRepository.save(userGroup);

        // 5. إنشاء المستخدم المشرف وربطه بـ ADMINS Group (owning side)
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        savedAdmin = userRepository.save(admin);
        adminGroup.getUsers().add(savedAdmin);
        groupRepository.save(adminGroup);

        // 6. استخراج الـ JWT Tokens لكل منهما لتستخدم في الـ Tests
        userToken  = fetchToken(USER_USERNAME,  USER_PASSWORD);
        adminToken = fetchToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    // Helper method لتجنب تمرير null وتسهيل إنشاء الصلاحيات
    private Permission createPermission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        return permissionRepository.save(permission);
    }

    protected String fetchToken(String username, String password) throws Exception {
        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        try {
            return objectMapper.readTree(response).get("token").asText();
        } catch (Exception e) {
            return response;
        }
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}