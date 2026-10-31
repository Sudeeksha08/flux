CREATE TABLE invoices (
 id BINARY(16) NOT NULL,invoice_number VARCHAR(40) NOT NULL,customer_id BINARY(16) NOT NULL,subscription_id BINARY(16) NOT NULL,
 billing_period_start DATE NOT NULL,billing_period_end DATE NOT NULL,invoice_date DATE NOT NULL,due_date DATE NOT NULL,
 customer_name VARCHAR(160) NOT NULL,customer_email VARCHAR(254) NOT NULL,company_name VARCHAR(180) NULL,customer_phone VARCHAR(40) NULL,customer_address VARCHAR(500) NULL,
 subtotal DECIMAL(19,4) NOT NULL,discount_amount DECIMAL(19,4) NOT NULL,tax_amount DECIMAL(19,4) NOT NULL,total_amount DECIMAL(19,4) NOT NULL,
 paid_amount DECIMAL(19,4) NOT NULL DEFAULT 0,outstanding_amount DECIMAL(19,4) NOT NULL,status VARCHAR(20) NOT NULL,
 discount_name VARCHAR(160) NULL,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT uk_invoices_number UNIQUE(invoice_number),CONSTRAINT uk_invoice_subscription_period UNIQUE(subscription_id,billing_period_start),
 CONSTRAINT fk_invoices_customer FOREIGN KEY(customer_id) REFERENCES customers(id),CONSTRAINT fk_invoices_subscription FOREIGN KEY(subscription_id) REFERENCES subscriptions(id),
 INDEX idx_invoices_customer_date(customer_id,invoice_date),INDEX idx_invoices_due_status(due_date,status),INDEX idx_invoices_status_date(status,invoice_date)
) ENGINE=InnoDB;
CREATE TABLE invoice_items (
 id BINARY(16) NOT NULL,invoice_id BINARY(16) NOT NULL,product_id VARCHAR(36) NULL,product_name VARCHAR(180) NOT NULL,variant_name VARCHAR(160) NULL,
 quantity INT NOT NULL,unit_price DECIMAL(19,4) NOT NULL,subtotal DECIMAL(19,4) NOT NULL,discount_amount DECIMAL(19,4) NOT NULL,
 tax_amount DECIMAL(19,4) NOT NULL,total_amount DECIMAL(19,4) NOT NULL,tax_details_json TEXT NOT NULL,
 PRIMARY KEY(id),CONSTRAINT fk_invoice_items_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id),INDEX idx_invoice_items_invoice(invoice_id)
) ENGINE=InnoDB;
ALTER TABLE discount_usages ADD COLUMN invoice_id BINARY(16) NULL;
ALTER TABLE discount_usages ADD CONSTRAINT fk_discount_usage_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id);
