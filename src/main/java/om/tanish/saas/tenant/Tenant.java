package om.tanish.saas.tenant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import om.tanish.saas.common.AuditableEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants",
        uniqueConstraints = {@UniqueConstraint(columnNames = "tenant_key")})
public class Tenant extends AuditableEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_key", nullable = false, updatable = false)
    @NotBlank
    @Size(min = 3, max = 50)
    private String tenantKey;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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


}