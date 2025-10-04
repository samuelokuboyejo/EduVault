package com.eduvault.services;

import com.eduvault.dto.*;
import com.eduvault.user.enums.Level;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrieveReceiptsService {
    private final CollegeDueService collegeDueService;
    private final CourseFormService courseFormService;
    private final DeptDueService deptDueService;
    private final RemitaSchoolFeeReceiptService remitaSchoolFeeReceiptService;
    private final SchoolFeeReceiptService schoolFeeReceiptService;
    private final SchoolFeeInvoiceService schoolFeeInvoiceService;

    public List<CollegeDueResponse> getAllCollegeDueReceipts() {
        return collegeDueService.getAllReceipts();
    }

    public List<CollegeDueResponse> getAllCollegeDueReceiptsByLevel(Level studentLevel) {
        return collegeDueService.getAllReceiptsByLevel(studentLevel);
    }

    public List<CourseFormResponse> getAllCourseFormReceipts() {
        return courseFormService.getAllReceipts();
    }

    public List<CourseFormResponse> getAllCourseFormsByLevel(Level studentLevel) {
        return courseFormService.getAllReceiptsByLevel(studentLevel);
    }

    public List<DeptDueResponse> getAllDeptDueReceipts() {
        return deptDueService.getAllReceipts();
    }

    public List<DeptDueResponse> getAllDeptDueReceiptsByLevel(Level studentLevel) {
        return deptDueService.getAllReceiptsByLevel(studentLevel);
    }

    public List<PdfResponse> getAllRemitaSchoolFeeReceipts() {
        return remitaSchoolFeeReceiptService.getAllReceipts();
    }

    public List<PdfResponse> getAllRemitaSchoolFeeReceiptsByLevel(Level studentLevel) {
        return remitaSchoolFeeReceiptService.getAllReceiptsByLevel(studentLevel);
    }

    public List<SchoolFeeInvoiceResponse> getAllSchoolFeeInvoiceReceipts() {
        return schoolFeeInvoiceService.getAllReceipts();
    }

    public List<SchoolFeeInvoiceResponse> getAllSchoolFeeInvoiceReceiptsByLevel(Level studentLevel) {
        return schoolFeeInvoiceService.getAllReceiptsByLevel(studentLevel);
    }

    public List<SchoolFeeResponse> getAllSchoolFeeReceipts() {
        return schoolFeeReceiptService.getAllReceipts();
    }

    public List<SchoolFeeResponse> getAllSchoolFeeReceiptsByLevel(Level studentLevel) {
        return schoolFeeReceiptService.getAllReceiptsByLevel(studentLevel);
    }
}
