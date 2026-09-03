package com.demo.controller;

import com.demo.model.Group;
import com.demo.model.Permission;
import com.demo.model.User;
import com.demo.repository.GroupRepository;
import com.demo.repository.PermissionRepository;
import com.demo.repository.TaskRepository;
import com.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    protected static final String USER_USERNAME  = "testuser";
    protected static final String USER_EMAIL     = "testuser@demo.com";
    protected static final String USER_PASSWORD  = "P@ssw0rd1";

    protected static final String ADMIN_USERNAME = "testadmin";
    protected static final String ADMIN_EMAIL    = "testadmin@demo.com";
    protected static final String ADMIN_PASSWORD = "P@ssw0rd2";

    @Autowired private WebApplicationContext webApplicationContext;
    protected ObjectMapper objectMapper = new ObjectMapper();
    @Autowired protected UserRepository      userRepository;
    @Autowired protected TaskRepository      taskRepository;
    @Autowired protected GroupRepository     groupRepository;
    @Autowired protected PermissionRepository permissionRepository;
    @Autowired protected PasswordEncoder     passwordEncoder;

    protected MockMvc mockMvc;
    protected User    savedUser;
    protected User    savedAdmin;
    protected String  userToken;
    protected String  adminToken;

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

        // 4. إنشاء المستخدم العادي وربطه بـ USERS Group
        User user = new User();
        user.setUsername(USER_USERNAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        user.getGroups().add(userGroup);
        savedUser = userRepository.save(user);

        // 5. إنشاء المستخدم المشرف وربطه بـ ADMINS Group
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.getGroups().add(adminGroup);
        savedAdmin = userRepository.save(admin);

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

        return result.getResponse().getContentAsString();
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}