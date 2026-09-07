package com.demo.security;

import com.demo.model.Group;
import com.demo.model.Permission;
import com.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private static final String TEST_SECRET = "SecretKeyForTestingJwtUtilsWhichIsAtLeast256BitsLongAndSecure!";
    private static final int TEST_EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken and extract details successfully")
    void generateToken_shouldCreateValidTokenWithSubjectAndPermissions() {
        // Arrange
        Permission perm = new Permission();
        perm.setId(1L);
        perm.setName("CREATE_TASK");

        Group group = new Group();
        group.setId(1L);
        group.setName("ADMIN");
        group.setPermissions(Set.of(perm));

        User user = new User("testuser", "test@demo.com", "password");
        user.setGroups(Set.of(group));

        // Act
        String token = jwtUtils.generateToken(user);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUsernameFromJwtToken(token)).isEqualTo("testuser");

        List<String> permissions = jwtUtils.getPermissionsFromToken(token);
        assertThat(permissions).contains("CREATE_TASK");
    }

    @Test
    @DisplayName("validateJwtToken should return false for invalid token string")
    void validateJwtToken_shouldReturnFalse_whenTokenIsMalformed() {
        // Act & Assert
        assertThat(jwtUtils.validateJwtToken("invalid.jwt.token")).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken should return false for expired token")
    void validateJwtToken_shouldReturnFalse_whenTokenIsExpired() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000); // Already expired
        User user = new User("expireduser", "expired@demo.com", "password");

        String token = jwtUtils.generateToken(user);

        // Act & Assert
        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }
}
