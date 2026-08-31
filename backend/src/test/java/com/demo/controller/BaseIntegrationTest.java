package com.demo.controller;

import com.demo.model.Role;
import com.demo.model.User;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Spring Boot 4.1.0 أزالت @AutoConfigureMockMvc و Web Slice testing كامل.
 * الحل: بناء MockMvc يدوياً عبر MockMvcBuilders.webAppContextSetup()
 * مع تطبيق springSecurity() عشان الـ Security Filter Chain تشتغل صح.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    // ─── Constants ───────────────────────────────────────────────
    protected static final String USER_USERNAME  = "testuser";
    protected static final String USER_EMAIL     = "testuser@demo.com";
    protected static final String USER_PASSWORD  = "P@ssw0rd1";

    protected static final String ADMIN_USERNAME = "testadmin";
    protected static final String ADMIN_EMAIL    = "testadmin@demo.com";
    protected static final String ADMIN_PASSWORD = "P@ssw0rd2";

    // ─── Injected Beans ──────────────────────────────────────────
    @Autowired private WebApplicationContext webApplicationContext;
    protected ObjectMapper objectMapper = new ObjectMapper();
    @Autowired protected UserRepository      userRepository;
    @Autowired protected TaskRepository      taskRepository;
    @Autowired protected PasswordEncoder     passwordEncoder;

    // ─── Shared State ────────────────────────────────────────────
    protected MockMvc mockMvc;
    protected User    savedUser;
    protected User    savedAdmin;
    protected String  userToken;
    protected String  adminToken;

    @BeforeEach
    void setUpAll() throws Exception {
        // 1. بناء MockMvc مع الـ Security Filter Chain
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 2. إنشاء اليوزر العادي في H2
        User user = new User();
        user.setUsername(USER_USERNAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        user.setRole(Role.USER);
        savedUser = userRepository.save(user);

        // 3. إنشاء الأدمن في H2
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole(Role.ADMIN);
        savedAdmin = userRepository.save(admin);

        // 4. جلب JWT لكلٍّ منهم عبر Login endpoint حقيقي
        userToken  = fetchToken(USER_USERNAME,  USER_PASSWORD);
        adminToken = fetchToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    protected String fetchToken(String username, String password) throws Exception {
        // JSON مباشرة بدون الاعتماد على شكل Constructor الـ DTO
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