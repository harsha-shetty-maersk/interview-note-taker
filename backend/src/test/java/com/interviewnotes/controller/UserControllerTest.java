package com.interviewnotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.interviewnotes.model.User;
import com.interviewnotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private UserController.UserDTO testUserDTO;
    private UserController.UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.UserRole.INTERVIEWER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserDTO = UserController.UserDTO.fromUser(testUser);

        updateRequest = new UserController.UpdateUserRequest();
        updateRequest.firstName = "Updated";
        updateRequest.lastName = "Name";
        updateRequest.email = "updated@example.com";
        updateRequest.enabled = true;
    }

    @Test
    void getUsers_AllUsers_Success() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].firstName").value("Test"))
                .andExpect(jsonPath("$[0].lastName").value("User"))
                .andExpect(jsonPath("$[0].role").value("INTERVIEWER"))
                .andExpect(jsonPath("$[0].enabled").value(true));

        verify(userRepository).findAll();
        verify(userRepository, never()).findByRole(any(User.UserRole.class));
    }

    @Test
    void getUsers_ByRole_Success() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(User.UserRole.INTERVIEWER)).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .param("role", "INTERVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].role").value("INTERVIEWER"));

        verify(userRepository).findByRole(User.UserRole.INTERVIEWER);
        verify(userRepository, never()).findAll();
    }

    @Test
    void getUsers_ByAdminRole_Success() throws Exception {
        testUser.setRole(User.UserRole.ADMIN);
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(User.UserRole.ADMIN)).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ADMIN"));

        verify(userRepository).findByRole(User.UserRole.ADMIN);
    }

    @Test
    void getUsers_ByHRManagerRole_Success() throws Exception {
        testUser.setRole(User.UserRole.HR_MANAGER);
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(User.UserRole.HR_MANAGER)).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .param("role", "HR_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("HR_MANAGER"));

        verify(userRepository).findByRole(User.UserRole.HR_MANAGER);
    }

    @Test
    void getUsers_EmptyList_Success() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("INTERVIEWER"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound_ThrowsException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isBadRequest());

        verify(userRepository).findById(1L);
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NotFound_ThrowsException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_PartialUpdate_Success() throws Exception {
        UserController.UpdateUserRequest partialUpdate = new UserController.UpdateUserRequest();
        partialUpdate.firstName = "NewName";
        partialUpdate.enabled = false;

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_OnlyFirstName_Success() throws Exception {
        UserController.UpdateUserRequest firstNameOnly = new UserController.UpdateUserRequest();
        firstNameOnly.firstName = "NewName";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstNameOnly)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_OnlyLastName_Success() throws Exception {
        UserController.UpdateUserRequest lastNameOnly = new UserController.UpdateUserRequest();
        lastNameOnly.lastName = "NewLastName";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lastNameOnly)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_OnlyEmail_Success() throws Exception {
        UserController.UpdateUserRequest emailOnly = new UserController.UpdateUserRequest();
        emailOnly.email = "newemail@example.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailOnly)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_OnlyEnabled_Success() throws Exception {
        UserController.UpdateUserRequest enabledOnly = new UserController.UpdateUserRequest();
        enabledOnly.enabled = false;

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enabledOnly)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_EmptyRequest_Success() throws Exception {
        UserController.UpdateUserRequest emptyRequest = new UserController.UpdateUserRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WrongContentType_ReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userRepository).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistentUser_Success() throws Exception {
        doNothing().when(userRepository).deleteById(999L);

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isOk());

        verify(userRepository).deleteById(999L);
    }

    @Test
    void userDTO_FromUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(User.UserRole.ADMIN);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserController.UserDTO dto = UserController.UserDTO.fromUser(user);

        assertEquals(1L, dto.id);
        assertEquals("testuser", dto.username);
        assertEquals("test@example.com", dto.email);
        assertEquals("Test", dto.firstName);
        assertEquals("User", dto.lastName);
        assertEquals("ADMIN", dto.role);
        assertTrue(dto.enabled);
        assertNotNull(dto.createdAt);
        assertNotNull(dto.updatedAt);
    }

    @Test
    void userDTO_FromUserWithNullValues_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName(null);
        user.setLastName(null);
        user.setRole(User.UserRole.INTERVIEWER);
        user.setEnabled(false);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);

        UserController.UserDTO dto = UserController.UserDTO.fromUser(user);

        assertEquals(1L, dto.id);
        assertEquals("testuser", dto.username);
        assertEquals("test@example.com", dto.email);
        assertNull(dto.firstName);
        assertNull(dto.lastName);
        assertEquals("INTERVIEWER", dto.role);
        assertFalse(dto.enabled);
        assertNull(dto.createdAt);
        assertNull(dto.updatedAt);
    }
} 