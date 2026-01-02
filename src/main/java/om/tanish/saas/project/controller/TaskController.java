package om.tanish.saas.project.controller;

import jakarta.validation.Valid;

import om.tanish.saas.project.dto.CreateTaskRequest;
import om.tanish.saas.project.dto.TaskResponseDTO;
import om.tanish.saas.project.entities.Task;
import om.tanish.saas.project.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public TaskResponseDTO createTask(@Valid @RequestBody CreateTaskRequest request){
        return taskService.createTask(request);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public List<TaskResponseDTO> getTasksByProjectId(@PathVariable UUID projectId){
        return taskService.getTasksByProject(projectId);
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public List<TaskResponseDTO> getMyTasks() {
        return taskService.getMyTasks();
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public TaskResponseDTO updateTask(@PathVariable UUID taskId,
                                      @Valid @RequestBody CreateTaskRequest request){
        return taskService.updateTask(taskId, request);
    }

    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public TaskResponseDTO updateTaskStatus(@PathVariable UUID taskId,
                                            @Valid @RequestBody CreateTaskRequest request){
        return taskService.updateTaskStatus(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public void deleteTask(@PathVariable UUID taskId){
        taskService.deleteTask(taskId);
    }
}
