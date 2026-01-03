package om.tanish.saas.user;

import jakarta.transaction.Transactional;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // SUPER_ADMIN → REGISTER TENANT ADMIN
    // =====================================================
    @Transactional
    public User registerTenantAdmin(String tenantKey, CreateUserRequest request) {

        if (!UserRole.TENANT_ADMIN.name().equalsIgnoreCase(request.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SUPER_ADMIN can only create TENANT_ADMIN"
            );
        }

        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Tenant not found"));

        return createUserInternal(tenant, request, UserRole.TENANT_ADMIN);
    }

    // =====================================================
    // GET ALL USERS
    // =====================================================
    public List<User> getAllUsers() {

        UserRole role = getCurrentRole();

        if (role == UserRole.SUPER_ADMIN) {
            return userRepository.findAll();
        }

        UUID tenantId = requireTenant();
        return userRepository.findAllByTenant_Id(tenantId);
    }

    // =====================================================
    // GET USER BY ID
    // =====================================================
    public User getUserById(UUID id) {

        UserRole role = getCurrentRole();

        if (role == UserRole.SUPER_ADMIN) {
            return userRepository.findById(id)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "User not found"));
        }

        UUID tenantId = requireTenant();
        User user = userRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        if (role == UserRole.USER) {
            UUID currentUserId = getCurrentUserId();
            if (!user.getId().equals(currentUserId)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Users can access only their own data");
            }
        }

        return user;
    }

    // =====================================================
    // TENANT_ADMIN → CREATE USER
    // =====================================================
    @Transactional
    public User createUser(CreateUserRequest request) {

        if (!UserRole.USER.name().equalsIgnoreCase(request.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "TENANT_ADMIN can only create USER"
            );
        }

        UUID tenantId = requireTenant();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Tenant not found"));

        return createUserInternal(tenant, request, UserRole.USER);
    }

    // =====================================================
    // UPDATE USER
    // =====================================================
    @Transactional
    public User updateUser(UUID userId, CreateUserRequest request)  {

        UUID tenantId = requireTenant();

        User user = userRepository.findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        if (request.getRole() != null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Role change is not allowed");
        }

        return userRepository.save(user);
    }

    // =====================================================
    // DELETE USER
    // =====================================================
    @Transactional
    public void deleteUser(UUID id) {

        UUID tenantId = requireTenant();

        User user = userRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
    }

    // =====================================================
    // INTERNAL HELPERS
    // =====================================================
    private User createUserInternal(
            Tenant tenant,
            CreateUserRequest request,
            UserRole role
    ) {

        if (userRepository.existsByTenantAndEmail(tenant, request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists in tenant");
        }

        if (userRepository.existsByTenantAndUsername(tenant, request.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists in tenant");
        }

        validatePassword(request.getPassword());

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setRole(String.valueOf(role));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(Instant.now());

        return userRepository.save(user);
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant context missing");
        }
        return tenantId;
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private UserRole getCurrentRole() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Role not found"
                        ));
    }

    private void validatePassword(String password) {
        if (password.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters");
        if (!password.matches(".*[A-Z].*"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain uppercase letter");
        if (!password.matches(".*[a-z].*"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain lowercase letter");
        if (!password.matches(".*\\d.*"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain digit");
    }
}
