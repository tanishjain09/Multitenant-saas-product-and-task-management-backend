package om.tanish.saas.tenant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant createTenant(@Valid @RequestBody CreateTenantRequest request) {

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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/getAll")
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
}