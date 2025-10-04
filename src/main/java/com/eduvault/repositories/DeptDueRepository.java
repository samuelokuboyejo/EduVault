package com.eduvault.repositories;


import com.eduvault.entities.DeptDue;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeptDueRepository extends JpaRepository<DeptDue, UUID> {
    List<DeptDue> findByUploadedBy(UUID userId);

    @Query("SELECT d.uploadedBy FROM DeptDue d WHERE d.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<DeptDue> findByState(Status state);
    List<DeptDue> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);

    List<DeptDue> findByStudentLevel(Level level);
}
