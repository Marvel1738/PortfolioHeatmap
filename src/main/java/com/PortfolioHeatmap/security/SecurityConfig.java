// package declaration for the security configuration
package com.PortfolioHeatmap.security;

/**
 * Configures Spring Security for the PortfolioHeatmap application.
 * Enables JWT-based authentication, defines security rules, disables CSRF,
 * enforces stateless session management, integrates JwtRequestFilter for token validation,
 * and sets up CORS to allow requests from the React frontend.
 * 
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.services.UserService; // Import UserService for user details retrieval

import java.util.Arrays; // Import Arrays for creating lists in CORS config

import org.springframework.context.annotation.Bean; // Import Bean annotation for defining Spring beans
import org.springframework.context.annotation.Configuration; // Import Configuration for marking this as a config class
import org.springframework.security.authentication.AuthenticationManager; // Import AuthenticationManager for login authentication
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Import for configuring authentication
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Import for configuring HTTP security
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Import to enable web security
import org.springframework.security.config.http.SessionCreationPolicy; // Import for stateless session policy
import org.springframework.security.web.SecurityFilterChain; // Import for defining the security filter chain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import for filter ordering
import org.springframework.web.cors.CorsConfiguration; // Import for CORS configuration
import org.springframework.web.cors.CorsConfigurationSource; // Import for CORS configuration source interface
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Import CORRECT UrlBasedCorsConfigurationSource for MVC

/**
 * SecurityConfig class to configure Spring Security and CORS for the
 * application.
 */
@Configuration // Marks this class as a Spring configuration class
@EnableWebSecurity // Enables Spring Security web features
public class SecurityConfig {
    // Dependency to fetch user details for authentication
    private final UserService userService;
    // Dependency to handle JWT token generation and validation
    private final JwtUtil jwtUtil;

    /**
     * Constructor for dependency injection of UserService and JwtUtil.
     * 
     * @param userService The service for retrieving user details
     * @param jwtUtil     The utility for handling JWT operations
     */
    public SecurityConfig(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService; // Assign injected UserService to field
        this.jwtUtil = jwtUtil; // Assign injected JwtUtil to field
    }

    /**
     * Configures the security filter chain for HTTP requests.
     * Disables CSRF, permits public access to /auth/** endpoints,
     * requires authentication for all other requests, sets stateless session
     * management,
     * applies CORS configuration, and adds JwtRequestFilter before
     * UsernamePasswordAuthenticationFilter.
     * 
     * @param http The HttpSecurity object to configure
     * @return SecurityFilterChain The configured filter chain
     * @throws Exception If configuration fails
     */
    @Bean // Registers this method as a Spring bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Explicitly apply CORS config
                .csrf(csrf -> csrf.disable()) // Disable CSRF as we're using JWT (stateless)
                .authorizeHttpRequests(auth -> auth // Configure request authorization
                        .requestMatchers("/auth/**", "/stocks/search").permitAll() // Allow unauthenticated access to
                                                                                   // /auth/** (e.g.,
                        // /auth/login)
                        .anyRequest().authenticated()) // Require authentication for all other requests
                .sessionManagement(session -> session // Configure session management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Use stateless sessions (JWT-based)

        // Add JwtRequestFilter before UsernamePasswordAuthenticationFilter to validate
        // tokens
        http.addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Build and return the configured filter chain
    }

    /**
     * Defines a bean for JwtRequestFilter to validate JWT tokens in incoming
     * requests.
     * 
     * @return JwtRequestFilter A new instance of JwtRequestFilter with dependencies
     */
    @Bean // Registers this method as a Spring bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter(userService, jwtUtil); // Create and return filter with injected dependencies
    }

    /**
     * Defines a bean for AuthenticationManager to handle user authentication during
     * login.
     * 
     * @param config The AuthenticationConfiguration to retrieve the manager
     * @return AuthenticationManager The configured authentication manager
     * @throws Exception If configuration fails
     */
    @Bean // Registers this method as a Spring bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // Retrieve and return the default AuthenticationManager
    }

    /**
     * Configures CORS to allow requests from the React frontend (localhost:3000).
     * Sets up allowed origins, methods, headers, and credentials for cross-origin
     * requests.
     * 
     * @return CorsConfigurationSource The configured CORS source for Spring
     *         Security
     */
    @Bean // Registers this method as a Spring bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Create a new CORS configuration object to define rules
        CorsConfiguration config = new CorsConfiguration();
        // Specify allowed origins (React frontend running on localhost:3000 and
        // production domain)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://theportfolioheatmap.com"));
        // Define allowed HTTP methods for frontend requests
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Allow all headers in requests (e.g., Authorization, Content-Type)
        config.setAllowedHeaders(Arrays.asList("*"));
        // Enable credentials (e.g., cookies, Authorization headers) to support JWT
        config.setAllowCredentials(true);

        // Create a URL-based CORS configuration source for applying rules to paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply the CORS configuration to all endpoints (/**)
        source.registerCorsConfiguration("/**", config);

        return source; // Return the configured source
    }
}