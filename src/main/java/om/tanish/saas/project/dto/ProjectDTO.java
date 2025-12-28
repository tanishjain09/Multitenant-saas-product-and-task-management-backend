package om.tanish.saas.project.dto;

import om.tanish.saas.project.entities.Project;

import java.time.Instant;
import java.util.UUID;

public class ProjectDTO {
    private UUID id;
     private String name;
     private String description;
     private String status;
     private UUID ownerId;
     private String ownerUsername;
     private Instant startDate;
     private Instant endDate;
     private Instant createdAt;
     private int taskCount;
     private int completedTasks;

    public ProjectDTO(Project project) {
         this.id = project.getId();
         this.name = project.getName();
         this.description = project.getDescription();
         this.status = project.getStatus().toString();
         if (project.getOwner() != null) {
             this.ownerId = project.getOwner().getId();
             this.ownerUsername = project.getOwner().getUsername();
         }
         this.startDate = project.getStartDate();
         this.endDate = project.getEndDate();
         this.createdAt = project.getCreatedAt();
     }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }
}
