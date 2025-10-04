package com.eduvault.services;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RemitaService {

    @Value("${remita.merchantId}")
    private String merchantId;

    @Value("${remita.apiKey}")
    private String apiKey;

    @Value("${remita.apiSecret}")
    private String apiSecret;

    @Value("${remita.baseUrl}")
    private String baseUrl;


    public ResponseEntity<?> verifyReceipt(MultipartFile file) {
        try {
            // Step 1: Extract text from PDF
            String text = extractTextFromPdf(file);

            // Step 2: Extract RRR
            String rrr = extractRRR(text);
            if (rrr == null) {
                return ResponseEntity.badRequest().body("No RRR number found in PDF.");
            }

            // Step 3: Call Remita API
            String response = callRemitaApi(rrr);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error reading PDF: " + e.getMessage());
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractRRR(String text) {
        Pattern pattern = Pattern.compile("\\b\\d{12}\\b"); // adjust if RRR format changes
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String callRemitaApi(String rrr) {
        try {
            String dataToHash = merchantId + apiKey + rrr + apiSecret;
            String hash = sha512(dataToHash);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "remitaConsumerKey=" + apiKey + ",remitaConsumerToken=" + hash);
            headers.set("MerchantId", merchantId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Call API
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/rrr/" + rrr;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();

        } catch (Exception e) {
            return "Error calling Remita API: " + e.getMessage();
        }
    }

    private String sha512(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
