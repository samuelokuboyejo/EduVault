package com.eduvault.services;

import com.eduvault.dto.SchoolFeeResponse;
import com.eduvault.entities.SchoolFeeReceipt;
import com.eduvault.repositories.SchoolFeeReceiptRepository;
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
public class SchoolFeeReceiptService {
    private final PdfReaderService pdfReaderService;
    private final SchoolFeeReceiptRepository schoolFeeReceiptRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;


    public SchoolFeeResponse processReceipt (MultipartFile file, String e_mail, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(e_mail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        String pdfUrl = uploadResponse.getSecureUrl();
        String content = pdfReaderService.readDocument(file);
        System.out.println("---- PDF CONTENT ----");
        System.out.println(content);
        System.out.println("---------------------");

        String name = extractField(content, "([A-Z][A-Za-z ]+)\\s+Received from", null);
        if (name == null) {
            name = extractField(content, "Received from\\s+([A-Za-z ]+)", null);
        }

        String college = extractField(content, "(College[^\\r\\n]+?)(?=Date:)", null);
        String department = extractField(content, "Department:", "([^\\r\\n]+)");
        String date = extractField(content, "Date:", "(\\d{2}/\\d{2}/\\d{4})");

        String receiptNo = extractField(content, "Receipt No:", "(\\d+)");
        if (receiptNo == null) {
            receiptNo = extractField(content, "(\\d+)Receipt No:", null);
        }

        String matric = extractField(content, "Matric. No.:", "([^\\r\\n]+)");
        String level = extractField(content, "Level:", "([^\\r\\n]+)");
        String invoiceNo = extractField(content, "Invoice No.:", "([^\\r\\n]+)");
        String bank = extractField(content, "Bank:", "([^\\r\\n]+)");
        String amount = extractField(content, "TOTAL", "([\\d,]+\\.\\d{2})");
        String description = extractField(content, "(Returning.*School Fees)", null);

        SchoolFeeReceipt receipt = SchoolFeeReceipt.builder()
                .name(name != null ? name.trim() : null)
                .college(college != null ? college.trim() : null)
                .department(department)
                .date(date)
                .receiptNumber(receiptNo)
                .matricNumber(matric)
                .level(level)
                .invoiceNumber(invoiceNo)
                .Bank(bank)
                .amount(amount)
                .description(description)
                .pdfUrl(pdfUrl)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user.getId())
                .state(Status.PENDING)
                .studentLevel(studentLevel)
                .build();

        SchoolFeeReceipt savedReceipt = schoolFeeReceiptRepository.save(receipt);

        return SchoolFeeResponse.builder()
                .id(savedReceipt.getId())
                .name(savedReceipt.getName())
                .college(savedReceipt.getCollege())
                .department(savedReceipt.getDepartment())
                .date(savedReceipt.getDate())
                .receiptNumber(savedReceipt.getReceiptNumber())
                .matricNumber(savedReceipt.getMatricNumber())
                .level(savedReceipt.getLevel())
                .invoiceNumber(savedReceipt.getInvoiceNumber())
                .Bank(savedReceipt.getBank())
                .amount(savedReceipt.getAmount())
                .description(savedReceipt.getDescription())
                .pdfUrl(savedReceipt.getPdfUrl())
                .uploadedAt(savedReceipt.getUploadedAt())
                .uploadedBy(savedReceipt.getUploadedBy())
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

    public SchoolFeeResponse uploadReceipt(MultipartFile file, String email, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<SchoolFeeReceipt> receipts = schoolFeeReceiptRepository.findByUploadedBy(user.getId());

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

    public List<SchoolFeeResponse> getAllReceipts() {
        List<SchoolFeeReceipt> receipts = schoolFeeReceiptRepository.findAll();
        return receipts.stream()
                .map(receipt -> SchoolFeeResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .college(receipt.getCollege())
                        .department(receipt.getDepartment())
                        .date(receipt.getDate())
                        .receiptNumber(receipt.getReceiptNumber())
                        .matricNumber(receipt.getMatricNumber())
                        .level(receipt.getLevel())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .Bank(receipt.getBank())
                        .amount(receipt.getAmount())
                        .description(receipt.getDescription())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }

    public List<SchoolFeeResponse> getAllReceiptsByLevel(Level studentLevel) {
        List<SchoolFeeReceipt> receipts = schoolFeeReceiptRepository.findByStudentLevel(studentLevel);
        return receipts.stream()
                .map(receipt -> SchoolFeeResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .college(receipt.getCollege())
                        .department(receipt.getDepartment())
                        .date(receipt.getDate())
                        .receiptNumber(receipt.getReceiptNumber())
                        .matricNumber(receipt.getMatricNumber())
                        .level(receipt.getLevel())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .Bank(receipt.getBank())
                        .amount(receipt.getAmount())
                        .description(receipt.getDescription())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())
                .toList();    }


    public List<SchoolFeeResponse> getAllReceiptsByLevelByUser(Level studentLevel, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<SchoolFeeReceipt> receipts = schoolFeeReceiptRepository.findByStudentLevelAndUploadedBy(studentLevel, user.getId());
        return receipts.stream()
                .map(receipt -> SchoolFeeResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .college(receipt.getCollege())
                        .department(receipt.getDepartment())
                        .date(receipt.getDate())
                        .receiptNumber(receipt.getReceiptNumber())
                        .matricNumber(receipt.getMatricNumber())
                        .level(receipt.getLevel())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .Bank(receipt.getBank())
                        .amount(receipt.getAmount())
                        .description(receipt.getDescription())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())
                .toList();
    }

    public List<SchoolFeeResponse> getAllReceiptsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<SchoolFeeReceipt> receipts = schoolFeeReceiptRepository.findByUploadedBy(user.getId());
        return receipts.stream()
                .map(receipt -> SchoolFeeResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .college(receipt.getCollege())
                        .department(receipt.getDepartment())
                        .date(receipt.getDate())
                        .receiptNumber(receipt.getReceiptNumber())
                        .matricNumber(receipt.getMatricNumber())
                        .level(receipt.getLevel())
                        .invoiceNumber(receipt.getInvoiceNumber())
                        .Bank(receipt.getBank())
                        .amount(receipt.getAmount())
                        .description(receipt.getDescription())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();
    }

    public UUID getUploadedBy(UUID receiptId) {
        return schoolFeeReceiptRepository.findUploadedByById(receiptId);
    }

    public byte[] downloadApprovedReceiptsAsZip() throws IOException {
        List<SchoolFeeReceipt> receipts = schoolFeeReceiptRepository.findByState(Status.APPROVED);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (SchoolFeeReceipt receipt : receipts) {
                if (receipt.getPdfUrl() == null) continue;

                String safeName = receipt.getName() != null ? receipt.getName().replaceAll("\\s+", "_") : "receipt";
                String fileName = safeName + "-" + receipt.getMatricNumber() + ".pdf";

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
