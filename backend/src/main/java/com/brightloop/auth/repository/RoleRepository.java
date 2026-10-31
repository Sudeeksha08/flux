package com.brightloop.auth.repository;

import com.brightloop.auth.entity.Role;
import com.brightloop.common.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, RoleName> { }
