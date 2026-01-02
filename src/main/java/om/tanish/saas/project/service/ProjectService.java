package om.tanish.saas.project.service;

import jakarta.transaction.Transactional;
import om.tanish.saas.project.dto.CreateProjectRequest;
import om.tanish.saas.project.dto.ProjectResponseDTO;
import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.enums.ProjectStatus;
import om.tanish.saas.project.repository.ProjectRepository;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponseDTO createProject(CreateProjectRequest request) {
        UUID tenantId = getTenantIdFromContext();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));

        Project project = new Project();
        project.setTenant(tenant);
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        setStatus(project, request.getStatus());
        setOwnerIfPresent(project, request.getOwnerId(), tenantId);

        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Instant now = Instant.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        Project saved = projectRepository.save(project);
        logger.info("Project created: {} for tenant: {}", saved.getId(), tenantId);

        return mapToDto(saved);
    }

    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable) {
        return projectRepository
                .findAllByTenant_Id(getTenantIdFromContext(), pageable)
                .map(this::mapToDto);
    }

    public ProjectResponseDTO getProjectById(UUID projectId) {
        Project project = projectRepository
                .findByIdAndTenant_Id(projectId, getTenantIdFromContext())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));

        return mapToDto(project);
    }

    public List<ProjectResponseDTO> getProjectsByStatus(ProjectStatus status) {
        return projectRepository
                .findAllByTenant_IdAndStatus(getTenantIdFromContext(), status)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public Page<ProjectResponseDTO> filterProjects(
            ProjectStatus status,
            UUID ownerId,
            Pageable pageable
    ) {
        UUID tenantId = getTenantIdFromContext();

        Page<Project> page;

        if (status != null && ownerId != null) {
            page = projectRepository
                    .findAllByTenant_IdAndStatusAndOwner_Id(
                            tenantId, status, ownerId, pageable);
        } else if (status != null) {
            page = projectRepository
                    .findAllByTenant_IdAndStatus(tenantId, status, pageable);
        } else if (ownerId != null) {
            page = projectRepository
                    .findAllByTenant_IdAndOwner_Id(tenantId, ownerId, pageable);
        } else {
            page = projectRepository
                    .findAllByTenant_Id(tenantId, pageable);
        }

        return page.map(this::mapToDto);
    }

    @Transactional
    public ProjectResponseDTO updateProject(UUID projectId, CreateProjectRequest request) {
        UUID tenantId = getTenantIdFromContext();

        Project project = projectRepository
                .findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        setStatus(project, request.getStatus());
        setOwnerIfPresent(project, request.getOwnerId(), tenantId);

        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedAt(Instant.now());

        Project updated = projectRepository.save(project);
        logger.info("Project updated: {} for tenant: {}", projectId, tenantId);

        return mapToDto(updated);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        UUID tenantId = getTenantIdFromContext();

        Project project = projectRepository
                .findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));

        projectRepository.delete(project);
        logger.info("Project deleted: {} for tenant: {}", projectId, tenantId);
    }

    private void setStatus(Project project, String status) {
        try {
            project.setStatus(ProjectStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + status
            );
        }
    }

    private void setOwnerIfPresent(Project project, UUID ownerId, UUID tenantId) {
        if (ownerId == null) return;

        User owner = userRepository.findById(ownerId)
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

    private UUID getTenantIdFromContext() {
        UUID tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant context not set"
            );
        }
        return tenantId;
    }
    private ProjectResponseDTO mapToDto(Project project) {
        User owner = project.getOwner();

        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                owner != null ? owner.getId() : null,
                owner != null ? owner.getEmail() : null
        );
    }
}