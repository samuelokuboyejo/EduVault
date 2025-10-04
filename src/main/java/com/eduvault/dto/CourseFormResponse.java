package com.eduvault.dto;

import com.eduvault.user.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
public class CourseFormResponse {
    private UUID id;

    private String name;

    private String matricNumber;

    private String programme;

    private String level;

    private String session;

    private Status state;

    private String pdfUrl;

    private UUID uploadedBy;

    private LocalDateTime uploadedAt;
}
