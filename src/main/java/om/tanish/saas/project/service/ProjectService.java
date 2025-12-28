package om.tanish.saas.project;

import jakarta.transaction.Transactional;
import om.tanish.saas.project.dto.CreateProjectRequest;
import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.enums.ProjectStatus;
import om.tanish.saas.project.repository.ProjectRepository;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          TenantRepository tenantRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Project createProject(CreateProjectRequest request) {
        UUID tenantId = getTenantIdFromContext();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));

        Project project = new Project();
        project.setTenant(tenant);
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        try {
            project.setStatus(ProjectStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus()
            );
        }

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Owner not found"
                    ));

            if (!owner.getTenant().getId().equals(tenantId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Owner does not belong to this tenant"
                );
            }
            project.setOwner(owner);
        }

        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Instant now = Instant.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        return projectRepository.save(project);
    }

    public Page<Project> getAllProjects(Pageable pageable) {
        UUID tenantId = getTenantIdFromContext();
        return projectRepository.findAllByTenant_Id(tenantId);
    }

    public Project getProjectById(UUID projectId) {
        UUID tenantId = getTenantIdFromContext();
        return projectRepository.findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));
    }

    @Transactional
    public Project updateProject(UUID projectId, CreateProjectRequest request) {
        UUID tenantId = getTenantIdFromContext();

        Project project = projectRepository.findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        try {
            project.setStatus(ProjectStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus()
            );
        }

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Owner not found"
                    ));

            if (!owner.getTenant().getId().equals(tenantId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Owner does not belong to this tenant"
                );
            }
            project.setOwner(owner);
        }

        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedAt(Instant.now());

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        UUID tenantId = getTenantIdFromContext();

        Project project = projectRepository.findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));

        projectRepository.delete(project);
    }

    public List<Project> getProjectsByStatus(ProjectStatus status) {
        UUID tenantId = getTenantIdFromContext();
        return projectRepository.findAllByTenant_IdAndStatus(tenantId, status);
    }

    private UUID getTenantIdFromContext() {
        UUID tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant context not set"
            );
        }
        return tenantId;
    }
}