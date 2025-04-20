package com.PortfolioHeatmap.controllers;

/**
 * Handles authentication-related endpoints for user registration and login.
 * This controller integrates with Spring Security for authentication, UserService for user management,
 * and JwtUtil for generating JWT tokens used in securing API endpoints.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.security.JwtUtil;
import com.PortfolioHeatmap.services.UserService;
import com.PortfolioHeatmap.services.GuestUserService;
import com.PortfolioHeatmap.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // Dependencies for authentication, user management, and JWT generation.
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final GuestUserService guestUserService;

    // Constructor for dependency injection of AuthenticationManager, UserService,
    // and JwtUtil.
    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil,
            GuestUserService guestUserService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.guestUserService = guestUserService;
    }

    // Handles user registration via POST /auth/register.
    // Takes a JSON request body with username, email, and password, registers the
    // user
    // using UserService,
    // and returns a success message.
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok("User registered successfully");
    }

    // Handles user login via POST /auth/login.
    // Authenticates the user with Spring Security, generates a JWT token using
    // JwtUtil,
    // and returns the token for use in subsequent authenticated requests.
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try {
            // Get username from email
            String username = userService.getUsernameByEmail(request.getEmail());
            // Authenticate the user with the username and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            // Load user details for JWT generation
            final UserDetails userDetails = userService.loadUserByUsername(username);
            // Generate a JWT token for the authenticated user
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());
            // Return the JWT token in the response
            return ResponseEntity.ok(jwt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<String> createGuestToken() {
        try {
            // Create a new guest user
            User guestUser = guestUserService.createGuestUser();

            // Generate a JWT token for the guest user
            final String jwt = jwtUtil.generateToken(guestUser.getUsername());

            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create guest token: " + e.getMessage());
        }
    }
}

// A simple DTO (Data Transfer Object) class to hold username, email, and
// password
// for authentication requests (used in /auth/register and /auth/login).
class AuthRequest {
    // Username provided in the request.
    private String username;
    // Password provided in the request.
    private String password;
    // Email provided in the request.
    private String email;

    // Getter for username.
    public String getUsername() {
        return username;
    }

    // Setter for username.
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for password.
    public String getPassword() {
        return password;
    }

    // Setter for password.
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter for email.
    public String getEmail() {
        return email;
    }

    // Setter for email.
    public void setEmail(String email) {
        this.email = email;
    }
}