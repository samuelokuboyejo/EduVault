package com.eduvault.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StaffActivityDto {
    private String staffName;
    private long approvalsThisMonth;
}
