package com.eduvault.repositories;

import com.eduvault.entities.SUGReceipt;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SUGReceiptRepository extends JpaRepository<SUGReceipt, UUID> {
    List<SUGReceipt> findByUploadedBy(UUID userId);

    @Query("SELECT s.uploadedBy FROM DeptDue s WHERE s.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<SUGReceipt> findByState(Status state);

    List<SUGReceipt> findByStudentLevel(Level level);
    List<SUGReceipt> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);

}
