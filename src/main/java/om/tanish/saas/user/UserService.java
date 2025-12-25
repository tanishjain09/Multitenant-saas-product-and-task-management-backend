package om.tanish.saas.user;

import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public UserService(UserRepository userRepository,
                       TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    public User createUser(CreateUserRequest request) {

        // 🔑 Tenant resolved automatically
        String tenantKey = TenantContext.getTenant();

        Tenant tenant = tenantRepository
                .findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));

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
        user.setPassword(request.getPassword()); // encryption comes in Phase 3
        user.setUsername(request.getUsername());
        user.setRole(UserRole.TENANT_ADMIN);
        user.setCreatedAt(Instant.now());

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        String tenantKey = TenantContext.getTenant();
        return userRepository.findAllByTenant_TenantKey(tenantKey);
    }
}