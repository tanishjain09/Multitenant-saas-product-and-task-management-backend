package om.tanish.saas.project.repository;

import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"owner", "tenant"})
    Page<Project> findAllByTenant_Id(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "tenant"})
    Optional<Project> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByIdAndTenant_Id(UUID id, UUID tenantId);

    // Status-based queries
    List<Project> findAllByTenant_IdAndStatus(UUID tenantId, ProjectStatus status);

    Page<Project> findAllByTenant_IdAndStatus(
            UUID tenantId,
            ProjectStatus status,
            Pageable pageable
    );

    // Owner-based queries
    @Query("SELECT p FROM Project p WHERE p.tenant.id = :tenantId AND p.owner.id = :ownerId")
    List<Project> findAllByTenantIdAndOwnerId(
            @Param("tenantId") UUID tenantId,
            @Param("ownerId") UUID ownerId
    );

    Page<Project> findAllByTenant_IdAndOwner_Id(
            UUID tenantId,
            UUID ownerId,
            Pageable pageable
    );

    // Combined filters for filterProjects method
    Page<Project> findAllByTenant_IdAndStatusAndOwner_Id(
            UUID tenantId,
            ProjectStatus status,
            UUID ownerId,
            Pageable pageable
    );

    // Additional useful queries
    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenant.id = :tenantId AND p.status = :status")
    long countByTenantIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("status") ProjectStatus status
    );

    @Query("SELECT p FROM Project p WHERE p.tenant.id = :tenantId " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Project> searchProjects(
            @Param("tenantId") UUID tenantId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    Object findByIdAndTenantId(UUID testProjectId, UUID testTenantId);
}