
package om.tanish.saas.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findById(UUID id);
    boolean existsByTenantKey(String tenantKey);

    Optional<Tenant> findByTenantKey(String tenantKey);
}
