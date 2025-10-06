package com.eduvault.analytics.service;

import com.eduvault.analytics.dto.*;
import com.eduvault.dto.ReceiptCountResponse;
import com.eduvault.dto.ReceiptResponse;
import com.eduvault.entities.*;
import com.eduvault.repositories.*;
import com.eduvault.user.User;
import com.eduvault.user.enums.Status;
import com.eduvault.user.enums.UserRole;
import com.eduvault.user.repo.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CollegeDueRepository collegeDueRepository;
    private final CourseFormRepository courseFormRepository;
    private final SchoolFeeReceiptRepository schoolFeeReceiptRepository;
    private final DeptDueRepository deptDueRepository;
    private final SchoolFeeInvoiceRepository schoolFeeInvoiceRepository;
    private final RemitaSchoolFeeReceiptRepository remitaSchoolFeeReceiptRepository;
    private final UserRepository userRepository;

    public List<StudentDto> getAllStudents() {
        List<User> students = userRepository.findByRole(UserRole.STUDENT);
        if (students.isEmpty()) {
            throw new EntityNotFoundException("No students found.");
        }

        return students.stream()
                .map(student -> new StudentDto(
                        student.getId(),
                        student.getEmail(),
                        student.getMatricNumber(),
                        student.getCreatedAt(),
                        student.getLastLogin()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, List<ApprovedReceiptDto>> getAllApprovedReceipts() {
        List<ApprovedReceiptDto> allApproved = new ArrayList<>();

        Map<String, List<ApprovedReceiptDto>> categorizedReceipts = new LinkedHashMap<>();
        List<CollegeDue> collegeDues = collegeDueRepository.findByState(Status.APPROVED);
        categorizedReceipts.put("College Due", mapToApprovedReceiptDtos(collegeDues, "College Due"));

        List<SchoolFeeReceipt> schoolFeeReceipts = schoolFeeReceiptRepository.findByState(Status.APPROVED);
        categorizedReceipts.put("School Fee Receipt", mapToApprovedReceiptDtos(schoolFeeReceipts, "School Fee Receipt"));

        List<DeptDue> deptDues = deptDueRepository.findByState(Status.APPROVED);
        categorizedReceipts.put("Department Due", mapToApprovedReceiptDtos(deptDues, "Department Due"));

        List<CourseForm> courseForms = courseFormRepository.findByState(Status.APPROVED);
        categorizedReceipts.put("Course Form", mapToApprovedReceiptDtos(courseForms, "Course Form"));

        List<RemitaSchoolFeeReceipt> remitaReceipts = remitaSchoolFeeReceiptRepository.findByState(Status.APPROVED);
        categorizedReceipts.put("Remita School Fee Receipt", mapToApprovedReceiptDtos(remitaReceipts, "Remita School Fee Receipt"));

        List<SchoolFeeInvoice> schoolFeeInvoices = schoolFeeInvoiceRepository.findByState(Status.APPROVED);
        categorizedReceipts.put("School Fee Invoice", mapToApprovedReceiptDtos(schoolFeeInvoices, "School Fee Invoice"));

        categorizedReceipts.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        if (categorizedReceipts.isEmpty()) {
            throw new EntityNotFoundException("No approved receipts found across any category.");
        }

        return categorizedReceipts;
    }





    private <T> List<ApprovedReceiptDto> mapToApprovedReceiptDtos(List<T> receipts, String typeName) {
        if (receipts.isEmpty()) return List.of();

        Set<UUID> userIds = receipts.stream()
                .flatMap(r -> Stream.of(getFieldValue(r, "uploadedBy"), getFieldValue(r, "approvedBy")))
                .filter(Objects::nonNull)
                .map(UUID.class::cast)
                .collect(Collectors.toSet());

        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return receipts.stream()
                .map(r -> {
                    UUID uploaderId = (UUID) getFieldValue(r, "uploadedBy");
                    UUID approverId = (UUID) getFieldValue(r, "approvedBy");
                    String receiptName = (String) getFieldValue(r, "name");
                    LocalDateTime uploadedAt = (LocalDateTime) getFieldValue(r, "uploadedAt");

                    User uploader = userMap.get(uploaderId);
                    User approver = userMap.get(approverId);

                    String uploaderName = uploader != null
                            ? uploader.getFirstName() + " " + uploader.getLastName()
                            : "Unknown";

                    String approverName = approver != null
                            ? approver.getFirstName() + " " + approver.getLastName()
                            : "N/A";

                    return new ApprovedReceiptDto(
                            (UUID) getFieldValue(r, "id"),
                            receiptName + " (" + typeName + ")",
                            uploaderName,
                            approverName,
                            uploadedAt
                    );
                })
                .toList();
    }


    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }



    public byte[] downloadAllApprovedReceiptsAsZip() throws IOException {
        Map<String, List<String>> receiptSources = Map.of(
                "CollegeDue", collegeDueRepository.findByState(Status.APPROVED)
                        .stream().map(CollegeDue::getPdfUrl).filter(Objects::nonNull).toList(),
                "CourseForm", courseFormRepository.findByState(Status.APPROVED)
                        .stream().map(CourseForm::getPdfUrl).filter(Objects::nonNull).toList(),
                "DeptDue", deptDueRepository.findByState(Status.APPROVED)
                        .stream().map(DeptDue::getPdfUrl).filter(Objects::nonNull).toList(),
                "SchoolFeeReceipt", schoolFeeReceiptRepository.findByState(Status.APPROVED)
                        .stream().map(SchoolFeeReceipt::getPdfUrl).filter(Objects::nonNull).toList(),
                "SchoolFeeInvoice", schoolFeeInvoiceRepository.findByState(Status.APPROVED)
                        .stream().map(SchoolFeeInvoice::getPdfUrl).filter(Objects::nonNull).toList(),
                "RemitaSchoolFeeReceipt", remitaSchoolFeeReceiptRepository.findByState(Status.APPROVED)
                        .stream().map(RemitaSchoolFeeReceipt::getPdfUrl).filter(Objects::nonNull).toList()
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, List<String>> entry : receiptSources.entrySet()) {
                String category = entry.getKey();
                List<String> urls = entry.getValue();

                if (urls.isEmpty()) continue;

                for (String url : urls) {
                    try {
                        String fileName = extractFileName(url);

                        String zipPath = category + "/" + fileName + ".pdf";

                        try (InputStream in = new URL(url).openStream()) {
                            zos.putNextEntry(new ZipEntry(zipPath));
                            in.transferTo(zos);
                            zos.closeEntry();
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Could not download PDF: " + url + " -> " + e.getMessage());
                    }
                }
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

    /**
     * Extracts a clean filename from the Cloudinary URL (without version or query params)
     */
    private String extractFileName(String pdfUrl) {
        try {
            String[] parts = pdfUrl.split("/");
            String lastPart = parts[parts.length - 1];
            return lastPart.replace(".pdf", "").replaceAll("[^a-zA-Z0-9_-]", "_");
        } catch (Exception e) {
            return "receipt_" + System.currentTimeMillis();
        }
    }




    public List<StaffApprovalRateDto> getApproverStats() {
        return collegeDueRepository.findApproverStats().stream()
                .map(m -> new StaffApprovalRateDto(
                        (String) m.get("staffName"),
                        ((Number) m.get("totalApproved")).longValue()
                ))
                .collect(Collectors.toList());
    }
    public ReceiptCountResponse getApprovedReceiptCount() {
        long receiptCount =  collegeDueRepository.countByState(Status.APPROVED);
        return ReceiptCountResponse.builder()
                .receiptCount(receiptCount)
                .build();
    }

    public ReceiptCountResponse getApprovedReceiptsThisWeek() {
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        long receiptCount =  collegeDueRepository.countByStatusAndApprovedAtBetween(
                "APPROVED",
                startOfWeek.atStartOfDay(),
                endOfWeek.atTime(23, 59, 59)
        );
        return ReceiptCountResponse.builder()
                .receiptCount(receiptCount)
                .build();
    }

    // Get receipts uploaded this month

    public ReceiptCountResponse getReceiptsUploadedThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        long receiptCount = collegeDueRepository.countByUploadedAtBetween(start, end);
        return ReceiptCountResponse.builder()
                .receiptCount(receiptCount)
                .build();
    }

    //Get new students registered this month
    public long getNewStudentsThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        return userRepository.countByRoleAndCreatedAtBetween(UserRole.STUDENT, start, end);
    }

    //Get staff activity (number of approvals they’ve done this month)
    public List<StaffActivityDto> getStaffActivityThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Map<String, Object>> result = collegeDueRepository.findStaffApprovalStatsThisMonth(start, end);

        return result.stream()
                .map(r -> new StaffActivityDto(
                        (String) r.get("staffName"),
                        ((Number) r.get("approvedCount")).longValue()
                ))
                .collect(Collectors.toList());
    }
}
