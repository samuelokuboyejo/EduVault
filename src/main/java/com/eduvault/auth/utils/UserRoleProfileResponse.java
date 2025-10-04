package com.eduvault.auth.utils;

import com.eduvault.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleProfileResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private UserRole role;

    private String imageUrl;

    private LocalDateTime lastLogin;

    private LocalDateTime dateJoined;
}
