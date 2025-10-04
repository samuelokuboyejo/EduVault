package com.eduvault.repositories;

import com.eduvault.entities.CourseForm;
import com.eduvault.user.enums.Level;
import com.eduvault.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseFormRepository extends JpaRepository<CourseForm, UUID> {
    List<CourseForm> findByUploadedBy(UUID userId);

    @Query("SELECT c.uploadedBy FROM CourseForm c WHERE c.id = :id")
    UUID findUploadedByById(@Param("id") UUID id);

    List<CourseForm> findByState(Status state);
    List<CourseForm> findByStudentLevelAndUploadedBy(Level studentLevel, UUID uploadedBy);

    List<CourseForm> findByStudentLevel(Level level);

}
