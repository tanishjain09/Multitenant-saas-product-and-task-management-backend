package om.tanish.saas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import om.tanish.saas.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // Token expiration: 1 hour
    private static final long TOKEN_VALIDITY_MS = 60 * 60 * 1000;

    @Value("${jwt.secret}")
    private String SECRET;

    private byte[] getSecretKey() {
        return SECRET.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(Map<String, Object> claims, User user) {
        long currentTime = System.currentTimeMillis();
        Date issuedAt = new Date(currentTime);
        Date expiresAt = new Date(currentTime + TOKEN_VALIDITY_MS);

        logger.debug("Generating token for user: {} with expiration: {}", user.getId(), expiresAt);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(Keys.hmacShaKeyFor(getSecretKey()))
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean expired = claims.getExpiration().before(new Date());
            if (expired) {
                logger.debug("Token expired at: {}", claims.getExpiration());
            }
            return expired;
        } catch (Exception e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean validateToken(String token, UUID userId) {
        try {
            Claims claims = extractClaims(token);
            UUID tokenUserId = UUID.fromString(claims.getSubject());
            boolean valid = tokenUserId.equals(userId) && !isTokenExpired(token);

            if (!valid) {
                logger.warn("Token validation failed for user: {}", userId);
            }

            return valid;
        } catch (JwtException e) {
            logger.error("Token validation error: {}", e.getMessage());
            throw e;
        }
    }
}