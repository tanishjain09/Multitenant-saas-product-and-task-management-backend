package om.tanish.saas.common;

import jakarta.validation.Valid;
import om.tanish.saas.security.JwtService;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.tenant.TenantStatus;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          TenantRepository tenantRepository,
                          RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tenantRepository = tenantRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt - Tenant Key: {}, Email: {}", request.getTenantKey(), request.getEmail());

        // 1. Validate input
        validateLoginRequest(request);

        // 2. Find tenant
        Tenant tenant = tenantRepository
                .findByTenantKey(request.getTenantKey().trim())
                .orElseThrow(() -> {
                    logger.warn("Tenant not found: {}", request.getTenantKey());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        logger.debug("Tenant found: {} (ID: {})", tenant.getName(), tenant.getId());

        // 3. Check tenant status
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            logger.warn("Inactive tenant login attempt: {}", tenant.getStatus());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant is not active");
        }

        // 4. Find user
        String sanitizedEmail = sanitizeEmail(request.getEmail());
        User user = userRepository
                .findByEmailAndTenantId(sanitizedEmail, tenant.getId())
                .orElseThrow(() -> {
                    logger.warn("User not found: {} for tenant: {}", request.getEmail(), tenant.getId());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        logger.debug("User found: {} (ID: {}), Role: {}", user.getUsername(), user.getId(), user.getRole());

        // 5. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Password mismatch for user: {}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // 6. Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenant.getId().toString());
        claims.put("role", user.getRole().toString());

        logger.debug("Generating token with claims - tenantId: {}, role: {}", tenant.getId(), user.getRole());

        // 7. Generate tokens
        String accessToken = jwtService.generateToken(claims, user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        logger.info("Login successful for user: {} (tenant: {})", user.getEmail(), tenant.getName());
        return new AuthResponse(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RequestTokenRequest request) {
        logger.info("Token refresh attempt");

        String requestRefreshToken = request.getRefreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    UUID userId = token.getUserId();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> {
                                logger.error("User not found for refresh token: {}", userId);
                                return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                            });

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("tenantId", user.getTenant().getId().toString());
                    claims.put("role", user.getRole().toString());

                    String newAccessToken = jwtService.generateToken(claims, user);
                    logger.info("Token refreshed for user: {}", user.getEmail());

                    return new AuthResponse(newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() -> {
                    logger.warn("Invalid refresh token attempt");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
                });
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestBody RequestTokenRequest request) {
        logger.info("Logout attempt");
        refreshTokenService.deleteByToken(request.getRefreshToken());
        logger.info("Logout successful");
        return Map.of("message", "Logged out successfully");
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request.getTenantKey() == null || request.getTenantKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant key is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private String sanitizeEmail(String email) {
        String sanitized = email.trim().toLowerCase();
        if (!sanitized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }
        return sanitized;
    }
}