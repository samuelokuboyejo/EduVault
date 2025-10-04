package com.eduvault.repositories;

import com.eduvault.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Notification> findByUserIdAndReadStatusFalse(UUID userId);
    Long countByUserIdAndReadStatusFalse(UUID userId);
}
