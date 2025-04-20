package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.User;
import com.PortfolioHeatmap.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class GuestUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GuestUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createGuestUser() {
        // Generate a unique guest username
        String guestUsername = "guest_" + UUID.randomUUID().toString().substring(0, 8);

        // Create a random password for the guest user
        String guestPassword = UUID.randomUUID().toString();

        // Create and save the guest user
        User guestUser = new User();
        guestUser.setUsername(guestUsername);
        guestUser.setEmail(guestUsername + "@guest.com");
        guestUser.setPassword(passwordEncoder.encode(guestPassword));
        guestUser.setIsGuest(true);

        return userRepository.save(guestUser);
    }
}