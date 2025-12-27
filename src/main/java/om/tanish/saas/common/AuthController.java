package om.tanish.saas.common;

import jakarta.validation.Valid;
import om.tanish.saas.config.JwtService;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.tenant.TenantStatus;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {

        System.out.println("🔐 Login attempt:");
        System.out.println("   Tenant Key: " + request.getTenantKey());
        System.out.println("   Email: " + request.getEmail());

        //1. Validate input
        if (request.getTenantKey() == null || request.getTenantKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tenant key is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is required");
        }

        //2. Find tenant - TRIM whitespaces
        Tenant tenant = tenantRepository
                .findByTenantKey(request.getTenantKey().trim())
                .orElseThrow(() -> {
                    System.out.println("Tenant not found: " + request.getTenantKey());
                    return new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "Invalid Credential");
                });

        System.out.println("✅ Tenant found: " + tenant.getName() + " (ID: " + tenant.getId() + ")");


        //3. Check tenant status
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            System.out.println("Tenant is not active: " + tenant.getStatus());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant is not active");
        }

        //4. Find user - trim email

        String sanitizedEmail = request.getEmail().trim().toLowerCase();
        if (!sanitizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid email format");
        }
        User user = userRepository
                .findByEmailAndTenantId(sanitizedEmail, tenant.getId())
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + request.getEmail() + " for tenant: " + tenant.getId());
                    return new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials User not fount");
                });

        System.out.println("✅ User found: " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("   User Role: " + user.getRole());
        System.out.println("   User Tenant: " + user.getTenant().getId());

        // 5. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("Password mismatch");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials : password different");
        }
        //6. Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenant.getId().toString());
        claims.put("role", user.getRole().toString());

        System.out.println("🎫 Generating token with claims:");
        System.out.println("   tenantId: " + tenant.getId());
        System.out.println("   role: " + user.getRole());

        //7. Generate token
        String token = jwtService.generateToken(claims, user);

        System.out.println("✅ Token generated successfully");
        System.out.println("   Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");

        return new AuthResponse(token);
    }
}