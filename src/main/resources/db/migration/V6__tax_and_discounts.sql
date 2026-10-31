CREATE TABLE tax_rules (
 id BINARY(16) NOT NULL,tax_name VARCHAR(160) NOT NULL,tax_percentage DECIMAL(9,4) NOT NULL,tax_type VARCHAR(16) NOT NULL,
 effective_from DATE NOT NULL,effective_to DATE NULL,active BOOLEAN NOT NULL DEFAULT TRUE,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),INDEX idx_tax_rules_active_dates(active,effective_from,effective_to)
) ENGINE=InnoDB;
CREATE TABLE tax_rule_products (
 tax_rule_id BINARY(16) NOT NULL,product_id BINARY(16) NOT NULL,PRIMARY KEY(tax_rule_id,product_id),
 CONSTRAINT fk_tax_rule_products_tax FOREIGN KEY(tax_rule_id) REFERENCES tax_rules(id),CONSTRAINT fk_tax_rule_products_product FOREIGN KEY(product_id) REFERENCES products(id),INDEX idx_tax_products_product(product_id)
) ENGINE=InnoDB;
CREATE TABLE discounts (
 id BINARY(16) NOT NULL,discount_name VARCHAR(160) NOT NULL,discount_type VARCHAR(16) NOT NULL,discount_value DECIMAL(19,4) NOT NULL,
 minimum_purchase DECIMAL(19,4) NOT NULL,minimum_quantity INT NOT NULL,start_date DATE NOT NULL,end_date DATE NULL,usage_limit INT NULL,usage_count INT NOT NULL DEFAULT 0,
 active BOOLEAN NOT NULL DEFAULT TRUE,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,PRIMARY KEY(id),
 INDEX idx_discounts_active_dates(active,start_date,end_date)
) ENGINE=InnoDB;
CREATE TABLE discount_products (
 discount_id BINARY(16) NOT NULL,product_id BINARY(16) NOT NULL,PRIMARY KEY(discount_id,product_id),
 CONSTRAINT fk_discount_products_discount FOREIGN KEY(discount_id) REFERENCES discounts(id),CONSTRAINT fk_discount_products_product FOREIGN KEY(product_id) REFERENCES products(id),INDEX idx_discount_products_product(product_id)
) ENGINE=InnoDB;
CREATE TABLE discount_usages (
 id BINARY(16) NOT NULL,discount_id BINARY(16) NOT NULL,billing_reference VARCHAR(80) NOT NULL,amount DECIMAL(19,4) NOT NULL,used_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT uk_discount_usage_reference UNIQUE(discount_id,billing_reference),CONSTRAINT fk_discount_usage_discount FOREIGN KEY(discount_id) REFERENCES discounts(id),INDEX idx_discount_usage_date(used_at)
) ENGINE=InnoDB;
