package com.interviewnotes.controller;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {
    @Test
    void testHandleServletException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletException ex = new ServletException("Test Servlet Exception");
        ResponseEntity<String> response = handler.handleServletException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Test Servlet Exception");
    }

    @Test
    void testHandleHttpMediaTypeNotSupported() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("application/xml");
        ResponseEntity<String> response = handler.handleHttpMediaTypeNotSupported(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).contains("application/xml");
    }

    @Test
    void testHandleRuntimeException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException ex = new RuntimeException("Test Runtime Exception");
        ResponseEntity<String> response = handler.handleRuntimeException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Test Runtime Exception");
    }

    @Test
    void testHandleServletException_WithNullMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletException ex = new ServletException();
        ResponseEntity<String> response = handler.handleServletException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testHandleHttpMediaTypeNotSupported_WithNullMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException((String) null);
        ResponseEntity<String> response = handler.handleHttpMediaTypeNotSupported(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testHandleRuntimeException_WithNullMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException ex = new RuntimeException();
        ResponseEntity<String> response = handler.handleRuntimeException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testHandleServletException_WithCauseAndMessage() {
        Throwable cause = new RuntimeException("Cause message");
        ServletException ex = new ServletException("Exception message", cause);
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleServletException(ex);
        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody()).contains("Cause message");
    }

    @Test
    void testHandleServletException_WithOnlyMessage() {
        ServletException ex = new ServletException("Only message");
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleServletException(ex);
        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody()).contains("Only message");
    }

    @Test
    void testHandleServletException_WithNeither() {
        ServletException ex = new ServletException();
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleServletException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Internal server error");
    }

    @Test
    void testHandleServletException_WithNullCauseAndMessage() {
        ServletException ex = new ServletException((String) null, null);
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleServletException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Internal server error");
    }
} 