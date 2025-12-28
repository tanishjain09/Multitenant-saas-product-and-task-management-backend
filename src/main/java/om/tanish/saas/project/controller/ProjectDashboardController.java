package om.tanish.saas.project.controller;

import om.tanish.saas.project.Task;
import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.enums.ProjectStatus;
import om.tanish.saas.project.enums.TaskPriority;
import om.tanish.saas.project.enums.TaskStatus;
import om.tanish.saas.project.repository.ProjectRepository;
import om.tanish.saas.project.repository.TaskRepository;
import om.tanish.saas.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class ProjectDashboardController {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectDashboardController(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public Map<String, Object> getDashboardOverview(){
        UUID tenantId = getTenantFromContext();

        List<Project> projects = projectRepository.findAllByTenant_Id(tenantId);
        List<Task> tasks = taskRepository.findAllByTenant_Id(tenantId);

        long activeProjects = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS)
                .count();

        long completedProjects = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
        long todoTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.TODO)
                .count();
        long inProgressTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        long blockedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.BLOCKED)
                .count();
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalProjects", projects.size());
        overview.put("activeProjects", activeProjects);
        overview.put("completedProjects", completedProjects);
        overview.put("totalTasks", totalTasks);
        overview.put("completedTasks", completedTasks);
        overview.put("todoTasks", todoTasks);
        overview.put("inProgressTasks", inProgressTasks);
        overview.put("blockedTasks", blockedTasks);

        if(totalTasks > 0){
            double completionRate = (completedTasks * 100.0) / totalTasks;
            overview.put("taskCompletionRate", String.format("%.2f%%", completionRate));
        }else {
            overview.put("taskCompletionRate", "0.00%");
        }
        return overview;
    }
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public Map<String, Object> getProjectStatistics(@PathVariable UUID projectId){
        UUID tenantId = getTenantFromContext();

        Project project = projectRepository.findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"
                ));
        List<Task> tasks = taskRepository.findAllByProject_IdAndTenant_Id(projectId, tenantId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        Map<String, Long> tasksByStatus = new HashMap<>();
        tasksByStatus.put("TODO", tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count());
        tasksByStatus.put("IN_PROGRESS", tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count());
        tasksByStatus.put("IN_REVIEW", tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_REVIEW).count());
        tasksByStatus.put("DONE", completedTasks);
        tasksByStatus.put("BLOCKED", tasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count());

        Map<String, Long> tasksByPriority = new HashMap<>();
        tasksByPriority.put("LOW", tasks.stream().filter(t -> t.getPriority() == TaskPriority.LOW).count());
        tasksByPriority.put("MEDIUM", tasks.stream().filter(t -> t.getPriority() == TaskPriority.MEDIUM).count());
        tasksByPriority.put("HIGH", tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count());
        tasksByPriority.put("URGENT", tasks.stream().filter(t -> t.getPriority() == TaskPriority.URGENT).count());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("projectName", project.getName());
        statistics.put("projectStatus", project.getStatus());
        statistics.put("totalTasks", totalTasks);
        statistics.put("completedTask", completedTasks);
        statistics.put("tasksByStatus", tasksByStatus);
        statistics.put("tasksByPriority", tasksByPriority);

        if (totalTasks > 0) {
            double completionRate = (completedTasks * 100.0) / totalTasks;
            statistics.put("completionRate", String.format("%.2f%%", completionRate));
        } else {
            statistics.put("completionRate", "0.00%");
        }

        return statistics;
    }

    private UUID getTenantFromContext(){
        UUID tenantId = TenantContext.getTenant();
        if(tenantId == null){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant context not set");
        }
        return tenantId;
    }
}
