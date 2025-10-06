package com.eduvault.repositories;

import com.eduvault.entities.RoleChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, UUID> {
}
