package om.tanish.saas.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import om.tanish.saas.tenant.TenantContext;

import java.util.UUID;

@MappedSuperclass
public abstract class TenantAwareEntity {

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    protected void assignTenant() {
        UUID tenant = TenantContext.getTenant();
        if (tenant == null) {
            throw new IllegalStateException("TenantContext missing while persisting entity");
        }
        this.tenantId = tenant;
    }

    public UUID getTenantId() {
        return tenantId;
    }
}