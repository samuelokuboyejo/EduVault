package com.eduvault.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
public class CollegeDueResponse {
    private UUID id;

    private String name;

    private String date;

    private String email;

    private String matricNumber;

    private String department;

    private String academicSession;

    private String level;

    private String transactionReference;

    private String status;

    private String amount;

    private String pdfUrl;

    private UUID uploadedBy;

    private LocalDateTime uploadedAt;
}
