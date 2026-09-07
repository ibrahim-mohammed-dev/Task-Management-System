package com.demo.service;

import com.demo.dto.AuthResponseDto;
import com.demo.exception.TokenRefreshException;
import com.demo.model.RefreshToken;
import com.demo.model.User;
import com.demo.repository.RefreshTokenRepository;
import com.demo.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private static final long EXPIRATION_MS = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        testUser = new User("ahmed", "ahmed@test.com", "password");
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", EXPIRATION_MS);
    }

    @Nested
    @DisplayName("createRefreshToken")
    class CreateRefreshToken {

        @Test
        @DisplayName("Should generate raw token, save hashed token, and return raw token")
        void createRefreshToken_shouldSaveHashedTokenAndReturnRawToken() {
            // Act
            String rawToken = refreshTokenService.createRefreshToken(testUser);

            // Assert
            assertThat(rawToken).isNotNull().isNotEmpty();
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("verifyRefreshToken")
    class VerifyRefreshToken {

        @Test
        @DisplayName("Should return token when not expired")
        void verifyRefreshToken_shouldReturnToken_whenNotExpired() {
            // Arrange
            RefreshToken token = new RefreshToken(testUser, "hashed-token", Instant.now().plusSeconds(600));

            // Act
            RefreshToken result = refreshTokenService.verifyRefreshToken(token);

            // Assert
            assertThat(result).isSameAs(token);
            verify(refreshTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should delete token and throw TokenRefreshException when expired")
        void verifyRefreshToken_shouldDeleteAndThrowException_whenExpired() {
            // Arrange
            RefreshToken expiredToken = new RefreshToken(testUser, "hashed-token", Instant.now().minusSeconds(600));

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken(expiredToken))
                    .isInstanceOf(TokenRefreshException.class)
                    .hasMessageContaining("expired");

            verify(refreshTokenRepository, times(1)).delete(expiredToken);
        }
    }

    @Nested
    @DisplayName("rotateRefreshToken")
    class RotateRefreshToken {

        @Test
        @DisplayName("Should rotate token and return AuthResponseDto when valid token provided")
        void rotateRefreshToken_shouldRotateAndReturnAuthResponseDto_whenValid() {
            // Arrange
            String rawToken = "raw-refresh-token";
            RefreshToken existingToken = new RefreshToken(testUser, "some-hash", Instant.now().plusSeconds(600));

            when(refreshTokenRepository.findByHashedToken(anyString())).thenReturn(Optional.of(existingToken));
            when(jwtUtils.generateToken(testUser)).thenReturn("new-access-token");

            // Act
            AuthResponseDto response = refreshTokenService.rotateRefreshToken(rawToken);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();

            verify(refreshTokenRepository, times(1)).delete(existingToken);
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should throw TokenRefreshException when token hash is not found in DB")
        void rotateRefreshToken_shouldThrowException_whenTokenNotFound() {
            // Arrange
            when(refreshTokenRepository.findByHashedToken(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("unknown-token"))
                    .isInstanceOf(TokenRefreshException.class)
                    .hasMessageContaining("Invalid refresh token");

            verify(refreshTokenRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("delete Methods")
    class DeleteMethods {

        @Test
        @DisplayName("deleteByUser should invoke repository deleteByUser")
        void deleteByUser_shouldCallRepository() {
            // Arrange
            when(refreshTokenRepository.deleteByUser(testUser)).thenReturn(1);

            // Act
            int count = refreshTokenService.deleteByUser(testUser);

            // Assert
            assertThat(count).isEqualTo(1);
            verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
        }

        @Test
        @DisplayName("deleteByUsername should invoke repository deleteByUserUsername")
        void deleteByUsername_shouldCallRepository() {
            // Arrange
            when(refreshTokenRepository.deleteByUserUsername("ahmed")).thenReturn(1);

            // Act
            int count = refreshTokenService.deleteByUsername("ahmed");

            // Assert
            assertThat(count).isEqualTo(1);
            verify(refreshTokenRepository, times(1)).deleteByUserUsername("ahmed");
        }
    }
}
