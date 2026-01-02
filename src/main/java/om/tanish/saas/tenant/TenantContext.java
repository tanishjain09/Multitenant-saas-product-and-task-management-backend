
package om.tanish.saas.tenant;

import om.tanish.saas.common.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TenantContext {
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    //Prevent external clearing during request processing
    private static final ThreadLocal<Boolean> LOCKED = ThreadLocal.withInitial(() -> false);

    public static void setTenant(UUID tenantId) {
        TENANT_ID.set(tenantId);
        LOCKED.set(true);
    }

    public static UUID getTenant() {
        return TENANT_ID.get();
    }

    public static void clear() {
        if (!LOCKED.get()) {
            logger.warn("Attempted to clear unlocked tenant context");
            return;
        }
        TENANT_ID.remove();
        LOCKED.remove();
    }
}
