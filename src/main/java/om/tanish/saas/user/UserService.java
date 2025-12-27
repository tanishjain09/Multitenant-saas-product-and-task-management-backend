package om.tanish.saas.user;

import jakarta.transaction.Transactional;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;




    public UserService(UserRepository userRepository,
                       TenantRepository tenantRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public User createInitialUser(String tenantKey, CreateUserRequest request) {

        Tenant tenant = tenantRepository
                .findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));

        return createUserInternal(tenant, request);
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        System.out.println("calling this method");
        UUID tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant context missing"
            );
        }
        Tenant tenant = tenantRepository
                .findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));

        return createUserInternal(tenant, request);
    }
    private User createUserInternal(Tenant tenant, CreateUserRequest request){
        if (userRepository.existsByTenantAndEmail(tenant, request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists in this tenant"
            );
        }

        if (userRepository.existsByTenantAndUsername(tenant, request.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists in this tenant"
            );
        }


        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.getEmail());
        validatePassword(request.getPassword());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        user.setCreatedAt(Instant.now());

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        UUID tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Tenant context not set - authentication required"
            );
        }
        System.out.println("🔍 Getting users for tenant: " + tenantId);
        return userRepository.findAllByTenant_Id(tenantId);
    }

    private void validatePassword(String password){
        if(password.length() < 8){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must contain at least one digit");
        }
    }
}