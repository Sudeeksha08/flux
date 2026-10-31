package com.brightloop.auth.entity;

import com.brightloop.common.enums.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private RoleName name;

    protected Role() { }

    public Role(RoleName name) { this.name = name; }

    public RoleName getName() { return name; }
}
