package com.eduvault.entities;

import com.eduvault.user.User;
import com.eduvault.user.enums.NotificationScope;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;

    private String message;

    @Builder.Default
    private Boolean readStatus = false;

    @Enumerated(EnumType.STRING)
    private NotificationScope scope;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
}
