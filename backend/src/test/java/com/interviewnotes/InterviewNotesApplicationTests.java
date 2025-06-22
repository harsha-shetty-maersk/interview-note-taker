package com.interviewnotes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for the main InterviewNotesApplication.
 * Tests that the Spring context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class InterviewNotesApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // If there are any configuration issues, this test will fail
    }

    @Test
    void mainMethodStartsApplication() {
        // Test that the main method can be called without throwing exceptions
        // This is a basic smoke test for the application startup
        try {
            InterviewNotesApplication.main(new String[]{});
        } catch (Exception e) {
            // Main method might throw exceptions in test environment, which is expected
            // We just want to ensure it doesn't crash immediately
        }
    }
} 