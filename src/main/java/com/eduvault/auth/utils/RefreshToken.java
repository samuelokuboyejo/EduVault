package com.eduvault.auth.utils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.eduvault.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID tokenId;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private Instant expirationTime;

    @OneToOne(mappedBy = "refreshToken")
    @JsonBackReference
    private User user;

}