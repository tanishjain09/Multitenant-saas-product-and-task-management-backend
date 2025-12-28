package om.tanish.saas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import om.tanish.saas.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET;

    private byte[] getSecretKey() {
        return SECRET.getBytes(StandardCharsets.UTF_8);
    }
    public String generateToken(Map claims, User user) {
        return Jwts.builder()
                .setClaims(claims)  // Add all claims at once
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
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
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    public boolean validateToken(String token, UUID userId) {
        try {
            Claims claims = extractClaims(token);
            UUID tokenUserId = UUID.fromString(claims.getSubject());
            return tokenUserId.equals(userId) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
