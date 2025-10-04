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
@Table(name = "remita_sch_fee_receipt")
public class RemitaSchoolFeeReceipt  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status state;

    @Enumerated(EnumType.STRING)
    private Level studentLevel;
}
