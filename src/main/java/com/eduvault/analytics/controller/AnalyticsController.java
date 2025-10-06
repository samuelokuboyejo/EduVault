package com.eduvault.analytics.controller;

import com.eduvault.analytics.dto.ApprovedReceiptDto;
import com.eduvault.analytics.dto.StaffActivityDto;
import com.eduvault.analytics.dto.StaffApprovalRateDto;
import com.eduvault.analytics.dto.StudentDto;
import com.eduvault.analytics.service.AnalyticsService;
import com.eduvault.dto.ReceiptCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for administrative reports and insights")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get all students", description = "Returns a list of all registered students.")
    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(analyticsService.getAllStudents());
    }

    @Operation(summary = "Get All approved receipts", description = "Returns all approved receipts and their details.")
    @GetMapping("/approved-receipts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, List<ApprovedReceiptDto>>> getApprovedReceipts() {
        return ResponseEntity.ok(analyticsService.getAllApprovedReceipts());
    }

    @Operation(summary = "Get approved receipt count", description = "Returns the total number of approved receipts.")
    @GetMapping("/approved-receipts/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReceiptCountResponse> getApprovedReceiptCount() {
        return ResponseEntity.ok(analyticsService.getApprovedReceiptCount());
    }

    @Operation(summary = "Get approver statistics", description = "Returns a list of staff members and the number of receipts they have approved.")
    @GetMapping("/approvers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffApprovalRateDto>> getApproverStats() {
        return ResponseEntity.ok(analyticsService.getApproverStats());
    }

    @Operation(summary = "Get receipts approved this week", description = "Returns count of all receipts approved this week.")
    @GetMapping("/approved/this-week")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReceiptCountResponse> getApprovedThisWeek() {
        return ResponseEntity.ok(analyticsService.getApprovedReceiptsThisWeek());
    }

    @Operation(summary = "Get receipts uploaded this month", description = "Returns the number of receipts uploaded this month.")
    @GetMapping("/uploads/this-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReceiptCountResponse> getUploadsThisMonth() {
        return ResponseEntity.ok(analyticsService.getReceiptsUploadedThisMonth());
    }

    @Operation(summary = "Get new students this month", description = "Returns count of students registered this month.")
    @GetMapping("/students/new-this-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getNewStudentsThisMonth() {
        return ResponseEntity.ok(analyticsService.getNewStudentsThisMonth());
    }

    @Operation(summary = "Get staff activity this month", description = "Returns staff members and how many receipts they approved this month.")
    @GetMapping("/staff/activity-this-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffActivityDto>> getStaffActivityThisMonth() {
        return ResponseEntity.ok(analyticsService.getStaffActivityThisMonth());
    }

    @GetMapping("/download/approved-receipts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadAllApprovedReceipts() throws IOException {
        byte[] zipBytes = analyticsService.downloadAllApprovedReceiptsAsZip();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"approved-receipts.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }
}
