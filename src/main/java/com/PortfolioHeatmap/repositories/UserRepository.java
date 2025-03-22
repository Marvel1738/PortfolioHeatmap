package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}