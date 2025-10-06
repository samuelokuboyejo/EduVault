package com.eduvault.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnouncementResponse {
    private String message;
}
