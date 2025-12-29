package om.tanish.saas.project.repository;


import om.tanish.saas.project.entities.Task;
import om.tanish.saas.project.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
 public interface TaskRepository extends JpaRepository<Task, UUID> {

     List<Task> findAllByTenant_Id(UUID tenantId);

     List<Task> findAllByProject_IdAndTenant_Id(UUID projectId, UUID tenantId);

     List<Task> findAllByAssignee_IdAndTenant_Id(UUID assigneeId, UUID tenantId);

     Optional<Task> findByIdAndTenant_Id(UUID id, UUID tenantId);

     @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
     long countByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") TaskStatus status);

     boolean existsByIdAndTenant_Id(UUID id, UUID tenantId);
 }