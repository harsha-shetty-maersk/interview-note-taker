package com.interviewnotes.service;

import com.interviewnotes.model.User;
import com.interviewnotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.UserRole.INTERVIEWER);
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsernameAndEnabled("testuser", true)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        // Check authorities
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_INTERVIEWER")));

        verify(userRepository).findByUsernameAndEnabled("testuser", true);
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsernameAndEnabled("nonexistent", true)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent"));

        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepository).findByUsernameAndEnabled("nonexistent", true);
    }

    @Test
    void loadUserByUsername_DisabledUser_ReturnsDisabledUserDetails() {
        testUser.setEnabled(false);
        when(userRepository.findByUsernameAndEnabled("testuser", true)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("testuser"));

        assertEquals("User not found with username: testuser", exception.getMessage());
        verify(userRepository).findByUsernameAndEnabled("testuser", true);
    }

    @Test
    void loadUserByUsername_AdminUser_HasAdminRole() {
        testUser.setRole(User.UserRole.ADMIN);
        when(userRepository.findByUsernameAndEnabled("admin", true)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByUsernameAndEnabled("admin", true);
    }

    @Test
    void loadUserByUsername_HRManagerUser_HasHRRole() {
        testUser.setRole(User.UserRole.HR_MANAGER);
        when(userRepository.findByUsernameAndEnabled("hruser", true)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("hruser");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_HR_MANAGER")));

        verify(userRepository).findByUsernameAndEnabled("hruser", true);
    }

    @Test
    void loadUserByUsername_InterviewerUser_HasInterviewerRole() {
        testUser.setRole(User.UserRole.INTERVIEWER);
        when(userRepository.findByUsernameAndEnabled("interviewer", true)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("interviewer");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_INTERVIEWER")));

        verify(userRepository).findByUsernameAndEnabled("interviewer", true);
    }

    @Test
    void loadUserByUsername_NullUsername_ThrowsException() {
        when(userRepository.findByUsernameAndEnabled(null, true)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null));

        assertEquals("User not found with username: null", exception.getMessage());
        verify(userRepository).findByUsernameAndEnabled(null, true);
    }

    @Test
    void loadUserByUsername_EmptyUsername_ThrowsException() {
        when(userRepository.findByUsernameAndEnabled("", true)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(""));

        assertEquals("User not found with username: ", exception.getMessage());
        verify(userRepository).findByUsernameAndEnabled("", true);
    }
} 