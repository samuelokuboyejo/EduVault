package com.eduvault.entities;

import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sch_fee_receipt")
public class SchoolFeeReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status state;

    @Enumerated(EnumType.STRING)
    private Level studentLevel;

    private UUID approvedBy;

    private UUID rejectedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

}
