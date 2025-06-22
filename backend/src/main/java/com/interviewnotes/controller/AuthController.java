package com.interviewnotes.controller;

import com.interviewnotes.dto.AuthRequest;
import com.interviewnotes.dto.AuthResponse;
import com.interviewnotes.dto.RegisterRequest;
import com.interviewnotes.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Authenticate user and return JWT token.
     * 
     * @param authRequest the authentication request
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.authenticateUser(authRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Register a new user.
     * 
     * @param registerRequest the registration request
     * @return authentication response with JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new user and return JWT token")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user information.
     * 
     * @return current user information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            return ResponseEntity.ok(authService.getCurrentUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
    }
} 