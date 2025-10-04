package com.eduvault.services;

import com.eduvault.dto.SchoolFeeInvoiceResponse;
import com.eduvault.entities.SchoolFeeInvoice;
import com.eduvault.repositories.SchoolFeeInvoiceRepository;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class SchoolFeeInvoiceService {
    private final PdfReaderService pdfReaderService;
    private final SchoolFeeInvoiceRepository schoolFeeInvoiceRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public SchoolFeeInvoiceResponse processReceipt (MultipartFile file, String e_mail, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(e_mail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        String pdfUrl = uploadResponse.getSecureUrl();
        String content = pdfReaderService.readDocument(file);
        System.out.println("---- PDF CONTENT ----");
        System.out.println(content);
        System.out.println("---------------------");
        String name = extractField(content, "(?i)([A-Z][A-Za-z ]+)\\s+\\d{11},", null); // before phone
        String phone = extractField(content, "(\\d{11}),", null);
        String email = extractField(content, "([\\w.%-]+@[\\w.-]+\\.[A-Za-z]{2,6})", null);
        String amount = extractField(content, "Total:\\s*N([\\d,]+\\.\\d{2})", null);
        String rrr = extractField(content, "REMITA RRR:\\s*(\\d+)", null);
        String invoiceNo = extractField(content, "REFERENCE/INVOICE NO.\\s*([A-Z0-9/]+)", null);
        String date = extractField(content, "(\\d{1,2}\\s+\\w+\\s+\\d{4}\\s+\\d{2}:\\d{2}:\\d{2})", null);

        SchoolFeeInvoice invoice = SchoolFeeInvoice.builder()
                .name(name != null ? name.trim() : null)
                .email(email)
                .phone(phone)
                .amount(amount)
                .RRR(rrr)
                .invoiceNumber(invoiceNo)
                .pdfUrl(pdfUrl)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user.getId())
                .state(Status.PENDING)
                .studentLevel(studentLevel)
                .build();

        SchoolFeeInvoice savedInvoice = schoolFeeInvoiceRepository.save(invoice);

        return SchoolFeeInvoiceResponse.builder()
                .id(savedInvoice.getId())
                .name(savedInvoice.getName())
                .email(savedInvoice.getEmail())
                .phone(savedInvoice.getPhone())
                .amount(savedInvoice.getAmount())
                .RRR(savedInvoice.getRRR())
                .invoiceNumber(savedInvoice.getInvoiceNumber())
                .pdfUrl(savedInvoice.getPdfUrl())
                .uploadedAt(savedInvoice.getUploadedAt())
                .uploadedBy(savedInvoice.getUploadedBy())
                .build();
    }

    private String extractField(String content, String label, String regex) {
        Pattern pattern;
        if (regex == null) {
            pattern = Pattern.compile(label, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        } else {
            pattern = Pattern.compile(label + "\\s*" + regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        }
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public SchoolFeeInvoiceResponse uploadReceipt(MultipartFile file, String email, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<SchoolFeeInvoice> receipts = schoolFeeInvoiceRepository.findByUploadedBy(user.getId());

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


    public List<SchoolFeeInvoiceResponse> getAllReceipts() {
        List<SchoolFeeInvoice> receipts = schoolFeeInvoiceRepository.findAll();
        return receipts.stream()
                .map(receipt -> SchoolFeeInvoiceResponse.builder()
                        .id(receipt.getId())
                        .email(receipt.getEmail())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .RRR(receipt.getRRR())
                        .amount(receipt.getAmount())
                        .phone(receipt.getPhone())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }

    public List<SchoolFeeInvoiceResponse> getAllReceiptsByLevel(Level studentLevel) {
        List<SchoolFeeInvoice> receipts = schoolFeeInvoiceRepository.findByStudentLevel(studentLevel);
        return receipts.stream()
                .map(receipt -> SchoolFeeInvoiceResponse.builder()
                        .id(receipt.getId())
                        .email(receipt.getEmail())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .RRR(receipt.getRRR())
                        .amount(receipt.getAmount())
                        .phone(receipt.getPhone())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())
                .toList();    }


    public List<SchoolFeeInvoiceResponse> getAllReceiptsByLevelByUser(Level studentLevel, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<SchoolFeeInvoice> receipts = schoolFeeInvoiceRepository.findByStudentLevelAndUploadedBy(studentLevel, user.getId());
        return receipts.stream()
                .map(receipt -> SchoolFeeInvoiceResponse.builder()
                        .id(receipt.getId())
                        .email(receipt.getEmail())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .RRR(receipt.getRRR())
                        .amount(receipt.getAmount())
                        .phone(receipt.getPhone())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())
                .toList();    }

    public List<SchoolFeeInvoiceResponse> getAllReceiptsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<SchoolFeeInvoice> receipts = schoolFeeInvoiceRepository.findByUploadedBy(user.getId());
        return receipts.stream()
                .map(receipt -> SchoolFeeInvoiceResponse.builder()
                        .id(receipt.getId())
                        .email(receipt.getEmail())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .RRR(receipt.getRRR())
                        .amount(receipt.getAmount())
                        .phone(receipt.getPhone())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();
    }
    public UUID getUploadedBy(UUID receiptId) {
        return schoolFeeInvoiceRepository.findUploadedByById(receiptId);
    }

    public byte[] downloadApprovedReceiptsAsZip() throws IOException {
        List<SchoolFeeInvoice> receipts = schoolFeeInvoiceRepository.findByState(Status.APPROVED);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (SchoolFeeInvoice receipt : receipts) {
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
}
