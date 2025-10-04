package com.eduvault.controllers;

import com.eduvault.dto.ApproveRequest;
import com.eduvault.dto.ReceiptResponse;
import com.eduvault.dto.RejectRequest;
import com.eduvault.services.ReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/receipts")
@Tag(name = "Receipts Handling", description = "Endpoints for approving or rejecting receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/sch-fee/approve")
    public ResponseEntity<ReceiptResponse> approveSchoolFeeReceipt(@RequestBody ApproveRequest request){
        return ResponseEntity.ok(receiptService.approveSchoolFeeReceipt(request.getReceiptId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/sch-fee/reject")
    public ResponseEntity<ReceiptResponse> rejectSchoolFeeReceipt(@RequestBody RejectRequest request){
        return ResponseEntity.ok(receiptService.rejectSchoolFeeReceipt(request.getReceiptId(), request.getReason()));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/remita-sch-fee/approve")
    public ResponseEntity<ReceiptResponse> approveRemitaSchoolFeeReceipt(@RequestBody ApproveRequest request){
        return ResponseEntity.ok(receiptService.approveRemitaSchoolFeeReceipt(request.getReceiptId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/remita-sch-fee/reject")
    public ResponseEntity<ReceiptResponse> rejectRemitaSchoolFeeReceipt(@RequestBody RejectRequest request){
        return ResponseEntity.ok(receiptService.rejectRemitaSchoolFeeReceipt(request.getReceiptId(), request.getReason()));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/invoice/approve")
    public ResponseEntity<ReceiptResponse> approveSchoolFeeInvoice(@RequestBody ApproveRequest request){
        return ResponseEntity.ok(receiptService.approveSchoolFeeInvoice(request.getReceiptId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/invoice/reject")
    public ResponseEntity<ReceiptResponse> rejectSchoolFeeInvoice(@RequestBody RejectRequest request){
        return ResponseEntity.ok(receiptService.rejectSchoolFeeInvoice(request.getReceiptId(), request.getReason()));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/college-due/approve")
    public ResponseEntity<ReceiptResponse> approveCollegeDueReceipt(@RequestBody ApproveRequest request){
        return ResponseEntity.ok(receiptService.approveCollegeDueReceipt(request.getReceiptId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/college-due/reject")
    public ResponseEntity<ReceiptResponse> rejectCollegeDueReceipt(@RequestBody RejectRequest request){
        return ResponseEntity.ok(receiptService.rejectCollegeDueReceipt(request.getReceiptId(), request.getReason()));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/dept-due/approve")
    public ResponseEntity<ReceiptResponse> approveDeptDueReceipt(@RequestBody ApproveRequest request){
        return ResponseEntity.ok(receiptService.approveDeptDueReceipt(request.getReceiptId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/dept-due/reject")
    public ResponseEntity<ReceiptResponse> rejectDeptDueReceipt(@RequestBody RejectRequest request){
        return ResponseEntity.ok(receiptService.rejectDeptDueReceipt(request.getReceiptId(), request.getReason()));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/course-form/approve")
    public ResponseEntity<ReceiptResponse> approveCourseForm(@RequestBody ApproveRequest request){
        return ResponseEntity.ok(receiptService.approveCourseForm(request.getReceiptId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/course-form/reject")
    public ResponseEntity<ReceiptResponse> rejectCourseForm(@RequestBody RejectRequest request){
        return ResponseEntity.ok(receiptService.rejectCourseForm(request.getReceiptId(), request.getReason()));
    }
}

