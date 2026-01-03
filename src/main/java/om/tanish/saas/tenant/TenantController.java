package om.tanish.saas.tenant;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDTO createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return new TenantDTO(tenantService.createTenant(request));
    }

    @GetMapping
    public List<TenantDTO> getAllTenants() {
        return tenantService.findAll()
                .stream()
                .map(TenantDTO::new)
                .toList();
    }

    @GetMapping("/{key}")
    public TenantDTO getTenantByKey(@PathVariable String key) {
        return new TenantDTO(tenantService.getTenantByKey(key));
    }

    @PutMapping("/{key}")
    public TenantDTO updateTenant(
            @PathVariable String key,
            @Valid @RequestBody CreateTenantRequest request
    ) {
        return new TenantDTO(tenantService.updateTenant(key, request));
    }

    @PutMapping("/{key}/status")
    public TenantDTO updateTenantStatus(
            @PathVariable String key,
            @Valid @RequestBody UpdateTenantStatusRequest request
    ) {
        return new TenantDTO(tenantService.updateTenantStatus(key, request));
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTenant(@PathVariable String key) {
        tenantService.deleteTenant(key);
    }
}
