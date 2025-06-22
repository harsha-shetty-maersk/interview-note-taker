package com.interviewnotes.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    private AuthEntryPointJwt authEntryPointJwt;
    private ByteArrayOutputStream outputStream;
    private ServletOutputStream servletOutputStream;

    @BeforeEach
    void setUp() throws IOException {
        authEntryPointJwt = new AuthEntryPointJwt();
        outputStream = new ByteArrayOutputStream();
        servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                outputStream.write(b, off, len);
            }
            @Override
            public void write(byte[] b) throws IOException {
                outputStream.write(b);
            }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {}
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @Test
    void commence_ShouldSetUnauthorizedStatus() throws IOException, ServletException {
        // Given
        when(authException.getMessage()).thenReturn("Authentication failed");

        // When
        authEntryPointJwt.commence(request, response, authException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
    }

    @Test
    void commence_ShouldWriteErrorResponse() throws IOException, ServletException {
        // Given
        when(authException.getMessage()).thenReturn("Invalid credentials");

        // When
        authEntryPointJwt.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertThat(responseBody).contains("Unauthorized");
        assertThat(responseBody).contains("Invalid credentials");
    }

    @Test
    void commence_WithNullExceptionMessage_ShouldWriteDefaultMessage() throws IOException, ServletException {
        // Given
        when(authException.getMessage()).thenReturn(null);

        // When
        authEntryPointJwt.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertThat(responseBody).contains("Unauthorized");
        assertThat(responseBody).contains("null");
    }

    @Test
    void commence_WithEmptyExceptionMessage_ShouldWriteDefaultMessage() throws IOException, ServletException {
        // Given
        when(authException.getMessage()).thenReturn("");

        // When
        authEntryPointJwt.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertThat(responseBody).contains("Unauthorized");
        assertThat(responseBody).contains("\"\"");
    }

    @Test
    void commence_WithIOException_ShouldHandleException() throws IOException, ServletException {
        // Given
        when(response.getOutputStream()).thenThrow(new IOException("OutputStream error"));
        when(authException.getMessage()).thenReturn("Authentication failed");

        // When & Then
        try {
            authEntryPointJwt.commence(request, response, authException);
        } catch (IOException e) {
            // Expected to throw IOException when output stream fails
            assertThat(e.getMessage()).isEqualTo("OutputStream error");
        }
    }
} 