package om.tanish.saas.project.controller;

import jakarta.validation.Valid;
import om.tanish.saas.project.dto.CreateProjectRequest;
import om.tanish.saas.project.dto.ProjectResponseDTO;
import om.tanish.saas.project.enums.ProjectStatus;
import om.tanish.saas.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ProjectResponseDTO createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest){
        return projectService.createProject(createProjectRequest);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public Page<ProjectResponseDTO> getALlProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction)
    {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return projectService.getAllProjects(pageable);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public Page<ProjectResponseDTO> filterProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return projectService.filterProjects(status, ownerId, pageable);
    }


    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ProjectResponseDTO getProjectById(@PathVariable UUID projectId){
        return projectService.getProjectById(projectId);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public void deleteProject(@PathVariable UUID projectId){
        projectService.deleteProject(projectId);
    }
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public List<ProjectResponseDTO> getProjectByStatus(@PathVariable String status){
        try{
            ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
            return projectService.getProjectsByStatus(projectStatus);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }


}
