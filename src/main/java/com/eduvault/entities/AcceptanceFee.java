package com.eduvault.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "acceptance_fee")
public class AcceptanceFee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private String date;

    private String receiptNumber;

    private String matricNumber;

    private String invoiceNumber;

    private String Bank;

    private String amount;

    private String description;
}
