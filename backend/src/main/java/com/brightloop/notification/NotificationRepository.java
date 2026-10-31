package com.brightloop.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query("select n from Notification n where n.status='PENDING' and n.nextAttemptAt<=:now order by n.createdAt")
    List<Notification> due(@Param("now") Instant now, Pageable page);
}
