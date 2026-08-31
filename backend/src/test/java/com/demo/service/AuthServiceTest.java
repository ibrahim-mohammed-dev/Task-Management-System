package com.demo.service;

import com.demo.dto.LoginRequestDto;
import com.demo.dto.RegisterRequestDto;
import com.demo.model.User;
import com.demo.repository.UserRepository;
import com.demo.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * عزل تام: لا @SpringBootTest، كل الـ Dependencies (UserRepository, PasswordEncoder,
 * AuthenticationManager, JwtUtils) تم عمل Mock لها.
 *
 * افتراض: LoginRequestDto عندها getUsername()/getPassword() (كما يظهر من استخدامها في
 * AuthService الأصلي)، لذلك تم عمل Mock لها بدلاً من افتراض شكل الـ Constructor.
 * RegisterRequestDto افترضتها record بالشكل (username, email, password) لأنها تُستخدم
 * كـ dto.username()/dto.email()/dto.password() في الكود الأصلي.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private static final String USERNAME = "ahmed";
    private static final String EMAIL = "ahmed@test.com";
    private static final String RAW_PASSWORD = "P@ssw0rd";
    private static final String ENCODED_PASSWORD = "encoded-p@ssw0rd";

    // ============================================================
    // 1. register
    // ============================================================
    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Happy Path: ينشئ مستخدم جديد بنجاح لما اليوزرنيم والايميل غير مستخدمين")
        void register_shouldSaveNewUser_whenUsernameAndEmailAreAvailable() {
            // Arrange
            RegisterRequestDto dto = new RegisterRequestDto(USERNAME, EMAIL, RAW_PASSWORD);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            User savedUser = new User(USERNAME, EMAIL, ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            User result = authService.register(dto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Business Rule: يرمي Exception لو اسم المستخدم مستخدم بالفعل ولا يحفظ أي حاجة")
        void register_shouldThrowException_whenUsernameAlreadyTaken() {
            // Arrange
            RegisterRequestDto dto = new RegisterRequestDto(USERNAME, EMAIL, RAW_PASSWORD);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Username is already taken");

            verify(userRepository, never()).save(any(User.class));
            verify(userRepository, never()).existsByEmail(any());
        }

        @Test
        @DisplayName("Business Rule: يرمي Exception لو الايميل مسجل بالفعل ولا يحفظ أي حاجة")
        void register_shouldThrowException_whenEmailAlreadyRegistered() {
            // Arrange
            RegisterRequestDto dto = new RegisterRequestDto(USERNAME, EMAIL, RAW_PASSWORD);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email is already registered");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    // ============================================================
    // 2. login
    // ============================================================
    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Happy Path: يعمل Authenticate ويرجع JWT Token صحيح لبيانات صحيحة")
        void login_shouldReturnJwtToken_whenCredentialsAreValid() {
            // Arrange
            LoginRequestDto dto = mock(LoginRequestDto.class);
            when(dto.username()).thenReturn(USERNAME);
            when(dto.password()).thenReturn(RAW_PASSWORD);
            when(jwtUtils.generateToken(USERNAME)).thenReturn("mocked-jwt-token");

            // Act
            String token = authService.login(dto);

            // Assert
            assertThat(token).isEqualTo("mocked-jwt-token");
            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils, times(1)).generateToken(USERNAME);
        }

        @Test
        @DisplayName("Business Rule: يرمي Exception ولا يولّد Token لو بيانات الدخول غلط")
        void login_shouldThrowException_whenCredentialsAreInvalid() {
            // Arrange
            LoginRequestDto dto = mock(LoginRequestDto.class);
            when(dto.username()).thenReturn(USERNAME);
            when(dto.password()).thenReturn("wrong-password");
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BadCredentialsException.class);

            verify(jwtUtils, never()).generateToken(any());
        }
    }
}
