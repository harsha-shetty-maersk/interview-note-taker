package com.interviewnotes.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Test class for User entity.
 * Tests all getters, setters, constructors, and utility methods.
 */
class UserTest {

    private User user;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        user = new User();
        testDateTime = LocalDateTime.now();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertEquals(User.UserRole.INTERVIEWER, user.getRole());
        assertTrue(user.isEnabled());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void testParameterizedConstructor() {
        User testUser = new User("testuser", "test@example.com", "password123");
        
        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("password123", testUser.getPassword());
        assertEquals(User.UserRole.INTERVIEWER, testUser.getRole());
        assertTrue(testUser.isEnabled());
    }

    @Test
    void testIdGetterAndSetter() {
        Long id = 1L;
        user.setId(id);
        assertEquals(id, user.getId());
    }

    @Test
    void testUsernameGetterAndSetter() {
        String username = "testuser";
        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }

    @Test
    void testEmailGetterAndSetter() {
        String email = "test@example.com";
        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    @Test
    void testPasswordGetterAndSetter() {
        String password = "password123";
        user.setPassword(password);
        assertEquals(password, user.getPassword());
    }

    @Test
    void testFirstNameGetterAndSetter() {
        String firstName = "John";
        user.setFirstName(firstName);
        assertEquals(firstName, user.getFirstName());
    }

    @Test
    void testLastNameGetterAndSetter() {
        String lastName = "Doe";
        user.setLastName(lastName);
        assertEquals(lastName, user.getLastName());
    }

    @Test
    void testRoleGetterAndSetter() {
        // Test default role
        assertEquals(User.UserRole.INTERVIEWER, user.getRole());
        
        // Test setting different roles
        user.setRole(User.UserRole.ADMIN);
        assertEquals(User.UserRole.ADMIN, user.getRole());
        
        user.setRole(User.UserRole.HR_MANAGER);
        assertEquals(User.UserRole.HR_MANAGER, user.getRole());
        
        user.setRole(User.UserRole.INTERVIEWER);
        assertEquals(User.UserRole.INTERVIEWER, user.getRole());
    }

    @Test
    void testEnabledGetterAndSetter() {
        // Test default value
        assertTrue(user.isEnabled());
        
        // Test setting to false
        user.setEnabled(false);
        assertFalse(user.isEnabled());
        
        // Test setting back to true
        user.setEnabled(true);
        assertTrue(user.isEnabled());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        user.setCreatedAt(testDateTime);
        assertEquals(testDateTime, user.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        user.setUpdatedAt(testDateTime);
        assertEquals(testDateTime, user.getUpdatedAt());
    }

    @Test
    void testToString() {
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(User.UserRole.ADMIN);
        user.setEnabled(true);
        
        String result = user.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("email='test@example.com'"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.contains("role=ADMIN"));
        assertTrue(result.contains("enabled=true"));
    }

    @Test
    void testUserRoleEnum() {
        // Test all enum values
        assertEquals("ADMIN", User.UserRole.ADMIN.name());
        assertEquals("INTERVIEWER", User.UserRole.INTERVIEWER.name());
        assertEquals("HR_MANAGER", User.UserRole.HR_MANAGER.name());
        
        // Test enum ordinal
        assertEquals(0, User.UserRole.ADMIN.ordinal());
        assertEquals(1, User.UserRole.INTERVIEWER.ordinal());
        assertEquals(2, User.UserRole.HR_MANAGER.ordinal());
    }

    @Test
    void testNullValues() {
        // Test setting null values
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);
        
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void testEdgeCases() {
        // Test empty strings
        user.setUsername("");
        user.setEmail("");
        user.setPassword("");
        user.setFirstName("");
        user.setLastName("");
        
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
        
        // Test very long strings (within limits)
        String longString = "a".repeat(50);
        user.setUsername(longString);
        assertEquals(longString, user.getUsername());
    }
} 