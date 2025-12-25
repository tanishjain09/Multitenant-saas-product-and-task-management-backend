package om.tanish.saas.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;
    /**
     * TEMP API
     * Create a tenant (company)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant createTenant(@RequestBody Tenant request) {

        if(tenantRepository.existsByTenantKey(request.getTenantKey())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tenant with this Key already exists");
        }
        Tenant tenant = new Tenant();
        tenant.setTenantKey(request.getTenantKey());
        tenant.setName(request.getName());
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setCreatedAt(Instant.now());
        return tenantRepository.save(tenant);
    }

    /**
     * TEMP API
     * Get all tenants
     */
    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
}