package com.eduvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfResponse {
    private UUID id;

    private String RRR;

    private String name;

    private String email;

    private String phoneNumber;

    private String amount;

    private String BalanceDue;

    private String authorizationRef;

    private String pdfUrl;

    private UUID uploadedBy;

    private LocalDateTime uploadedAt;
}
