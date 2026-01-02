package om.tanish.saas.tenant;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService ;

    //---------------CREATE------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDTO createTenant(@Valid @RequestBody CreateTenantRequest request) {

        Tenant savedTenant = tenantService.createTenant(request);
        return new TenantDTO(savedTenant);
    }
    // ---------------RETRIEVE----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/getAll")
    public List<TenantDTO> getAllTenants() {

        return tenantService.findAll().stream()
                .map(TenantDTO::new)
                .toList();
    }
    //----------------

    @GetMapping("/{key}")
    public TenantDTO getTenantByKey(@RequestParam String key){
        Tenant tenant = tenantService.getTenantByKey(key);
        return new TenantDTO(tenant);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("{key}")
    public TenantDTO updateTenant(
            @RequestParam String key,
            @RequestBody CreateTenantRequest request
    ){
        Tenant updatedTenant = tenantService.updateTenant(key, request);
        return new TenantDTO(updatedTenant);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("key")
    public void deleteTenant(
            @RequestParam String key
    ){
        tenantService.deleteTenant(key);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("{key}")
    public TenantDTO updateTenantStatus(
            @RequestParam String key,
            @RequestBody UpdateTenantStatusRequest request){
        Tenant tenant = tenantService.updateTenantStatus(key, request);
        return new TenantDTO(tenant);
    }
}
