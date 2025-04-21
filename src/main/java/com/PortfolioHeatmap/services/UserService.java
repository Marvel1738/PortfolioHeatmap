package com.PortfolioHeatmap.services;

/**
 * Manages user-related operations, including user registration and authentication.
 * This service implements UserDetailsService for Spring Security to load user details during login
 * and provides methods for registering new users with encoded passwords.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.models.User;
import com.PortfolioHeatmap.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    // Repository for performing database operations on User entities.
    private final UserRepository userRepository;
    // PasswordEncoder for securely encoding user passwords during registration.
    private final PasswordEncoder passwordEncoder;
    private final PortfolioService portfolioService;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // Constructor for dependency injection of UserRepository, PasswordEncoder, and
    // PortfolioService.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PortfolioService portfolioService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.portfolioService = portfolioService;
    }

    // Loads user details by username for Spring Security authentication.
    // Throws UsernameNotFoundException if the user is not found.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }

    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    public String getUsernameByEmail(String email) {
        User user = loadUserByEmail(email);
        return user.getUsername();
    }

    // Registers a new user with the given username, email, and password.
    // Encodes the password using PasswordEncoder before saving the user to the
    // database.
    public User registerUser(String username, String email, String password) {
        log.info("Registering new user: {}", username);
        try {
            // Check if username or email already exists
            if (userRepository.findByUsername(username) != null) {
                log.error("Username already exists: {}", username);
                throw new RuntimeException("Username already exists");
            }
            if (userRepository.findByEmail(email) != null) {
                log.error("Email already exists: {}", email);
                throw new RuntimeException("Email already exists");
            }

            // Create and save the new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setIsGuest(false);

            newUser = userRepository.save(newUser);
            log.info("Created new user with ID: {}", newUser.getId());

            // Create the default portfolio
            try {
                portfolioService.createDefaultPortfolio(newUser.getId());
                log.info("Created default portfolio for new user: {}", newUser.getId());
            } catch (Exception e) {
                log.error("Failed to create default portfolio for new user {}: {}", newUser.getId(), e.getMessage());
                // Continue even if portfolio creation fails
            }

            return newUser;
        } catch (Exception e) {
            log.error("Failed to register user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }
}