package om.tanish.saas.project.service;

import om.tanish.saas.project.entities.Task;
import om.tanish.saas.project.dto.CreateTaskRequest;
import om.tanish.saas.project.entities.Project;
import om.tanish.saas.project.enums.TaskStatus;
import om.tanish.saas.project.repository.ProjectRepository;
import om.tanish.saas.project.repository.TaskRepository;
import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.tenant.TenantStatus;
import om.tanish.saas.user.User;
import om.tanish.saas.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Tenant testTenant;
    private UUID testTenantId;
    private UUID testUserId;
    private UUID testProjectId;
    private User testUser;
    private Project testProject;
    private CreateTaskRequest validRequest;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();

        testTenant = new Tenant();
        testTenant.setId(testTenantId);
        testTenant.setTenantKey("test-tenant");
        testTenant.setName("Test Tenant");
        testTenant.setStatus(TenantStatus.ACTIVE);

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setRole("USER");
        testUser.setTenant(testTenant);

        testProject = new Project();
        testProject.setId(testProjectId);
        testProject.setName("Test Project");
        testProject.setTenant(testTenant);

        validRequest = new CreateTaskRequest();
        validRequest.setProjectId(testProjectId);
        validRequest.setTitle("Test Task");
        validRequest.setDescription("Test Description");
        validRequest.setStatus("TODO");
        validRequest.setPriority("MEDIUM");
        validRequest.setDueDate(Instant.now().plusSeconds(86400));

        // Set security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        testUserId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Task savedTask = new Task();
        savedTask.setId(UUID.randomUUID());
        savedTask.setTitle(validRequest.getTitle());
        savedTask.setStatus(TaskStatus.TODO);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRequest.getTitle(), result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_ProjectNotFound_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Project not found"));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WithAssignee_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        UUID assigneeId = UUID.randomUUID();
        validRequest.setAssigneeId(assigneeId);

        User assignee = new User();
        assignee.setId(assigneeId);
        assignee.setTenant(testTenant);

        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));

        Task savedTask = new Task();
        savedTask.setId(UUID.randomUUID());
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(validRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(assigneeId);
    }

    @Test
    void createTask_AssigneeFromDifferentTenant_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        UUID assigneeId = UUID.randomUUID();
        validRequest.setAssigneeId(assigneeId);

        Tenant differentTenant = new Tenant();
        differentTenant.setId(UUID.randomUUID());

        User assignee = new User();
        assignee.setId(assigneeId);
        assignee.setTenant(differentTenant);

        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(validRequest)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("does not belong to this tenant"));
    }

    @Test
    void createTask_InvalidStatus_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        validRequest.setStatus("INVALID_STATUS");

        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid status"));
    }

    @Test
    void getTasksByProject_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(projectRepository.existsByIdAndTenant_Id(testProjectId, testTenantId))
                .thenReturn(true);

        Task task1 = new Task();
        task1.setId(UUID.randomUUID());
        Task task2 = new Task();
        task2.setId(UUID.randomUUID());

        when(taskRepository.findAllByProjectIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Arrays.asList(task1, task2));

        // Act
        List<Task> result = taskService.getTasksByProject(testProjectId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAllByProjectIdAndTenantId(testProjectId, testTenantId);
    }

    @Test
    void getMyTasks_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        Task task1 = new Task();
        task1.setId(UUID.randomUUID());

        when(taskRepository.findAllByAssignee_IdAndTenant_Id(testUserId, testTenantId))
                .thenReturn(List.of(task1));

        // Act
        List<Task> result = taskService.getMyTasks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByAssignee_IdAndTenant_Id(testUserId, testTenantId);
    }

    @Test
    void updateTask_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        UUID taskId = UUID.randomUUID();

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setTenant(testTenant);

        when(taskRepository.findByIdAndTenant_Id(taskId, testTenantId))
                .thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.updateTask(taskId, validRequest);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_TaskNotFound_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findByIdAndTenant_Id(taskId, testTenantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(taskId, validRequest)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Task not found"));
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        UUID taskId = UUID.randomUUID();

        Task existingTask = new Task();
        existingTask.setId(taskId);

        when(taskRepository.findByIdAndTenant_Id(taskId, testTenantId))
                .thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).delete(existingTask);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    void updateTaskStatus_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        UUID taskId = UUID.randomUUID();

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setStatus(TaskStatus.TODO);

        validRequest.setStatus("IN_PROGRESS");

        when(taskRepository.findByIdAndTenant_Id(taskId, testTenantId))
                .thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.updateTaskStatus(taskId, validRequest);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
}