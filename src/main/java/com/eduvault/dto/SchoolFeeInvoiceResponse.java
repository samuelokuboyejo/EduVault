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
public class SchoolFeeInvoiceResponse {

    private UUID id;

    private String name;

    private String email;

    private String invoiceNumber;

    private String phone;

    private String amount;

    private String RRR;

    private UUID uploadedBy;

    private LocalDateTime uploadedAt;

    private String pdfUrl;
}
