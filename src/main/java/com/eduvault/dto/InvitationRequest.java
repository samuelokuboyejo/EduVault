package com.eduvault.dto;

import com.eduvault.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationRequest {
    private String email;

    private UserRole role;
}
