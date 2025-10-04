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
public class SchoolFeeResponse {
    private UUID id;

    private String name;

    private String college;

    private String department;

    private String date;

    private String receiptNumber;

    private String matricNumber;

    private String level;

    private String invoiceNumber;

    private String Bank;

    private String amount;

    private String description;

    private String pdfUrl;

    private UUID uploadedBy;

    private LocalDateTime uploadedAt;

}
