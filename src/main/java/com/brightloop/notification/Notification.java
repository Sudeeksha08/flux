package com.brightloop.notification;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notifications_delivery", columnList = "delivery_status,next_attempt_at"))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="recipient_email", nullable=false, length=254) private String recipientEmail;
    @Column(nullable=false, length=200) private String subject;
    @Column(nullable=false, columnDefinition="text") private String body;
    @Column(name="notification_type", nullable=false, length=40) private String type;
    @Column(name="delivery_status", nullable=false, length=20) private String status="PENDING";
    @Column(nullable=false) private int attempts;
    @Column(name="next_attempt_at", nullable=false) private Instant nextAttemptAt;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @Column(name="delivered_at") private Instant deliveredAt;
    @Column(name="last_error", length=500) private String lastError;
    protected Notification() { }
    public Notification(String recipientEmail, String subject, String body, String type) {
        this.recipientEmail=recipientEmail; this.subject=subject; this.body=body; this.type=type;
        this.nextAttemptAt=Instant.now(); this.createdAt=Instant.now();
    }
    public void delivered(){status="SENT";deliveredAt=Instant.now();lastError=null;}
    public void failed(Exception e){attempts++;lastError=(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage()).substring(0,Math.min(500,(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage()).length()));if(attempts>=8)status="FAILED";else{status="PENDING";nextAttemptAt=Instant.now().plusSeconds(Math.min(86400L,60L<<Math.min(attempts,10)));}}
    public UUID getId(){return id;} public String getRecipientEmail(){return recipientEmail;} public String getSubject(){return subject;} public String getBody(){return body;} public String getType(){return type;} public String getStatus(){return status;} public int getAttempts(){return attempts;}
}
