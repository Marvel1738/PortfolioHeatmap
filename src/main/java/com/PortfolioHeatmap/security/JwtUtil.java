package com.PortfolioHeatmap.security;

/**
 * Utility class for handling JWT (JSON Web Token) operations such as token generation, validation,
 * and username extraction. This class is used to secure API endpoints by creating and verifying JWT tokens.
 * 
 * @author [Marvel Bana]
 */
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // The secret key for signing JWT tokens, loaded from application properties.
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    // Token expiration time set to 10 hours (in milliseconds).
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    // Generates a SecretKey for signing JWT tokens using the SECRET_KEY.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generates a JWT token for the given username.
    // Sets the subject as the username, issued date as now, expiration time, and
    // signs the token.
    @SuppressWarnings("deprecation")
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Changed from .subject(username) to .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // Extracts the username from a given JWT token.
    // Parses the token using the signing key and retrieves the subject (username).
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validates a JWT token by checking if the extracted username matches the
    // provided username
    // and if the token has not expired.
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Checks if a JWT token has expired by comparing its expiration date with the
    // current date.
    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}