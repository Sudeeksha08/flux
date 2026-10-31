package com.brightloop.auth.service;

import com.brightloop.auth.entity.Role;
import com.brightloop.auth.entity.User;
import com.brightloop.auth.repository.RoleRepository;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.common.enums.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                          @Value("${app.auth.bootstrap-admin-email:}") String email,
                          @Value("${app.auth.bootstrap-admin-password:}") String password) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank() || password.length() < 9 || password.length() > 72
                || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s]).*$")) {
            throw new IllegalStateException("Set both bootstrap admin variables and provide a compliant password");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        var existing = users.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            boolean isAdmin = existing.get().getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);
            if (!isAdmin) throw new IllegalStateException("Bootstrap email already belongs to a non-admin account");
            log.info("Bootstrap admin account already exists; no account changes made");
            return;
        }
        Role adminRole = roles.findById(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role was not seeded"));
        users.save(new User(normalizedEmail, passwordEncoder.encode(password), Set.of(adminRole)));
        log.info("Created initial ADMIN account from deployment environment configuration");
    }
}
