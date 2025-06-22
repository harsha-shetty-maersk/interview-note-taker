package com.interviewnotes.controller;

import com.interviewnotes.model.User;
import com.interviewnotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<UserDTO> getUsers(@RequestParam(value = "role", required = false) String role) {
        List<User> users;
        if (role != null) {
            users = userRepository.findByRole(User.UserRole.valueOf(role));
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(UserDTO::fromUser).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return UserDTO.fromUser(user);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest update) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (update.firstName != null) user.setFirstName(update.firstName);
        if (update.lastName != null) user.setLastName(update.lastName);
        if (update.email != null) user.setEmail(update.email);
        if (update.enabled != null) user.setEnabled(update.enabled);
        userRepository.save(user);
        return UserDTO.fromUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    public static class UserDTO {
        public Long id;
        public String username;
        public String email;
        public String firstName;
        public String lastName;
        public String role;
        public boolean enabled;
        public java.time.LocalDateTime createdAt;
        public java.time.LocalDateTime updatedAt;

        public static UserDTO fromUser(User user) {
            UserDTO dto = new UserDTO();
            dto.id = user.getId();
            dto.username = user.getUsername();
            dto.email = user.getEmail();
            dto.firstName = user.getFirstName();
            dto.lastName = user.getLastName();
            dto.role = user.getRole().name();
            dto.enabled = user.isEnabled();
            dto.createdAt = user.getCreatedAt();
            dto.updatedAt = user.getUpdatedAt();
            return dto;
        }
    }

    public static class UpdateUserRequest {
        public String firstName;
        public String lastName;
        public String email;
        public Boolean enabled;
    }
} 