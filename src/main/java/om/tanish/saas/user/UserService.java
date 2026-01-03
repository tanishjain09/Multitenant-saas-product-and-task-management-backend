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
    public User register(String tenantKey, CreateUserRequest request) {

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
        UserRole role = UserRole.valueOf(request.getRole().trim().toUpperCase());

        if(role == UserRole.SUPER_ADMIN){
            if(tenantId != null){
                throw new IllegalStateException(
                        "SUPER_ADMIN must not be associated with a tenant"
                );
            }
        }else{
            if(tenantId == null){
                throw new IllegalStateException(
                        "SUPER_ADMIN must not be associated with a tenant"
                );
            }
        }
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

        return userRepository.findAll();
    }

    public User getUserById(UUID id){
        return userRepository.findByIdAndTenant_Id(id, TenantContext.getTenant())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot get user by id"
                ));

    }
    @Transactional
    public User updateUser(UUID  userId, CreateUserRequest request){
        UUID tenantId = TenantContext.getTenant();

        User user = userRepository.findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot get user by get"
                ));
        if(request.getUsername() != null){
            user.setUsername(request.getUsername());
        }
        if(request.getEmail() != null){
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id){
        UUID tenantId = TenantContext.getTenant();
         User user = userRepository.findById(id)
                 .orElseThrow(() -> new ResponseStatusException(
                         HttpStatus.NOT_FOUND, "User not found"
                 ));
         userRepository.deleteByIdAndTenant_Id(id, tenantId);
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