package om.tanish.saas.tenant;

import java.time.Instant;
import java.util.UUID;

public class TenantDTO {

    private String tenantKey;
    private String name;
    private TenantStatus status;
    private Instant createdAt;

    public TenantDTO(Tenant tenant) {
        this.name = tenant.getName();
        this.status = tenant.getStatus();
        this.tenantKey = tenant.getTenantKey();
        this.createdAt = tenant.getCreatedAt();
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
