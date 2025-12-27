package om.tanish.saas.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTenantRequest {
    @NotBlank(message = "Tenant key is required")
    @Size(min = 3, max = 50)
    private String tenantKey;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100)
    private String name;

    public @NotBlank(message = "Tenant key is required") @Size(min = 3, max = 50) String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(@NotBlank(message = "Tenant key is required") @Size(min = 3, max = 50) String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public @NotBlank(message = "Name is required") @Size(min = 3, max = 100) String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name is required") @Size(min = 3, max = 100) String name) {
        this.name = name;
    }
}