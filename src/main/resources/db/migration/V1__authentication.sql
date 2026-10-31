CREATE TABLE roles (
    name VARCHAR(24) NOT NULL,
    PRIMARY KEY (name)
) ENGINE=InnoDB;

INSERT INTO roles (name) VALUES ('ADMIN'), ('STAFF'), ('CUSTOMER');

CREATE TABLE users (
    id BINARY(16) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_name VARCHAR(24) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_name) REFERENCES roles (name)
) ENGINE=InnoDB;

CREATE TABLE password_reset_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_password_reset_user_expiry (user_id, expires_at)
) ENGINE=InnoDB;

CREATE TABLE revoked_tokens (
    token_id VARCHAR(36) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (token_id),
    INDEX idx_revoked_tokens_expiry (expires_at)
) ENGINE=InnoDB;
