package com.eduvault.controllers;

import com.eduvault.services.RemitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/remita")
public class RemitaController {

    private final RemitaService remitaService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyReceipt(@RequestParam("file") MultipartFile file) {
        return remitaService.verifyReceipt(file);
    }
}
