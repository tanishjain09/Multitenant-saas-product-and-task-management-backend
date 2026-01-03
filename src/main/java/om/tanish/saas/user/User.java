
package om.tanish.saas.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import om.tanish.saas.tenant.Tenant;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "email"}),
                @UniqueConstraint(columnNames = {"tenant_id", "username"})
        }
)
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false,name = "created_at")
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        try {
            this.role = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
