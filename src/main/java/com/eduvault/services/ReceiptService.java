package com.eduvault.services;

import com.eduvault.dto.ReceiptResponse;
import com.eduvault.entities.*;
import com.eduvault.repositories.*;
import com.eduvault.user.User;
import com.eduvault.user.enums.Status;
import com.eduvault.user.repo.UserRepository;
import com.eduvault.utils.NotificationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final DeptDueService deptDueService;
    private final CollegeDueService collegeDueService;
    private final RemitaSchoolFeeReceiptService remitaSchoolFeeReceiptService;
    private final SchoolFeeReceiptService schoolFeeReceiptService;
    private final SchoolFeeInvoiceService schoolFeeInvoiceService;
    private final EmailService emailService;
    private final CourseFormService courseFormService;
    private final CourseFormRepository courseFormRepository;
    private final SchoolFeeReceiptRepository schoolFeeReceiptRepository;
    private final DeptDueRepository deptDueRepository;
    private final CollegeDueRepository collegeDueRepository;
    private final SchoolFeeInvoiceRepository schoolFeeInvoiceRepository;
    private final RemitaSchoolFeeReceiptRepository remitaSchoolFeeReceiptRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;



    public ReceiptResponse approveSchoolFeeReceipt(UUID receiptId){
        SchoolFeeReceipt receipt = schoolFeeReceiptRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.APPROVED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = schoolFeeReceiptService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_APPROVED, "Your School Fee Receipt has been received and approved by the school");
        return ReceiptResponse.builder()
                .message("Receipt Approved Successfully!")
                .build();

    }

    public ReceiptResponse rejectSchoolFeeReceipt(UUID receiptId, String reason){
        SchoolFeeReceipt receipt = schoolFeeReceiptRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.REJECTED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = schoolFeeReceiptService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_REJECTED, "Your School Fee Receipt has been rejected, something seems to be wrong with the receipt you uploaded; "+ reason);
        String reuploadLink = "go to the app";
        String document = "School Fee Receipt";
        emailService.sendReceiptRejectionEmail(user.getEmail(), reason, reuploadLink, user.getMatricNumber(), document);
        return ReceiptResponse.builder()
                .message("Receipt Rejected!")
                .build();

    }


    public ReceiptResponse approveSchoolFeeInvoice(UUID receiptId){
        SchoolFeeInvoice receipt = schoolFeeInvoiceRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.APPROVED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = schoolFeeInvoiceService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_APPROVED, "Your School Fee Invoice has been received and approved by the school");
        return ReceiptResponse.builder()
                .message("Receipt Approved Successfully!")
                .build();

    }

    public ReceiptResponse rejectSchoolFeeInvoice(UUID receiptId, String reason){
        SchoolFeeInvoice receipt = schoolFeeInvoiceRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.REJECTED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = schoolFeeInvoiceService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_REJECTED, "Your School Fee Invoice has been rejected, something seems to be wrong with the receipt you uploaded; "+ reason);
        String reuploadLink = "go to the app";
        String document = "School Fee Invoice";
        emailService.sendReceiptRejectionEmail(user.getEmail(), reason, reuploadLink, user.getMatricNumber(), document);
        return ReceiptResponse.builder()
                .message("Receipt Rejected!")
                .build();

    }

    public ReceiptResponse approveRemitaSchoolFeeReceipt(UUID receiptId){
        RemitaSchoolFeeReceipt receipt = remitaSchoolFeeReceiptRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.APPROVED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = remitaSchoolFeeReceiptService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_APPROVED, "Your Remita School Fee Receipt has been received and approved by the school");
        return ReceiptResponse.builder()
                .message("Receipt Approved Successfully!")
                .build();

    }

    public ReceiptResponse rejectRemitaSchoolFeeReceipt(UUID receiptId, String reason){
        RemitaSchoolFeeReceipt receipt = remitaSchoolFeeReceiptRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.REJECTED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = remitaSchoolFeeReceiptService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_REJECTED, "Your Remita School Fee Receipt has been rejected, something seems to be wrong with the receipt you uploaded; "+ reason);
        String reuploadLink = "go to the app";
        String document = "Remita School Fee Receipt";
        emailService.sendReceiptRejectionEmail(user.getEmail(), reason, reuploadLink, user.getMatricNumber(), document);
        return ReceiptResponse.builder()
                .message("Receipt Rejected!")
                .build();

    }

    public ReceiptResponse approveDeptDueReceipt(UUID receiptId){
        DeptDue receipt = deptDueRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.APPROVED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = deptDueService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_APPROVED, "Your Dept Due Receipt has been received and approved by the school");
        return ReceiptResponse.builder()
                .message("Receipt Approved Successfully!")
                .build();

    }

    public ReceiptResponse rejectDeptDueReceipt(UUID receiptId, String reason){
        DeptDue receipt = deptDueRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.REJECTED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = deptDueService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_REJECTED, "Your Dept Due Receipt has been rejected, something seems to be wrong with the receipt you uploaded; "+ reason);
        String reuploadLink = "go to the app";
        String document = "Dept Due Receipt";
        emailService.sendReceiptRejectionEmail(user.getEmail(), reason, reuploadLink, user.getMatricNumber(), document);
        return ReceiptResponse.builder()
                .message("Receipt Rejected!")
                .build();

    }

    public ReceiptResponse approveCollegeDueReceipt(UUID receiptId){
        CollegeDue receipt = collegeDueRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.APPROVED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = collegeDueService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_APPROVED, "Your College Due Receipt has been received and approved by the school");
        return ReceiptResponse.builder()
                .message("Receipt Approved Successfully!")
                .build();

    }

    public ReceiptResponse rejectCollegeDueReceipt(UUID receiptId, String reason){
        CollegeDue receipt = collegeDueRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.REJECTED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = collegeDueService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_REJECTED, "Your College Due Receipt has been rejected, something seems to be wrong with the receipt you uploaded; "+ reason);
        String reuploadLink = "go to the app";
        String document = "College Due Receipt";
        emailService.sendReceiptRejectionEmail(user.getEmail(), reason, reuploadLink, user.getMatricNumber(), document);
        return ReceiptResponse.builder()
                .message("Receipt Rejected!")
                .build();

    }

    public ReceiptResponse approveCourseForm(UUID receiptId){
        CourseForm receipt = courseFormRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.APPROVED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = courseFormService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_APPROVED, "Your Course Form has been received and approved by the school");
        return ReceiptResponse.builder()
                .message("Receipt Approved Successfully!")
                .build();

    }

    public ReceiptResponse rejectCourseForm(UUID receiptId, String reason){
        CourseForm receipt = courseFormRepository.findById(receiptId)  .orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        receipt.setState(Status.REJECTED);
        receipt.setUpdatedAt(LocalDateTime.now());
        UUID userId = courseFormService.getUploadedBy(receiptId);
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Receipt not found"));
        notificationService.createNotification(user.getEmail(), NotificationUtils.DOCUMENT_REJECTED, "Your Course Form has been rejected, something seems to be wrong with the receipt you uploaded; "+ reason);
        String reuploadLink = "go to the app";
        String document = "Course Form";
        emailService.sendReceiptRejectionEmail(user.getEmail(), reason, reuploadLink, user.getMatricNumber(), document);
        return ReceiptResponse.builder()
                .message("Receipt Rejected!")
                .build();

    }
}
