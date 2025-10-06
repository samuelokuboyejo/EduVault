package com.eduvault.entities;

import com.eduvault.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role_change_log")
public class RoleChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole oldRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole newRole;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
