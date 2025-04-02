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

    // Constructor for dependency injection of UserRepository and PasswordEncoder.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    // Registers a new user with the given username and password.
    // Encodes the password using PasswordEncoder before saving the user to the
    // database.
    public User registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }
}