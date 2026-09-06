package com.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationEntryPoint Unit Tests")
class CustomAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private CustomAuthenticationEntryPoint entryPoint;

    @Test
    @DisplayName("commence sends 401 Unauthorized status")
    void commence_shouldSendUnauthorizedError() throws IOException {
        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
