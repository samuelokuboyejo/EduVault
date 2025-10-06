package com.eduvault.dto;

import com.eduvault.user.enums.UserRole;
import lombok.Data;

@Data
public class RoleRequest {
    private String email;
    private UserRole role;
}
