package com.eduvault.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RejectRequest {
    private UUID receiptId;
    private String reason;
}