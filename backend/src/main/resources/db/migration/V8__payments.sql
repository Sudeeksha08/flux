CREATE TABLE payments (
 id BINARY(16) NOT NULL,recorded_by_user_id BINARY(16) NOT NULL,payment_date DATETIME(6) NOT NULL,payment_method VARCHAR(24) NOT NULL,
 amount DECIMAL(19,4) NOT NULL,notes VARCHAR(1000) NULL,payment_reference VARCHAR(120) NULL,status VARCHAR(24) NOT NULL,created_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT fk_payments_recorder FOREIGN KEY(recorded_by_user_id) REFERENCES users(id),INDEX idx_payments_date_status(payment_date,status),INDEX idx_payments_reference(payment_reference)
) ENGINE=InnoDB;
CREATE TABLE payment_allocations (
 id BINARY(16) NOT NULL,payment_id BINARY(16) NOT NULL,invoice_id BINARY(16) NOT NULL,amount DECIMAL(19,4) NOT NULL,allocated_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT uk_payment_invoice UNIQUE(payment_id,invoice_id),CONSTRAINT fk_allocations_payment FOREIGN KEY(payment_id) REFERENCES payments(id),
 CONSTRAINT fk_allocations_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id),INDEX idx_allocations_invoice(invoice_id)
) ENGINE=InnoDB;
