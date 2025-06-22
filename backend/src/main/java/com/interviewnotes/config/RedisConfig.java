package com.interviewnotes.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Redis.
 * This configuration is only active when Redis is explicitly enabled.
 */
@Configuration
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisConfig {
    // Redis configuration will be added here when needed
} 