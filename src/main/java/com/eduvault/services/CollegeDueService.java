package com.eduvault.services;

import com.eduvault.dto.CollegeDueResponse;
import com.eduvault.entities.CollegeDue;
import com.eduvault.repositories.CollegeDueRepository;
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
public class CollegeDueService {
    private final PdfReaderService pdfReaderService;
    private final CollegeDueRepository collegeDueRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public CollegeDueResponse processReceipt (MultipartFile file, String e_mail, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(e_mail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        String pdfUrl = uploadResponse.getSecureUrl();
        String content = pdfReaderService.readDocument(file);

        System.out.println("---- PDF CONTENT ----");
        System.out.println(content);
        System.out.println("---------------------");
        String name = extractField(content, "Payer's Name", "[^\\r\\n]+");
        String email = extractField(content, "Payer's Email", "[^\\r\\n]+");
        String matric = extractField(content, "Matric No", "[^\\r\\n]+");
        String department = extractField(content, "Department", "[^\\r\\n]+");
        String session = extractField(content, "Academic Session", "[^\\r\\n]+");
        String level = extractField(content, "Level", "[^\\r\\n]+");
        String transactionRef = extractField(content, "Transaction Reference", "[^\\r\\n]+");
        String status = extractField(content, "Status", "[^\\r\\n]+");
        String amount = extractField(content, "Total Amount", "NGN[\\d,]+");
        String date = extractField(content, "Date Paid", "[^\\r\\n]+");

        CollegeDue due = CollegeDue.builder()
                .name(name != null ? name.trim() : null)
                .email(email)
                .matricNumber(matric)
                .department(department)
                .academicSession(session)
                .level(level)
                .transactionReference(transactionRef)
                .status(status)
                .amount(amount)
                .date(date)
                .pdfUrl(pdfUrl)
                .uploadedBy(user.getId())
                .uploadedAt(LocalDateTime.now())
                .state(Status.PENDING)
                .studentLevel(studentLevel)
                .build();

        CollegeDue savedReceipt = collegeDueRepository.save(due);

        return CollegeDueResponse.builder()
                .id(savedReceipt.getId())
                .amount(savedReceipt.getAmount())
                .matricNumber(savedReceipt.getMatricNumber())
                .department(savedReceipt.getDepartment())
                .academicSession(savedReceipt.getAcademicSession())
                .email(savedReceipt.getEmail())
                .name(savedReceipt.getName())
                .level(savedReceipt.getLevel())
                .status(savedReceipt.getStatus())
                .date(savedReceipt.getDate())
                .pdfUrl(savedReceipt.getPdfUrl())
                .transactionReference(savedReceipt.getTransactionReference())
                .uploadedAt(savedReceipt.getUploadedAt())
                .uploadedBy(savedReceipt.getUploadedBy())
                .build();
    }

    private String extractField(String content, String label, String regex) {
        Pattern pattern = Pattern.compile(label + "\\s+(" + regex + ")", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public CollegeDueResponse uploadReceipt(MultipartFile file, String email, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<CollegeDue> receipts = collegeDueRepository.findByUploadedBy(user.getId());

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



    public List<CollegeDueResponse> getAllReceipts() {
        List<CollegeDue> receipts = collegeDueRepository.findAll();
        return receipts.stream()
                .map(receipt -> CollegeDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
                        .department(receipt.getDepartment())
                        .academicSession(receipt.getAcademicSession())
                        .email(receipt.getEmail())
                        .name(receipt.getName())
                        .level(receipt.getLevel())
                        .status(receipt.getStatus())
                        .date(receipt.getDate())
                        .transactionReference(receipt.getTransactionReference())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }


    public List<CollegeDueResponse> getAllReceiptsByLevel(Level studentLevel) {
        List<CollegeDue> receipts = collegeDueRepository.findByStudentLevel(studentLevel);
        return receipts.stream()
                .map(receipt -> CollegeDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
                        .department(receipt.getDepartment())
                        .academicSession(receipt.getAcademicSession())
                        .email(receipt.getEmail())
                        .name(receipt.getName())
                        .level(receipt.getLevel())
                        .status(receipt.getStatus())
                        .date(receipt.getDate())
                        .transactionReference(receipt.getTransactionReference())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }

    public List<CollegeDueResponse> getAllReceiptsByLevelByUser(Level studentLevel, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<CollegeDue> receipts = collegeDueRepository.findByStudentLevelAndUploadedBy(studentLevel, user.getId());
        return receipts.stream()
                .map(receipt -> CollegeDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
                        .department(receipt.getDepartment())
                        .academicSession(receipt.getAcademicSession())
                        .email(receipt.getEmail())
                        .name(receipt.getName())
                        .level(receipt.getLevel())
                        .status(receipt.getStatus())
                        .date(receipt.getDate())
                        .transactionReference(receipt.getTransactionReference())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }

    public List<CollegeDueResponse> getAllReceiptsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<CollegeDue> receipts = collegeDueRepository.findByUploadedBy(user.getId());
        return receipts.stream()
                .map(receipt -> CollegeDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
                        .department(receipt.getDepartment())
                        .academicSession(receipt.getAcademicSession())
                        .email(receipt.getEmail())
                        .name(receipt.getName())
                        .level(receipt.getLevel())
                        .status(receipt.getStatus())
                        .date(receipt.getDate())
                        .transactionReference(receipt.getTransactionReference())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();
    }
    public UUID getUploadedBy(UUID receiptId) {
        return collegeDueRepository.findUploadedByById(receiptId);
    }

    public byte[] downloadApprovedReceiptsAsZip() throws IOException {
        List<CollegeDue> receipts = collegeDueRepository.findByState(Status.APPROVED);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (CollegeDue receipt : receipts) {
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


