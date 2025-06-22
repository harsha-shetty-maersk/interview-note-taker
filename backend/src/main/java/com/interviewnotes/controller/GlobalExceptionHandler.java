package com.interviewnotes.controller;

import jakarta.servlet.ServletException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<String> handleServletException(ServletException ex) {
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            return ResponseEntity.badRequest().body(cause.getMessage());
        }
        if (ex.getMessage() != null) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        return ResponseEntity.status(500).body("Internal server error");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(415).body(ex.getMessage());
    }
} 