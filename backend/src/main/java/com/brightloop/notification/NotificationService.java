package com.brightloop.notification;

import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final JavaMailSender mail;
    private final String from;
    public NotificationService(NotificationRepository repository, JavaMailSender mail,
                               @Value("${app.mail.from:no-reply@brightloop.local}") String from) {
        this.repository=repository;this.mail=mail;this.from=from;
    }
    @Transactional public void enqueue(String email, String type, String subject, String body) {
        repository.save(new Notification(email, subject, body, type));
    }
    @Scheduled(fixedDelayString="${brightloop.notifications.poll-ms:30000}")
    @Transactional
    public void deliverPending() {
        for (Notification notification : repository.due(Instant.now(), PageRequest.of(0, 50))) {
            try {
                SimpleMailMessage message=new SimpleMailMessage(); message.setFrom(from);
                message.setTo(notification.getRecipientEmail()); message.setSubject(notification.getSubject());
                message.setText(notification.getBody()); mail.send(message); notification.delivered();
            } catch (Exception failure) { notification.failed(failure); }
        }
    }
}
