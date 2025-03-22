package com.PortfolioHeatmap.security;

/**
 * Configures Spring Security for the application, enabling JWT-based authentication.
 * This class sets up security rules, disables CSRF, enforces stateless session management,
 * and integrates the JwtRequestFilter for token validation.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Dependencies for user details retrieval and JWT token handling.
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // Constructor for dependency injection of UserService and JwtUtil.
    public SecurityConfig(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Configures the security filter chain for HTTP requests.
    // Disables CSRF, permits /auth/** endpoints, requires authentication for all
    // other requests,
    // sets stateless session management, and adds the JwtRequestFilter.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Defines a bean for the JwtRequestFilter, which validates JWT tokens in
    // incoming requests.
    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter(userService, jwtUtil);
    }

    // Defines a bean for the AuthenticationManager, used for authenticating users
    // during login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}