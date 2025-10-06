package com.eduvault.services;

import com.cloudinary.Cloudinary;
import com.eduvault.dto.DeptDueResponse;
import com.eduvault.dto.FileDownloadResponse;
import com.eduvault.entities.CourseForm;
import com.eduvault.entities.DeptDue;
import com.eduvault.repositories.DeptDueRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DeptDueService {
    private final UserRepository userRepository;
    private final DeptDueRepository deptDueRepository;
    private final CloudinaryService cloudinaryService;
    private final PdfReaderService pdfReaderService;
    private final Cloudinary cloudinary;

    public DeptDueResponse processReceipt (MultipartFile file, String e_mail, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(e_mail).orElseThrow(() -> new EntityNotFoundException("User not found"));
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        String pdfUrl = uploadResponse.getSecureUrl();
        String content = pdfReaderService.readDocument(file);
        System.out.println("---- PDF CONTENT ----");
        System.out.println(content);
        System.out.println("---------------------");
        String name = extractField(content, "Payer's Name", "[^\\r\\n]+");
        String email = extractField(content, "Payer's Email", "[^\\r\\n]+");
        String matric = extractField(content, "Matric No", "[^\\r\\n]+");
        String session = extractField(content, "Academic Session", "[^\\r\\n]+");
        String level = extractField(content, "Level", "[^\\r\\n]+");
        String transactionRef = extractField(content, "Transaction Reference", "[^\\r\\n]+");
        String status = extractField(content, "Status", "[^\\r\\n]+");
        String amount = extractField(content, "Total Amount", "NGN[\\d,]+");
        String date = extractField(content, "Date Paid", "[^\\r\\n]+");

        DeptDue due = DeptDue.builder()
                .name(name != null ? name.trim() : null)
                .email(email)
                .matricNumber(matric)
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

        DeptDue savedReceipt = deptDueRepository.save(due);

        return DeptDueResponse.builder()
                .id(savedReceipt.getId())
                .amount(savedReceipt.getAmount())
                .matricNumber(savedReceipt.getMatricNumber())
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

    public DeptDueResponse uploadReceipt(MultipartFile file, String email, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<DeptDue> receipts = deptDueRepository.findByUploadedBy(user.getId());
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

    public List<DeptDueResponse> getAllReceipts() {
        List<DeptDue> receipts = deptDueRepository.findAll();
        return receipts.stream()
                .map(receipt -> DeptDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
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

    public List<DeptDueResponse> getAllReceiptsByLevel(Level studentLevel) {
        List<DeptDue> receipts = deptDueRepository.findByStudentLevel(studentLevel);
        return receipts.stream()
                .map(receipt -> DeptDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
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

    public List<DeptDueResponse> getAllReceiptsByLevelByUser(Level studentLevel, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<DeptDue> receipts = deptDueRepository.findByStudentLevelAndUploadedBy(studentLevel, user.getId());
        return receipts.stream()
                .map(receipt -> DeptDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
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


    public List<DeptDueResponse> getAllReceiptsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<DeptDue> receipts = deptDueRepository.findByUploadedBy(user.getId());
        return receipts.stream()
                .map(receipt -> DeptDueResponse.builder()
                        .id(receipt.getId())
                        .amount(receipt.getAmount())
                        .matricNumber(receipt.getMatricNumber())
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
        return deptDueRepository.findUploadedByById(receiptId);
    }

    public byte[] downloadApprovedReceiptsAsZip() throws IOException {
        List<DeptDue> receipts = deptDueRepository.findByState(Status.APPROVED);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (DeptDue receipt : receipts) {
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

    public FileDownloadResponse downloadReceiptByUser(String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        DeptDue receipt = deptDueRepository.findByUploadedBy(user.getId())
                .stream()
                .max(Comparator.comparing(DeptDue::getUploadedAt))
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
                + "-" + receipt.getMatricNumber() + ".pdf";

        return new FileDownloadResponse(fileName, pdfBytes);
    }
}


