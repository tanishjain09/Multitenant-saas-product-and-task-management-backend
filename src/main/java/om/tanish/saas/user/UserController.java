
package om.tanish.saas.user;

import jakarta.validation.Valid;
import om.tanish.saas.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody CreateUserRequest request) { //the validation annotation we write in the CreateUserRequest class wont
        //work wihtout @Valid
        return userService.createUser(request);
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserDTO::new)
                .toList();
    }
    @PostMapping("/new")
    public User createInitialUser(
            @RequestParam String tenantKey,
            @RequestBody CreateUserRequest request
    ) {
        return userService.createInitialUser(tenantKey, request);
    }

    @GetMapping("/debug/context")
    public String debugContext() {
        UUID tenantId = TenantContext.getTenant();
        return "Current Tenant Context: " + (tenantId != null ? tenantId : "NOT SET");
    }
}
