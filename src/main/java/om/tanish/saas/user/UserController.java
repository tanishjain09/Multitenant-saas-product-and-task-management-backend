package om.tanish.saas.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =====================================================
    // REGISTER TENANT ADMIN (SUPER_ADMIN)
    // =====================================================
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/register-tenant-admin/{tenantKey}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO registerTenantAdmin(
            @PathVariable String tenantKey,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return new UserDTO(userService.registerTenantAdmin(tenantKey, request));
    }

    // =====================================================
    // GET ALL USERS
    // SUPER_ADMIN → global
    // TENANT_ADMIN → tenant
    // =====================================================
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN')")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserDTO::new)
                .toList();
    }

    // =====================================================

    // =====================================================
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN','USER')")
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id) {
        return new UserDTO(userService.getUserById(id));
    }

    // =====================================================
    // CREATE USER (TENANT_ADMIN)
    // =====================================================
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        return new UserDTO(userService.createUser(request));
    }

    // =====================================================
    // UPDATE USER (TENANT_ADMIN)
    // =====================================================
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PutMapping("/{id}")
    public UserDTO updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return new UserDTO(userService.updateUser(id, request));
    }

    // =====================================================
    // DELETE USER (TENANT_ADMIN)
    // =====================================================
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
