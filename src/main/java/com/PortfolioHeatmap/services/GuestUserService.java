package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.User;
import com.PortfolioHeatmap.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class GuestUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortfolioService portfolioService;
    private static final Logger log = LoggerFactory.getLogger(GuestUserService.class);

    public GuestUserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PortfolioService portfolioService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.portfolioService = portfolioService;
    }

    public User createGuestUser() {
        log.info("Creating new guest user");
        try {
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

            guestUser = userRepository.save(guestUser);
            log.info("Created guest user with ID: {}", guestUser.getId());

            // Create the default portfolio
            try {
                portfolioService.createDefaultPortfolio(guestUser.getId());
                log.info("Created default portfolio for guest user: {}", guestUser.getId());
            } catch (Exception e) {
                log.error("Failed to create default portfolio for guest user {}: {}", guestUser.getId(),
                        e.getMessage());
                // Continue even if portfolio creation fails
            }

            return guestUser;
        } catch (Exception e) {
            log.error("Failed to create guest user: {}", e.getMessage());
            throw e;
        }
    }
}