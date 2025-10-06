package com.eduvault.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StudentDto {
    private UUID id;
    private String email;
    private String matricNumber;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
