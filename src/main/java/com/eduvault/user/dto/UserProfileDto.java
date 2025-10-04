package com.eduvault.user.dto;

import com.eduvault.user.enums.UserRole;

import java.util.UUID;

public record UserProfileDto(UUID id,
                             String email,
                             String firstName,
                             String imageUrl,
                             UserRole role) {

}