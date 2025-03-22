package com.PortfolioHeatmap.security;

/**
 * A filter that intercepts HTTP requests to validate JWT tokens and set the authentication context.
 * This class extends OncePerRequestFilter to ensure it runs once per request, checking for a valid
 * JWT token in the Authorization header and authenticating the user with Spring Security.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtRequestFilter extends OncePerRequestFilter {
    // Dependencies for user details retrieval and JWT token validation.
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // Constructor for dependency injection of UserService and JwtUtil.
    public JwtRequestFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Filters each HTTP request to validate the JWT token and set the
    // authentication context.
    // Extracts the JWT token from the Authorization header, validates it, and sets
    // the authenticated user.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Extract the Authorization header from the request.
        final String authorizationHeader = request.getHeader("Authorization");

        // Variables to hold the username and JWT token.
        String username = null;
        String jwt = null;

        // Check if the Authorization header exists and starts with "Bearer ".
        // If present, extract the JWT token by removing the "Bearer " prefix.
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        // If a username is extracted and no authentication is currently set in the
        // SecurityContext,
        // validate the token and set the authentication.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details using the extracted username.
            UserDetails userDetails = userService.loadUserByUsername(username);
            // Validate the JWT token against the username.
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                // Create an authentication token with the user details and authorities.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // Set additional request details for the authentication token.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set the authentication in the SecurityContext for the current request.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continue with the filter chain to process the request.
        chain.doFilter(request, response);
    }
}