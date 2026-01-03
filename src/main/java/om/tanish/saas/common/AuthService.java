package om.tanish.saas.common;

import om.tanish.saas.security.JwtService;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.tenant.TenantStatus;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // ========================= LOGIN =========================

    public AuthResponse login(LoginRequest request) {

        logger.info("Login attempt for email: {}", request.getEmail());

        validateBasicLoginRequest(request);
        String email = sanitizeEmail(request.getEmail());

        // --------- SUPER ADMIN LOGIN ---------
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if ("SUPER_ADMIN".equals(user.getRole().toString())) {

            if (request.getTenantKey() != null && !request.getTenantKey().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "SUPER_ADMIN must not provide tenant key"
                );
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().toString());

            String accessToken = jwtService.generateToken(claims, user);
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            logger.info("SUPER_ADMIN login successful: {}", user.getEmail());
            return new AuthResponse(accessToken, refreshToken);
        }

        // --------- TENANT USER LOGIN ---------
        if (request.getTenantKey() == null || request.getTenantKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tenant key is required");
        }

        Tenant tenant = tenantRepository
                .findByTenantKey(request.getTenantKey().trim())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant is not active");
        }

        // Tenant-aware user lookup (IMPORTANT)
        user = userRepository
                .findByEmailAndTenant_TenantKey(email, request.getTenantKey().trim())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenant.getId().toString());
        claims.put("role", user.getRole().toString());

        String accessToken = jwtService.generateToken(claims, user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        logger.info("Login successful for user: {} (tenant: {})",
                user.getEmail(), tenant.getName());

        return new AuthResponse(accessToken, refreshToken);
    }

    // ========================= REFRESH TOKEN =========================

    public AuthResponse refresh(RequestTokenRequest request) {

        logger.info("Token refresh attempt");

        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {

                    UUID userId = refreshToken.getUserId();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND, "User not found"));

                    Map<String, Object> claims = new HashMap<>();

                    if (user.getTenant() != null) {
                        claims.put("tenantId", user.getTenant().getId().toString());
                    }

                    claims.put("role", user.getRole().toString());

                    String newAccessToken =
                            jwtService.generateToken(claims, user);

                    logger.info("Token refreshed for user: {}", user.getEmail());

                    return new AuthResponse(newAccessToken, request.getRefreshToken());
                })
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
    }

    // ========================= LOGOUT =========================

    public Map<String, String> logout(RequestTokenRequest request) {

        logger.info("Logout attempt");

        refreshTokenService.deleteByToken(request.getRefreshToken());

        logger.info("Logout successful");
        return Map.of("message", "Logged out successfully");
    }

    private void validateBasicLoginRequest(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private String sanitizeEmail(String email) {

        String sanitized = email.trim().toLowerCase();

        if (!sanitized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        return sanitized;
    }
}
