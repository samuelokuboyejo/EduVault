package com.eduvault.controllers;

import com.eduvault.dto.*;
import com.eduvault.services.RetrieveReceiptsService;
import com.eduvault.user.enums.Level;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("retrieve")
@Tag(name="Retrieve Receipts", description = "Endpoints for retrieving all receipts")
public class RetrieveReceiptsController {
    private final RetrieveReceiptsService service;

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/college-due")
    public ResponseEntity<List<CollegeDueResponse>>  getAllCollegeDueReceipts(){
        return ResponseEntity.ok(service.getAllCollegeDueReceipts());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/college-due/{studentLevel}")
    public ResponseEntity<List<CollegeDueResponse>>  getAllCollegeDueReceiptsByLevel(@PathVariable Level studentLevel){
        return ResponseEntity.ok(service.getAllCollegeDueReceiptsByLevel(studentLevel));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/course-form")
    public ResponseEntity<List<CourseFormResponse>>  getAllCourseFormReceipts(){
        return ResponseEntity.ok(service.getAllCourseFormReceipts());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/course-form/{studentLevel}")
    public ResponseEntity<List<CourseFormResponse>>  getAllCourseFormsByLevel(@PathVariable Level studentLevel){
        return ResponseEntity.ok(service.getAllCourseFormsByLevel(studentLevel));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/dept-due")
    public ResponseEntity<List<DeptDueResponse>> getAllDeptDueReceipts(){
        return ResponseEntity.ok(service.getAllDeptDueReceipts());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/dept-due/{studentLevel}")
    public ResponseEntity<List<DeptDueResponse>> getAllDeptDueReceiptsByLevel(@PathVariable Level studentLevel){
        return ResponseEntity.ok(service.getAllDeptDueReceiptsByLevel(studentLevel));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/remita-sch-fee")
    public ResponseEntity<List<PdfResponse>> getAllRemitaSchoolFeeReceipts(){
        return ResponseEntity.ok(service.getAllRemitaSchoolFeeReceipts());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/remita-sch-fee/{studentLevel}")
    public ResponseEntity<List<PdfResponse>> getAllRemitaSchoolFeeReceiptsByLevel(@PathVariable Level studentLevel){
        return ResponseEntity.ok(service.getAllRemitaSchoolFeeReceiptsByLevel(studentLevel));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/invoice")
    public ResponseEntity<List<SchoolFeeInvoiceResponse>> getAllSchoolFeeInvoiceReceipts(){
        return ResponseEntity.ok(service.getAllSchoolFeeInvoiceReceipts());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/invoice/{studentLevel}")
    public ResponseEntity<List<SchoolFeeInvoiceResponse>> getAllSchoolFeeInvoiceReceiptsByLevel(@PathVariable Level studentLevel){
        return ResponseEntity.ok(service.getAllSchoolFeeInvoiceReceiptsByLevel(studentLevel));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/sch-fee")
    public ResponseEntity<List<SchoolFeeResponse>> getAllSchoolFeeReceipts(){
        return ResponseEntity.ok(service.getAllSchoolFeeReceipts());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/sch-fee/{studentLevel}")
    public ResponseEntity<List<SchoolFeeResponse>> getAllSchoolFeeReceiptsByLevel(@PathVariable Level studentLevel){
        return ResponseEntity.ok(service.getAllSchoolFeeReceiptsByLevel(studentLevel));
    }
}
