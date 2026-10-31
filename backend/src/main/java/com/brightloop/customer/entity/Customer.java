package com.brightloop.customer.entity;

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
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customers_user_id", columnNames = "user_id"))
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "company_name", length = 180)
    private String companyName;

    @Column(length = 40)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Customer() { }

    public Customer(User user, String name, String companyName, String phone, String address) {
        this.user = user;
        update(name, companyName, phone, address);
    }

    public void update(String name, String companyName, String phone, String address) {
        this.name = name.trim();
        this.companyName = clean(companyName);
        this.phone = clean(phone);
        this.address = clean(address);
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getEmail() { return user.getEmail(); }
    public String getCompanyName() { return companyName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
    public void archive() { archived = true; }
}
