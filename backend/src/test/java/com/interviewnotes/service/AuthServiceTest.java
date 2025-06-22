package com.interviewnotes.service;

import com.interviewnotes.dto.AuthRequest;
import com.interviewnotes.dto.AuthResponse;
import com.interviewnotes.dto.RegisterRequest;
import com.interviewnotes.model.User;
import com.interviewnotes.repository.UserRepository;
import com.interviewnotes.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AuthService.
 * Tests authentication, registration, and user retrieval functionality.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private AuthRequest authRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(User.UserRole.INTERVIEWER);
        testUser.setEnabled(true);

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");

        // Only use lenient stubbing for securityContext.getAuthentication()
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testAuthenticateUser_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(jwtUtils.generateJwtToken(authentication))
                .thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.authenticateUser(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getRole().name(), response.getRole());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtils).generateJwtToken(authentication);
        verify(securityContext).setAuthentication(authentication);
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(authRequest);
        });
        
        assertEquals("User not found", exception.getMessage());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testAuthenticateUser_AuthenticationFailure() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(authRequest);
        });
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtils.generateTokenFromUsername("newuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getRole().name(), response.getRole());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtils).generateTokenFromUsername("newuser");
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertEquals("Username is already taken!", exception.getMessage());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertEquals("Email is already in use!", exception.getMessage());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_VerifyUserCreation() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });
        when(jwtUtils.generateTokenFromUsername("newuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.registerUser(registerRequest);

        // Assert
        verify(userRepository).save(argThat(user -> {
            assertEquals("newuser", user.getUsername());
            assertEquals("new@example.com", user.getEmail());
            assertEquals("encodedPassword", user.getPassword());
            assertEquals("Jane", user.getFirstName());
            assertEquals("Smith", user.getLastName());
            assertEquals(User.UserRole.INTERVIEWER, user.getRole());
            assertTrue(user.isEnabled());
            return true;
        }));
    }

    @Test
    void testGetCurrentUser_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        // Act
        User currentUser = authService.getCurrentUser();

        // Assert
        assertNotNull(currentUser);
        assertEquals(testUser, currentUser);
        
        verify(authentication).getName();
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser();
        });
        
        assertEquals("User not found", exception.getMessage());
        
        verify(authentication).getName();
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetCurrentUser_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            authService.getCurrentUser();
        });
        
        verify(securityContext).getAuthentication();
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void testRegisterUser_WithNullValues() {
        // Arrange
        registerRequest.setFirstName(null);
        registerRequest.setLastName(null);
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtils.generateTokenFromUsername("newuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(argThat(user -> {
            assertNull(user.getFirstName());
            assertNull(user.getLastName());
            return true;
        }));
    }

    @Test
    void testRegisterUser_WithEmptyStrings() {
        // Arrange
        registerRequest.setFirstName("");
        registerRequest.setLastName("");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtils.generateTokenFromUsername("newuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(argThat(user -> {
            assertEquals("", user.getFirstName());
            assertEquals("", user.getLastName());
            return true;
        }));
    }
} 