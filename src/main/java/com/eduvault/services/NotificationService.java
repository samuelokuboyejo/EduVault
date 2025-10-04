package com.eduvault.services;

import com.eduvault.dto.CountResponse;
import com.eduvault.dto.MarkAllReadResponse;
import com.eduvault.dto.NotificationDto;
import com.eduvault.dto.NotificationResponse;
import com.eduvault.entities.Notification;
import com.eduvault.repositories.NotificationRepository;
import com.eduvault.user.User;
import com.eduvault.user.repo.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Page<NotificationDto> getUserNotifications(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return notifications.map(n -> NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .readStatus(n.getReadStatus())
                .createdAt(n.getCreatedAt())
                .build()
        );
    }


    public NotificationResponse markAsRead(UUID notificationId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized to mark this notification");
        }
        notification.setReadStatus(true);
        Notification updated = notificationRepository.save(notification);

        NotificationDto dto = NotificationDto.builder()
                .id(updated.getId())
                .title(updated.getTitle())
                .message(updated.getMessage())
                .readStatus(updated.getReadStatus())
                .createdAt(updated.getCreatedAt())
                .build();

        return new NotificationResponse("Notification marked as read successfully", dto);
    }


    public MarkAllReadResponse markAllAsRead(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadStatusFalse(user.getId());

        unreadNotifications.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(unreadNotifications);

        return new MarkAllReadResponse(unreadNotifications.size(), "All unread notifications marked as read successfully");
    }


    public Notification createNotification(String email, String title, String message) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    public CountResponse getUnreadCount(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Long unreadCount = notificationRepository.countByUserIdAndReadStatusFalse(user.getId());
        return CountResponse.builder()
                .unreadNotifications(unreadCount)
                .build();
    }
}

