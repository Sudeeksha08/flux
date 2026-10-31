CREATE TABLE quotation_templates (
 id BINARY(16) NOT NULL,name VARCHAR(160) NOT NULL,terms VARCHAR(5000) NULL,validity_days INT NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),INDEX idx_quotation_templates_active(active)
) ENGINE=InnoDB;
CREATE TABLE quotations (
 id BINARY(16) NOT NULL,quotation_number VARCHAR(40) NOT NULL,customer_id BINARY(16) NOT NULL,template_id BINARY(16) NULL,
 validity_date DATE NOT NULL,status VARCHAR(20) NOT NULL,subtotal DECIMAL(19,4) NOT NULL,discount_amount DECIMAL(19,4) NOT NULL,
 tax_amount DECIMAL(19,4) NOT NULL,total_amount DECIMAL(19,4) NOT NULL,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT uk_quotations_number UNIQUE(quotation_number),
 CONSTRAINT fk_quotations_customer FOREIGN KEY(customer_id) REFERENCES customers(id),
 CONSTRAINT fk_quotations_template FOREIGN KEY(template_id) REFERENCES quotation_templates(id),
 INDEX idx_quotations_customer_status(customer_id,status),INDEX idx_quotations_validity_status(validity_date,status)
) ENGINE=InnoDB;
CREATE TABLE quotation_items (
 id BINARY(16) NOT NULL,quotation_id BINARY(16) NOT NULL,product_id VARCHAR(36) NULL,product_name VARCHAR(180) NOT NULL,
 variant_name VARCHAR(160) NULL,quantity INT NOT NULL,unit_price DECIMAL(19,4) NOT NULL,line_total DECIMAL(19,4) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT fk_quotation_items_quotation FOREIGN KEY(quotation_id) REFERENCES quotations(id),
 INDEX idx_quotation_items_quotation(quotation_id)
) ENGINE=InnoDB;
