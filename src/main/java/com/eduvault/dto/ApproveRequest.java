package com.eduvault.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ApproveRequest {
    private UUID receiptId;
}
