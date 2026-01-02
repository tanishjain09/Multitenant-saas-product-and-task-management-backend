
package om.tanish.saas.user;

import jakarta.validation.Valid;
import om.tanish.saas.tenant.TenantContext;
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

    // ----------------CREATE--------------------
    @PostMapping("/register")
    public UserDTO register(
            @RequestParam String tenantKey,
            @RequestBody CreateUserRequest request
    ) {
        User user = userService.register(tenantKey, request);
        return new UserDTO(user);
    }


    @PreAuthorize("hasAnyRole('TENANT_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    // --------------------Retrieve------------------------
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserDTO::new)
                .toList();
    }
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id){
        User user = userService.getUserById(id);
        return new UserDTO(user);
    }

    // ---------------------UPDATE-------------------
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PutMapping("/{id}")
    public UserDTO updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUserRequest request) {
        User updateUser = userService.updateUser(id, request);

        return new UserDTO(updateUser);
    }

    // -----------------DELETE---------------------------
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
    }

    //Debug
    @GetMapping("/debug/context")
    public String debugContext() {
        UUID tenantId = TenantContext.getTenant();
        return "Current Tenant Context: " + (tenantId != null ? tenantId : "NOT SET");
    }
}
