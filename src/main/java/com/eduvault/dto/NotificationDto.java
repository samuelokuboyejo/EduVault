package com.eduvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {
    private UUID id;
    private String title;
    private String message;
    private boolean readStatus;
    private LocalDateTime createdAt;
}
