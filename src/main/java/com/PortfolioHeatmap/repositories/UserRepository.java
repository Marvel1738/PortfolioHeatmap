package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link User} entities.
 * Provides methods to interact with user data in the database.
 */

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}