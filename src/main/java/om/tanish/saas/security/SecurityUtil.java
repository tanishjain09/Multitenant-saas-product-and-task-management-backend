package om.tanish.saas.security;

import om.tanish.saas.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static boolean isSuperAdmin() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    public static UUID requireTenant() {
        if (isSuperAdmin()) {
            return null; // explicitly allowed
        }

        UUID tenantId = TenantContext.getTenant();

        if (tenantId == null) {
            throw new IllegalStateException("TenantContext missing");
        }
        return tenantId;
    }
}
