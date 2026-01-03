package om.tanish.saas.user;

import om.tanish.saas.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByTenantAndEmail(Tenant tenant, String email);

    boolean existsByTenantAndUsername(Tenant tenant, String username);

    List<User> findAllByTenant_Id(UUID tenantId);

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndTenantId(String email, UUID id);

    Optional<User> findByIdAndTenant_Id(UUID userId, UUID tenantId);

    void deleteByIdAndTenant_Id(UUID userId, UUID tenantId);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.role = :role")
    List<User> findAllByTenantIdAndRole(@Param("tenantId") UUID tenantId,
                                        @Param("role") UserRole role);

}