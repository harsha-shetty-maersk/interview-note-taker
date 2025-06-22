package com.interviewnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Interview Notes system.
 * 
 * This application provides a comprehensive interview note-taking system
 * with support for multiple interview rounds, candidate management,
 * and automated report generation.
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class InterviewNotesApplication {

    public static void main(String[] args) {
        // Force SecurityContext to use InheritableThreadLocal
        org.springframework.security.core.context.SecurityContextHolder.setStrategyName(
            org.springframework.security.core.context.SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        SpringApplication.run(InterviewNotesApplication.class, args);
    }
} 