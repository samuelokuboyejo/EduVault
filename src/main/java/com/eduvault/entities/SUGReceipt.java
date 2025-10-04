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
@Table(name = "sug_receipt")
public class SUGReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private String college;

    private String department;

    private String date;

    private String receiptNumber;

    private String matricNumber;

    private String invoiceNumber;

    private String studentType;

    private String transactionDate;

    private String paymentType;

    private String paymentReference;

    private String amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status state;

    private String pdfUrl;

    private LocalDateTime uploadedAt;

    private UUID uploadedBy;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Level studentLevel;
}
