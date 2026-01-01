package om.tanish.saas.project.dto;

import java.time.Instant;
import java.util.UUID;

public class ProjectResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private Instant createdAt;

    // flatten owner
    private UUID ownerId;

    public ProjectResponseDTO(UUID id, String name, String description, Instant createdAt, UUID ownerId, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
    }

    private String ownerEmail;

    public ProjectResponseDTO() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}