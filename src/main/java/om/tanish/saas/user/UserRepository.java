package om.tanish.saas.user;

import om.tanish.saas.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantAndEmail(Tenant tenant, String email);
    boolean existsByTenantAndEmail(Tenant tenant, String email);

    boolean existsByTenantAndUsername(Tenant tenant, String username);

    List<User> findAllByTenant_TenantKey(String tenantKey);

}