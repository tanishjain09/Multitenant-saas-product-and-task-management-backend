package om.tanish.saas.project.dto;

import om.tanish.saas.project.enums.TaskPriority;
import om.tanish.saas.project.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public class TaskResponseDTO {

    private UUID id;
    private String title;
    private String description;

    private UUID projectId;
    private String projectName;

    private UUID assigneeId;
    private String assigneeEmail;

    private TaskPriority priority;
    private TaskStatus status;
    private Instant dueDate;

    private Instant createdAt;
    private Instant updatedAt;

    public TaskResponseDTO() {}

    public TaskResponseDTO(
            UUID id,
            String title,
            String description,
            UUID projectId,
            String projectName,
            UUID assigneeId,
            String assigneeEmail,
            TaskPriority priority,
            TaskStatus status,
            Instant dueDate,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.projectName = projectName;
        this.assigneeId = assigneeId;
        this.assigneeEmail = assigneeEmail;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public void setAssigneeEmail(String assigneeEmail) {
        this.assigneeEmail = assigneeEmail;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
