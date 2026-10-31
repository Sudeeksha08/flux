CREATE TABLE subscriptions (
 id BINARY(16) NOT NULL,subscription_number VARCHAR(40) NOT NULL,customer_id BINARY(16) NOT NULL,plan_id BINARY(16) NOT NULL,quotation_id BINARY(16) NULL,
 start_date DATE NOT NULL,expiry_date DATE NULL,next_billing_date DATE NULL,payment_terms VARCHAR(80) NULL,total_amount DECIMAL(19,4) NOT NULL,
 status VARCHAR(20) NOT NULL,paused_at DATETIME(6) NULL,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT uk_subscriptions_number UNIQUE(subscription_number),
 CONSTRAINT fk_subscriptions_customer FOREIGN KEY(customer_id) REFERENCES customers(id),
 CONSTRAINT fk_subscriptions_plan FOREIGN KEY(plan_id) REFERENCES plans(id),
 CONSTRAINT fk_subscriptions_quote FOREIGN KEY(quotation_id) REFERENCES quotations(id),
 INDEX idx_subscriptions_customer_created(customer_id,created_at),INDEX idx_subscriptions_due_status(status,next_billing_date),INDEX idx_subscriptions_expiry_status(status,expiry_date)
) ENGINE=InnoDB;
CREATE TABLE subscription_items (
 id BINARY(16) NOT NULL,subscription_id BINARY(16) NOT NULL,product_id VARCHAR(36) NULL,product_name VARCHAR(180) NOT NULL,variant_name VARCHAR(160) NULL,
 quantity INT NOT NULL,unit_price DECIMAL(19,4) NOT NULL,line_total DECIMAL(19,4) NOT NULL,
 PRIMARY KEY(id),CONSTRAINT fk_subscription_items_subscription FOREIGN KEY(subscription_id) REFERENCES subscriptions(id),INDEX idx_subscription_items_subscription(subscription_id)
) ENGINE=InnoDB;
