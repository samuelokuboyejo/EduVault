package com.eduvault.services;

import com.eduvault.dto.*;
import com.eduvault.entities.Notification;
import com.eduvault.repositories.NotificationRepository;
import com.eduvault.user.User;
import com.eduvault.user.enums.NotificationScope;
import com.eduvault.user.enums.UserRole;
import com.eduvault.user.repo.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final WebSocketNotificationService webSocketNotificationService;


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
        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = NotificationDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .readStatus(saved.getReadStatus())
                .createdAt(saved.getCreatedAt())
                .build();

        webSocketNotificationService.sendNotification(email, dto);

        return saved;
    }

    public CountResponse getUnreadCount(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Long unreadCount = notificationRepository.countByUserIdAndReadStatusFalse(user.getId());
        return CountResponse.builder()
                .unreadNotifications(unreadCount)
                .build();
    }

    public AnnouncementResponse broadcastAnnouncementToStudents(String senderEmail, String title, String message) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        List<User> students = userRepository.findByRoleIn(List.of(UserRole.STUDENT));

        List<Notification> notifications = students.stream()
                .map(student -> Notification.builder()
                        .title(title)
                        .message(message)
                        .user(student)
                        .sender(sender)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);

        saved.forEach(n -> {
            NotificationDto dto = NotificationDto.builder()
                    .id(n.getId())
                    .title(n.getTitle())
                    .message(n.getMessage())
                    .readStatus(n.getReadStatus())
                    .createdAt(n.getCreatedAt())
                    .build();
            webSocketNotificationService.sendNotification(n.getUser().getEmail(), dto);
        });
        return AnnouncementResponse.builder()
                .message("Announcement broadcasted successfully!")
                .build();
    }



    public AnnouncementResponse broadcastAnnouncementToAdminsAndStaff(String senderEmail, String title, String message) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        List<User> users = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.STAFF));

        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .title(title)
                        .message(message)
                        .user(user)
                        .sender(sender)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);

        saved.forEach(n -> {
            NotificationDto dto = NotificationDto.builder()
                    .id(n.getId())
                    .title(n.getTitle())
                    .message(n.getMessage())
                    .readStatus(n.getReadStatus())
                    .createdAt(n.getCreatedAt())
                    .build();
            webSocketNotificationService.sendNotification(n.getUser().getEmail(), dto);
        });
        return AnnouncementResponse.builder()
                .message("Announcement broadcasted successfully!")
                .build();
    }



    public AnnouncementResponse sendCustomNotification(String senderEmail, List<String> recipientEmails, String title, String message) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        List<User> recipients = userRepository.findByEmailIn(recipientEmails);

        List<Notification> notifications = recipients.stream()
                .map(u -> Notification.builder()
                        .title(title)
                        .message(message)
                        .sender(sender)
                        .user(u)
                        .scope(NotificationScope.CUSTOM)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);

        saved.forEach(n -> {
            NotificationDto dto = NotificationDto.builder()
                    .id(n.getId())
                    .title(n.getTitle())
                    .message(n.getMessage())
                    .readStatus(n.getReadStatus())
                    .createdAt(n.getCreatedAt())
                    .build();
            webSocketNotificationService.sendNotification(n.getUser().getEmail(), dto);
        });
        return AnnouncementResponse.builder()
                .message("Notification sent successfully!")
                .build();
    }

    public Page<NotificationDto> getNotificationsSentBy(String senderEmail, Pageable pageable) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Page<Notification> notifications = notificationRepository.findBySenderIdOrderByCreatedAtDesc(sender.getId(), pageable);

        return notifications.map(n -> NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .readStatus(n.getReadStatus())
                .createdAt(n.getCreatedAt())
                .build());
    }


}

