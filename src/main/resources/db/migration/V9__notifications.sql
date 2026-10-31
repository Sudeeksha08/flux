CREATE TABLE notifications (
  id BINARY(16) NOT NULL PRIMARY KEY,
  recipient_email VARCHAR(254) NOT NULL,
  subject VARCHAR(200) NOT NULL,
  body TEXT NOT NULL,
  notification_type VARCHAR(40) NOT NULL,
  delivery_status VARCHAR(20) NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMP(6) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  delivered_at TIMESTAMP(6) NULL,
  last_error VARCHAR(500) NULL,
  INDEX idx_notifications_delivery (delivery_status, next_attempt_at),
  INDEX idx_notifications_recipient (recipient_email, created_at)
);
