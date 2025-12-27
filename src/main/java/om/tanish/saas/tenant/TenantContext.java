
package om.tanish.saas.tenant;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    public static void setTenant(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static UUID getTenant() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
