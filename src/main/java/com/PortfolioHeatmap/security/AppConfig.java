package com.PortfolioHeatmap.security;

/**
 * Provides application-wide configuration for Spring beans, specifically for security-related components.
 * This class defines a PasswordEncoder bean used for encoding user passwords during registration.
 * 
 * @author [Marvel Bana]
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    // Defines a PasswordEncoder bean for encoding user passwords.
    // Uses BCryptPasswordEncoder for secure password hashing.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}