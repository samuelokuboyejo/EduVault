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
@Table(name = "college_due")
public class CollegeDue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
