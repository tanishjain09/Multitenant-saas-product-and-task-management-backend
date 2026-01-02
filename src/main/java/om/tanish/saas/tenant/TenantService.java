package om.tanish.saas.tenant;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import java.util.List;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant createTenant(CreateTenantRequest request) {
        if(tenantRepository.existsByTenantKey(request.getTenantKey())) {
        throw new ResponseStatusException(
        HttpStatus.CONFLICT,
                    "Tenant with this Key already exists");
        }
        Tenant tenant = new Tenant();
        tenant.setTenantKey(request.getTenantKey());
        tenant.setName(request.getName());
        tenant.setStatus(TenantStatus.PENDING);
        tenant.setCreatedAt(Instant.now());
        return tenantRepository.save(tenant);
    }

    public Tenant getTenantByKey(String tenantKey){
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));
        return tenant;
    }

    @Transactional
    public Tenant updateTenant(String tenantKey, CreateTenantRequest request){
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant no found"
                ));

        tenant.setName(request.getName());
        tenant.setTenantKey(request.getTenantKey());
        return tenant;
    }

    //for super admin only
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public boolean existsByTenantKey(String tenantKey) {
        return tenantRepository.existsByTenantKey(tenantKey);
    }

    @Transactional
    public void deleteTenant(String tenantKey){
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));
        tenantRepository.deleteByTenantKey(tenantKey);
    }


    @Transactional
    public Tenant updateTenantStatus(String tenantKey, UpdateTenantStatusRequest request) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));
        tenant.setStatus(request.getTenantStatus());
        return tenant;
    }
}
