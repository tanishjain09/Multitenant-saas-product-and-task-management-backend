package om.tanish.saas.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class TenantContext {

    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenant(UUID tenantId) {
        TENANT_ID.set(tenantId);
        logger.debug("TenantContext set to {}", tenantId);
    }

    public static UUID getTenant() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        logger.debug("TenantContext cleared");
    }
}
