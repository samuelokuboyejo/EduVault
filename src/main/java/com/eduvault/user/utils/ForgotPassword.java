package com.eduvault.user.utils;

import com.eduvault.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ForgotPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID fpId;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @OneToOne
    private User user;

    private boolean used;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}