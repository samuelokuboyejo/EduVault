package com.eduvault.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountStatusResponse {
    private String message;
}
