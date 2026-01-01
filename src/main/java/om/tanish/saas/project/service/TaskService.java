package om.tanish.saas.project.service;

import om.tanish.saas.project.dto.CreateTaskRequest;
import om.tanish.saas.project.dto.TaskResponseDTO;
import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.entities.Task;
import om.tanish.saas.project.enums.TaskPriority;
import om.tanish.saas.project.enums.TaskStatus;
import om.tanish.saas.project.repository.ProjectRepository;
import om.tanish.saas.project.repository.TaskRepository;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       TenantRepository tenantRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public TaskResponseDTO createTask(CreateTaskRequest request){
        UUID tenantId = getTenantIdFromContext();
        UUID userId = getCurrentUserId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Tenant not found"
                ));
        Project project = projectRepository.findByIdAndTenant_Id(request.getProjectId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Project not found for the tenant"
                ));

        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Creator user not found"
                ));
        Task task = new Task();
        task.setTenant(tenant);
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreatedBy(createdBy);



        try{
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        }catch (IllegalArgumentException e){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus()
            );
        }

        try {
            task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
        }catch (IllegalArgumentException e){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid priority: " + request.getStatus()
            );
        }

        if(request.getAssigneeId() != null){
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Assignee user not found"
                    ));
            if(!assignee.getTenant().getId().equals(tenantId)){
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Assignee does not belong to this tenant"
                );
            }
            task.setAssignee(assignee);
        }
        task.setDueDate(request.getDueDate());
        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        Task savedTask = taskRepository.save(task);

        return mapToTaskResponseDTO(savedTask);
    }

    public List<TaskResponseDTO> getTasksByProject(UUID projectId) {
        UUID tenantId = getTenantIdFromContext();

        if (!projectRepository.existsByIdAndTenant_Id(projectId, tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Project not found"
            );
        }
        List<Task> taskList = taskRepository.findAllByProject_IdAndTenant_Id(projectId, tenantId);
        return taskList.stream()
                .map(this::mapToTaskResponseDTO)
                .toList();
    }

    public List<TaskResponseDTO> getMyTasks() {
        UUID tenantId = getTenantIdFromContext();
        UUID userId = getCurrentUserId();

        List<Task> taskList = taskRepository.findAllByAssignee_IdAndTenant_Id(userId, tenantId);

        return taskList.stream()
                .map(this::mapToTaskResponseDTO)
                .toList();
    }

    public TaskResponseDTO getTaskById(UUID taskId) {
        UUID tenantId = getTenantIdFromContext();

        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"
                ));
        return mapToTaskResponseDTO(task);
    }

    @Transactional
    public TaskResponseDTO updateTask(UUID taskId, CreateTaskRequest request){
        UUID tenantId = getTenantIdFromContext();
        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"
                ));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        try {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus()
            );
        }

        try {
            task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid priority: " + request.getPriority()
            );
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Assignee not found"
                    ));

            if (!assignee.getTenant().getId().equals(tenantId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Assignee does not belong to this tenant"
                );
            }
            task.setAssignee(assignee);
        }
        task.setDueDate(request.getDueDate());
        task.setUpdatedAt(Instant.now());
        Task savedTask = taskRepository.save(task);
        return mapToTaskResponseDTO(savedTask);
    }

    @Transactional
    public void deleteTask(UUID taskId){
        UUID tenantId = getTenantIdFromContext();
        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"
                ));
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponseDTO updateTaskStatus(UUID taskId, CreateTaskRequest request){
        UUID tenantId = getTenantIdFromContext();
        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"
                ));
        try {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus()
            );
        }
        Task savedTask = taskRepository.save(task);
        return mapToTaskResponseDTO(savedTask);
    }

    private UUID getTenantIdFromContext(){
        UUID tenantId = TenantContext.getTenant();
        if(tenantId == null){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tenant context not set"
            );
        }
        return tenantId;
    }

    private UUID getCurrentUserId(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof UUID){
            return (UUID) principal;
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "User not authenticated");
    }
    private TaskResponseDTO mapToTaskResponseDTO(Task task) {

        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getEmail() : null,
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }


}
