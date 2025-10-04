package com.eduvault.repositories;

import com.eduvault.entities.RemitaSchoolFeeReceipt;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RemitaSchoolFeeReceiptRepository extends JpaRepository<RemitaSchoolFeeReceipt, UUID> {
    List<RemitaSchoolFeeReceipt> findByUploadedBy(UUID userId);

    @Query("SELECT s.uploadedBy FROM DeptDue s WHERE s.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<RemitaSchoolFeeReceipt> findByState(Status state);
    List<RemitaSchoolFeeReceipt> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);

    List<RemitaSchoolFeeReceipt> findByStudentLevel(Level level);

}
