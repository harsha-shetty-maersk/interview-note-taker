package com.interviewnotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewnotes.dto.AuthRequest;
import com.interviewnotes.dto.AuthResponse;
import com.interviewnotes.dto.RegisterRequest;
import com.interviewnotes.model.User;
import com.interviewnotes.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthController.
 * Tests all authentication endpoints and their responses.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private AuthRequest authRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(User.UserRole.INTERVIEWER);

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");

        authResponse = new AuthResponse("jwt-token", testUser);
    }

    @Test
    void testAuthenticateUser_Success() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("INTERVIEWER"));

        verify(authService).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthenticateUser_InvalidRequest() throws Exception {
        // Arrange
        AuthRequest invalidRequest = new AuthRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticateUser(any());
    }

    @Test
    void testAuthenticateUser_ServiceException() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Authentication failed"));

        verify(authService).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("INTERVIEWER"));

        verify(authService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testRegisterUser_InvalidRequest() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any());
    }

    @Test
    void testRegisterUser_ServiceException() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Registration failed"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Registration failed"));

        verify(authService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("INTERVIEWER"));

        verify(authService).getCurrentUser();
    }

    @Test
    void testGetCurrentUser_UserNotAuthenticated() throws Exception {
        // Arrange
        when(authService.getCurrentUser())
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not authenticated"));

        verify(authService).getCurrentUser();
    }

    @Test
    void testGetCurrentUser_GenericException() throws Exception {
        // Arrange
        when(authService.getCurrentUser())
                .thenThrow(new RuntimeException("Some other error"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not authenticated"));

        verify(authService).getCurrentUser();
    }

    @Test
    void testAuthenticateUser_WithNullValues() throws Exception {
        // Arrange
        authRequest.setUsername(null);
        authRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticateUser(any());
    }

    @Test
    void testRegisterUser_WithNullValues() throws Exception {
        // Arrange
        registerRequest.setUsername(null);
        registerRequest.setEmail(null);
        registerRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any());
    }

    @Test
    void testAuthenticateUser_WithEmptyStrings() throws Exception {
        // Arrange
        authRequest.setUsername("");
        authRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticateUser(any());
    }

    @Test
    void testRegisterUser_WithEmptyStrings() throws Exception {
        // Arrange
        registerRequest.setUsername("");
        registerRequest.setEmail("");
        registerRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any());
    }

    @Test
    void testAuthenticateUser_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticateUser(any());
    }

    @Test
    void testRegisterUser_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any());
    }

    @Test
    void testAuthenticateUser_WrongContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content("some text"))
                .andExpect(status().isUnsupportedMediaType());

        verify(authService, never()).authenticateUser(any());
    }

    @Test
    void testRegisterUser_WrongContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.TEXT_PLAIN)
                .content("some text"))
                .andExpect(status().isUnsupportedMediaType());

        verify(authService, never()).registerUser(any());
    }
} 