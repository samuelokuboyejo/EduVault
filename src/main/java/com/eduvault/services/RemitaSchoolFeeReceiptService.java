package com.eduvault.services;

import com.cloudinary.Cloudinary;
import com.eduvault.dto.FileDownloadResponse;
import com.eduvault.dto.PdfResponse;
import com.eduvault.entities.DeptDue;
import com.eduvault.entities.RemitaSchoolFeeReceipt;
import com.eduvault.repositories.RemitaSchoolFeeReceiptRepository;
import com.eduvault.user.User;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import com.eduvault.user.repo.UserRepository;
import com.eduvault.user.service.CloudinaryService;
import com.eduvault.user.utils.UploadResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class RemitaSchoolFeeReceiptService {
    private final PdfReaderService pdfReaderService;
    private final RemitaSchoolFeeReceiptRepository remitaSchoolFeeReceiptRepository;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public PdfResponse processReceipt (MultipartFile file, String e_mail, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(e_mail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        String pdfUrl = uploadResponse.getSecureUrl();
        String content = pdfReaderService.readDocument(file);

        System.out.println("---- PDF CONTENT ----");
        System.out.println(content);
        System.out.println("---------------------");

        String[] lines = content.split("\\r?\\n");

        String rrr = Arrays.stream(lines)
                .filter(l -> l.matches("\\d{4}-\\d{4}-\\d{4,}"))
                .findFirst()
                .orElse(null);

        String name = extractSmart(lines, "NAME");
        String email = extractSmart(lines, "EMAIL");
        String phone = extractSmart(lines, "PHONE NUMBER");
        String amount = extractSmart(lines, "TOTAL AMOUNT");
        String balanceDue = null;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].toUpperCase().contains("BALANCE DUE")) {
                if (i + 1 < lines.length && lines[i + 1].toUpperCase().contains("TOTAL AMOUNT")) {
                    balanceDue = (i + 2 < lines.length) ? lines[i + 2].trim() : null;
                } else {
                    balanceDue = (i + 1 < lines.length) ? lines[i + 1].trim() : null;
                }
                break;
            }
        }

        String authorizationRef = Arrays.stream(lines)
                .filter(l -> l.toUpperCase().contains("AUTHORIZATION REF") || l.toUpperCase().contains("CARD PAYMENT"))
                .map(l -> {
                    Matcher m = Pattern.compile("\\d{6,}").matcher(l);
                    return m.find() ? m.group() : null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        assert name != null;
        String[] nameParts = name.split(" ");
        String lastName = nameParts[0];
        String firstName = nameParts[2];
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUpdatedAt(LocalDateTime.now());

        RemitaSchoolFeeReceipt receipt = RemitaSchoolFeeReceipt.builder()
                .RRR(rrr)
                .name(name != null ? name.trim() : null)
                .email(email)
                .phoneNumber(phone)
                .amount(amount)
                .BalanceDue(balanceDue)
                .authorizationRef(authorizationRef)
                .pdfUrl(pdfUrl)
                .uploadedBy(user.getId())
                .uploadedAt(LocalDateTime.now())
                .state(Status.PENDING)
                .studentLevel(studentLevel)
                .build();

        RemitaSchoolFeeReceipt savedReceipt = remitaSchoolFeeReceiptRepository.save(receipt);
        return PdfResponse.builder()
                .id(savedReceipt.getId())
                .amount(savedReceipt.getAmount())
                .authorizationRef(savedReceipt.getAuthorizationRef())
                .BalanceDue(savedReceipt.getBalanceDue())
                .RRR(savedReceipt.getRRR())
                .email(savedReceipt.getEmail())
                .name(savedReceipt.getName())
                .phoneNumber(savedReceipt.getPhoneNumber())
                .pdfUrl(savedReceipt.getPdfUrl())
                .uploadedAt(savedReceipt.getUploadedAt())
                .uploadedBy(savedReceipt.getUploadedBy())
                .build();
    }

    private String extractSmart(String[] lines, String label) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.toUpperCase().contains(label.toUpperCase())) {
                // Case 1: label and value on same line
                String cleaned = line.replaceAll("(?i)" + label, "").trim();
                if (!cleaned.isEmpty()) {
                    return cleaned;
                }
                if (i + 1 < lines.length) {
                    String next = lines[i + 1].trim();
                    if (!next.toUpperCase().contains(label.toUpperCase()) && !next.isEmpty()) {
                        return next;
                    }
                }
                if (i + 2 < lines.length) {
                    String next2 = lines[i + 2].trim();
                    if (!next2.toUpperCase().contains(label.toUpperCase()) && !next2.isEmpty()) {
                        return next2;
                    }
                }
            }
        }
        return null;
    }

    public PdfResponse uploadReceipt(MultipartFile file, String email, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<RemitaSchoolFeeReceipt> receipts = remitaSchoolFeeReceiptRepository.findByUploadedBy(user.getId());

        boolean alreadyUploadedForLevel = receipts.stream()
                .anyMatch(receipt ->
                        receipt.getStudentLevel() == studentLevel && receipt.getState() != Status.REJECTED
                );

        if (alreadyUploadedForLevel) {
            throw new IllegalStateException(
                    "You have already uploaded a receipt for " + studentLevel + ". " +
                            "You can only re-upload if your receipt was rejected."
            );
        }
        return processReceipt(file, email, studentLevel);
    }


    public List<PdfResponse> getAllReceipts() {
        List<RemitaSchoolFeeReceipt> receipts = remitaSchoolFeeReceiptRepository.findAll();
        return receipts.stream()
                .map(receipt -> PdfResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .RRR(receipt.getRRR())
                        .authorizationRef(receipt.getAuthorizationRef())
                        .BalanceDue(receipt.getBalanceDue())
                        .email(receipt.getEmail())
                        .phoneNumber(receipt.getPhoneNumber())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .amount(receipt.getAmount())
                        .build())

                .toList();    }

    public List<PdfResponse> getAllReceiptsByLevel(Level studentLevel) {
        List<RemitaSchoolFeeReceipt> receipts = remitaSchoolFeeReceiptRepository.findByStudentLevel(studentLevel);
        return receipts.stream()
                .map(receipt -> PdfResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .RRR(receipt.getRRR())
                        .authorizationRef(receipt.getAuthorizationRef())
                        .BalanceDue(receipt.getBalanceDue())
                        .email(receipt.getEmail())
                        .phoneNumber(receipt.getPhoneNumber())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .amount(receipt.getAmount())
                        .build())
                .toList();    }

    public List<PdfResponse> getAllReceiptsByLevelByUser(Level studentLevel, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<RemitaSchoolFeeReceipt> receipts = remitaSchoolFeeReceiptRepository.findByStudentLevelAndUploadedBy(studentLevel, user.getId());
        return receipts.stream()
                .map(receipt -> PdfResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .RRR(receipt.getRRR())
                        .authorizationRef(receipt.getAuthorizationRef())
                        .BalanceDue(receipt.getBalanceDue())
                        .email(receipt.getEmail())
                        .phoneNumber(receipt.getPhoneNumber())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .amount(receipt.getAmount())
                        .build())
                .toList();    }

    public List<PdfResponse> getAllReceiptsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<RemitaSchoolFeeReceipt> receipts = remitaSchoolFeeReceiptRepository.findByUploadedBy(user.getId());
        return receipts.stream()
                .map(receipt -> PdfResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .RRR(receipt.getRRR())
                        .authorizationRef(receipt.getAuthorizationRef())
                        .BalanceDue(receipt.getBalanceDue())
                        .email(receipt.getEmail())
                        .phoneNumber(receipt.getPhoneNumber())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .amount(receipt.getAmount())
                        .build())

                .toList();
    }

    public UUID getUploadedBy(UUID receiptId) {
        return remitaSchoolFeeReceiptRepository.findUploadedByById(receiptId);
    }

    public byte[] downloadApprovedReceiptsAsZip() throws IOException {
        List<RemitaSchoolFeeReceipt> receipts = remitaSchoolFeeReceiptRepository.findByState(Status.APPROVED);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (RemitaSchoolFeeReceipt receipt : receipts) {
                if (receipt.getPdfUrl() == null) continue;

                String safeName = receipt.getName() != null ? receipt.getName().replaceAll("\\s+", "_") : "receipt";
                String fileName = safeName + ".pdf";

                try (InputStream in = new URL(receipt.getPdfUrl()).openStream()) {
                    zos.putNextEntry(new ZipEntry(fileName));
                    in.transferTo(zos);
                    zos.closeEntry();
                } catch (Exception e) {
                    System.err.println("Could not download PDF: " + receipt.getPdfUrl() + " -> " + e.getMessage());
                }
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

    public FileDownloadResponse downloadReceiptByUser(String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        RemitaSchoolFeeReceipt receipt = remitaSchoolFeeReceiptRepository.findByUploadedBy(user.getId())
                .stream()
                .max(Comparator.comparing(RemitaSchoolFeeReceipt::getUploadedAt))
                .orElseThrow(() -> new EntityNotFoundException("No receipt found for this user."));

        if (receipt.getPdfUrl() == null || receipt.getPdfUrl().isBlank()) {
            throw new FileNotFoundException("No PDF found for this receipt.");
        }

        String fileUrl = receipt.getPdfUrl();

        byte[] pdfBytes;
        try (InputStream in = new URL(fileUrl).openStream()) {
            pdfBytes = in.readAllBytes();
        } catch (Exception e) {
            throw new IOException("Could not download PDF from Cloudinary: " + e.getMessage(), e);
        }

        String fileName = (receipt.getName() != null ? receipt.getName().replaceAll("\\s+", "_") : "receipt")
               + ".pdf";

        return new FileDownloadResponse(fileName, pdfBytes);
    }
}
