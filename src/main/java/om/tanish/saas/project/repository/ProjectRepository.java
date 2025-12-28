package om.tanish.saas.project.repository;

import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByTenant_Id(UUID tenantId);
    Page<Project> findAllByTenant_Id(UUID tenantId, Pageable pageable);

    Optional<Project> findByIdAndTenant_Id(UUID id, UUID tenantId);

    List<Project> findAllByTenant_IdAndStatus(UUID tenantId, ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.tenant.id = :tenantId AND p.owner.id = :ownerId")
    List<Project> findAllByTenantIdAndOwnerId(@Param("tenantId") UUID tenantId, @Param("ownerId") UUID ownerId);

    boolean existsByIdAndTenant_Id(UUID id, UUID tenantId);

    Page<Project> findAllByTenant_IdAndStatus(UUID tenantId, ProjectStatus status, Pageable pageable);
    Page<Project> findAllByTenant_IdAndOwner_Id(UUID tenantId, UUID ownerId, Pageable pageable);
    Page<Project> findAllByTenant_IdAndStatusAndOwner_Id(UUID tenantId, ProjectStatus status, UUID ownerId, Pageable pageable);
}