package com.eduvault.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfReaderService {
    public String readDocument(MultipartFile file) throws IOException {
        String content = "";

        if (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                content = pdfStripper.getText(document);
            }
        } else {
            content = new String(file.getBytes());
        }

        return content;
    }
}
