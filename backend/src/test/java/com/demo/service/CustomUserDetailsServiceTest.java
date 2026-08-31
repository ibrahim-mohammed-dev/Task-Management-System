package com.demo.service;

import com.demo.model.User;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * عزل تام: لا @SpringBootTest، تم عمل Mock لـ UserRepository فقط.
 * User يُفترض أنه يُحقق (implements) UserDetails مباشرة كما هو موضح في الكود الأصلي.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String USERNAME = "ahmed";

    @Test
    @DisplayName("Happy Path: يرجع UserDetails صحيح لما اليوزرنيم موجود")
    void loadUserByUsername_shouldReturnUser_whenUsernameExists() {
        // Arrange
        User user = new User(USERNAME, "ahmed@test.com", "encoded-password");
        when(userRepository.findByUsername(USERNAME)).thenReturn(user);

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(user);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("Not Found: يرمي UsernameNotFoundException لما اليوزرنيم مش موجود")
    void loadUserByUsername_shouldThrowException_whenUsernameDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: " + USERNAME);
    }
}
