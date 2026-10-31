CREATE TABLE products (
    id BINARY(16) NOT NULL,
    name VARCHAR(180) NOT NULL,
    product_type VARCHAR(40) NOT NULL,
    sales_price DECIMAL(19,4) NOT NULL,
    cost_price DECIMAL(19,4) NOT NULL,
    notes VARCHAR(2000) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id), INDEX idx_products_active_name (active,name)
) ENGINE=InnoDB;
CREATE TABLE product_variants (
    id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    variant_name VARCHAR(160) NOT NULL,
    additional_price DECIMAL(19,4) NOT NULL,
    PRIMARY KEY (id), INDEX idx_product_variants_product (product_id),
    CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;
CREATE TABLE plans (
    id BINARY(16) NOT NULL,
    plan_name VARCHAR(160) NOT NULL,
    price DECIMAL(19,4) NOT NULL,
    billing_frequency VARCHAR(16) NOT NULL,
    minimum_quantity INT NOT NULL,
    available_from DATE NULL,
    available_to DATE NULL,
    auto_close BOOLEAN NOT NULL DEFAULT FALSE,
    early_close_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    pause_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    renewal_allowed BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id), INDEX idx_plans_active_frequency (active,billing_frequency)
) ENGINE=InnoDB;
CREATE TABLE plan_products (
    id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id), CONSTRAINT uk_plan_products_pair UNIQUE (plan_id,product_id),
    CONSTRAINT fk_plan_products_plan FOREIGN KEY (plan_id) REFERENCES plans(id),
    CONSTRAINT fk_plan_products_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_plan_products_product (product_id)
) ENGINE=InnoDB;
