package com.eduvault.services;

import com.eduvault.dto.CourseFormResponse;
import com.eduvault.entities.CourseForm;
import com.eduvault.repositories.CourseFormRepository;
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
public class CourseFormService {
    private final UserRepository userRepository;
    private final CourseFormRepository courseFormRepository;
    private final CloudinaryService cloudinaryService;
    private final PdfReaderService pdfReaderService;

    public CourseFormResponse processReceipt  (MultipartFile file, String e_mail, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(e_mail).orElseThrow(() -> new EntityNotFoundException("User not found"));
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        String pdfUrl = uploadResponse.getSecureUrl();
        String content = pdfReaderService.readDocument(file);
        System.out.println("---- PDF CONTENT ----");
        System.out.println(content);
        System.out.println("---------------------");
        String session = extractField(content, "(\\d{4}/\\d{4}\\s+SESSION)", null);
        String name = extractField(content, "Name:", "([A-Za-z ]+)");
        String programme = extractField(content, "Programme:", "([A-Za-z ]+)");
        String level = extractField(content, "Level:", "([0-9]+L)");
        String matric = extractField(content, "ID:", "(\\d+)");

        CourseForm form = CourseForm.builder()
                .name(name != null ? name.trim() : null)
                .programme(programme)
                .level(level)
                .matricNumber(matric)
                .session(session)
                .studentLevel(studentLevel)
                .build();

        CourseForm savedForm = courseFormRepository.save(form);

        return CourseFormResponse.builder()
                .id(savedForm.getId())
                .name(savedForm.getName())
                .programme(savedForm.getProgramme())
                .level(savedForm.getLevel())
                .matricNumber(savedForm.getMatricNumber())
                .session(savedForm.getSession())
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user.getId())
                .state(Status.PENDING)
                .build();
    }

    private String extractField(String content, String label, String regex) {
        Pattern pattern = Pattern.compile(label + "\\s*(" + regex + ")", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }


    public CourseFormResponse uploadReceipt(MultipartFile file, String email, Level studentLevel) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<CourseForm> receipts = courseFormRepository.findByUploadedBy(user.getId());

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

    public List<CourseFormResponse> getAllReceiptsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<CourseForm> receipts = courseFormRepository.findByUploadedBy(user.getId());
        return receipts.stream()
                .map(receipt -> CourseFormResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .matricNumber(receipt.getMatricNumber())
                        .programme(receipt.getProgramme())
                        .session(receipt.getSession())
                        .level(receipt.getLevel())
                        .state(receipt.getState())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();
    }

    public List<CourseFormResponse> getAllReceipts() {
        List<CourseForm> receipts = courseFormRepository.findAll();
        return receipts.stream()
                .map(receipt -> CourseFormResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .matricNumber(receipt.getMatricNumber())
                        .programme(receipt.getProgramme())
                        .session(receipt.getSession())
                        .level(receipt.getLevel())
                        .state(receipt.getState())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }


    public List<CourseFormResponse> getAllReceiptsByLevel(Level studentLevel) {
        List<CourseForm> receipts = courseFormRepository.findByStudentLevel(studentLevel);
        return receipts.stream()
                .map(receipt -> CourseFormResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .matricNumber(receipt.getMatricNumber())
                        .programme(receipt.getProgramme())
                        .session(receipt.getSession())
                        .level(receipt.getLevel())
                        .state(receipt.getState())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }

    public List<CourseFormResponse> getAllReceiptsByLevelByUser(Level studentLevel, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<CourseForm> receipts = courseFormRepository.findByStudentLevelAndUploadedBy(studentLevel, user.getId());
        return receipts.stream()
                .map(receipt -> CourseFormResponse.builder()
                        .id(receipt.getId())
                        .name(receipt.getName())
                        .matricNumber(receipt.getMatricNumber())
                        .programme(receipt.getProgramme())
                        .session(receipt.getSession())
                        .level(receipt.getLevel())
                        .state(receipt.getState())
                        .pdfUrl(receipt.getPdfUrl())
                        .uploadedAt(receipt.getUploadedAt())
                        .uploadedBy(receipt.getUploadedBy())
                        .build())

                .toList();    }


    public UUID getUploadedBy(UUID receiptId) {
        return courseFormRepository.findUploadedByById(receiptId);
    }

    public byte[] downloadApprovedReceiptsAsZip() throws IOException {
        List<CourseForm> receipts = courseFormRepository.findByState(Status.APPROVED);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (CourseForm receipt : receipts) {
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
