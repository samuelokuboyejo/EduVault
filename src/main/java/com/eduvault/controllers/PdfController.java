package com.eduvault.controllers;

import com.eduvault.services.PdfReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class PdfController {
    private final PdfReaderService documentReaderService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String content = documentReaderService.readDocument(file);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error reading file: " + e.getMessage());
        }
    }


}
