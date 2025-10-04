package com.eduvault.auth.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRegisterRequest {
    private String matricNumber;

    private String email;

    private String password;
}
