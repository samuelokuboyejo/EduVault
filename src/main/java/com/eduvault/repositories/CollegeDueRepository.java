package com.eduvault.repositories;

import com.eduvault.entities.CollegeDue;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CollegeDueRepository extends JpaRepository<CollegeDue, UUID> {
    List<CollegeDue> findByUploadedBy(UUID userId);

    @Query("SELECT c.uploadedBy FROM CollegeDue c WHERE c.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<CollegeDue> findByState(Status state);
    List<CollegeDue> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);
    List<CollegeDue> findByStudentLevel(Level level);


    long countByState(Status state);

    @Query("""
        SELECT c.approvedBy AS approverId,
               COUNT(c) AS approvedCount
        FROM CollegeDue c
        WHERE c.state = 'APPROVED'
          AND c.approvedBy IS NOT NULL
        GROUP BY c.approvedBy
    """)
    List<Map<String, Object>> findApproverStats();

    @Query("""
        SELECT c.approvedBy AS staffId,
               COUNT(c) AS approvedCount
        FROM CollegeDue c
        WHERE c.state = 'APPROVED'
          AND c.uploadedAt BETWEEN :start AND :end
          AND c.approvedBy IS NOT NULL
        GROUP BY c.approvedBy
""")
    List<Map<String, Object>> findStaffApprovalStatsThisMonth(LocalDateTime start, LocalDateTime end);


    long countByUploadedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatusAndApprovedAtBetween(String status, LocalDateTime start, LocalDateTime end);

}
