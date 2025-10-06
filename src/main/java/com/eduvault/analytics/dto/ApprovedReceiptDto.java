package com.eduvault.analytics.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ApprovedReceiptDto {
    private UUID id;
    private String receiptName;
    private String uploadedBy;
    private String approvedBy;
    private LocalDateTime uploadedAt;
}
