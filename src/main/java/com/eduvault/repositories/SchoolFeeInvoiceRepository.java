package com.eduvault.repositories;

import com.eduvault.entities.SchoolFeeInvoice;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SchoolFeeInvoiceRepository extends JpaRepository<SchoolFeeInvoice, UUID> {
    List<SchoolFeeInvoice> findByUploadedBy(UUID userId);

    @Query("SELECT s.uploadedBy FROM DeptDue s WHERE s.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<SchoolFeeInvoice> findByState(Status state);
    List<SchoolFeeInvoice> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);

    List<SchoolFeeInvoice> findByStudentLevel(Level level);

}
