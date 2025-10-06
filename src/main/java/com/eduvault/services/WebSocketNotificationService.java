package com.eduvault.services;


import com.eduvault.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String email, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                notification
        );
    }
}