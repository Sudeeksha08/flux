CREATE TABLE customers (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(160) NOT NULL,
    company_name VARCHAR(180) NULL,
    phone VARCHAR(40) NULL,
    address VARCHAR(500) NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_customers_user_id UNIQUE (user_id),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_customers_name (name),
    INDEX idx_customers_company_name (company_name),
    INDEX idx_customers_archived_created (archived, created_at)
) ENGINE=InnoDB;

CREATE TABLE staff (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    phone VARCHAR(40) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_staff_user_id UNIQUE (user_id),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_staff_active_created (active, created_at)
) ENGINE=InnoDB;
