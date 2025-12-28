package om.tanish.saas.project.dto;

import om.tanish.saas.project.Task;

import java.time.Instant;
import java.util.UUID;

public class TaskDTO {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private UUID projectId;
    private String projectName;
    private UUID assigneeId;
    private String assigneeUsername;
    private UUID createdById;
    private String createdByUsername;
    private Instant dueDate;
    private Instant createdAt;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
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

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus().toString();
        this.priority = task.getPriority().toString();

        if (task.getProject() != null) {
            this.projectId = task.getProject().getId();
            this.projectName = task.getProject().getName();
        }

        if (task.getAssignee() != null) {
            this.assigneeId = task.getAssignee().getId();
            this.assigneeUsername = task.getAssignee().getUsername();
        }

        if (task.getCreatedBy() != null) {
            this.createdById = task.getCreatedBy().getId();
            this.createdByUsername = task.getCreatedBy().getUsername();
        }
        this.dueDate = task.getDueDate();
        this.createdAt = task.getCreatedAt();

    }
}
