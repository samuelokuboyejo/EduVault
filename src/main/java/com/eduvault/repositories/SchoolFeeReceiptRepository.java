package com.eduvault.repositories;

import com.eduvault.entities.SchoolFeeReceipt;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SchoolFeeReceiptRepository extends JpaRepository<SchoolFeeReceipt, UUID> {
    List<SchoolFeeReceipt> findByUploadedBy(UUID userId);

    @Query("SELECT s.uploadedBy FROM SchoolFeeReceipt s WHERE s.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<SchoolFeeReceipt> findByState(Status state);
    List<SchoolFeeReceipt> findByStudentLevel(Level level);
    List<SchoolFeeReceipt> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);

}
