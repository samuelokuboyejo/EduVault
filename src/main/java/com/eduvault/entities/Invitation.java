package com.eduvault.entities;

import com.eduvault.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String email;
    private String token;

    @Enumerated(EnumType.STRING)
    private UserRole invitedRole;

    private LocalDateTime expiresAt;
    private boolean used = false;
}
