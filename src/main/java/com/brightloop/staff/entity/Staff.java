package com.brightloop.staff.entity;

import com.brightloop.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "staff", uniqueConstraints = @UniqueConstraint(name = "uk_staff_user_id", columnNames = "user_id"))
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(length = 40)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Staff() { }

    public Staff(User user, String displayName, String phone) {
        this.user = user;
        update(displayName, phone);
    }

    public void update(String displayName, String phone) {
        this.displayName = displayName.trim();
        this.phone = phone == null || phone.isBlank() ? null : phone.trim();
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return displayName; }
    public String getEmail() { return user.getEmail(); }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void deactivate() { active = false; }
}
