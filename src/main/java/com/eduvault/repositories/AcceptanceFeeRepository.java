package com.eduvault.repositories;

import com.eduvault.entities.AcceptanceFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcceptanceFeeRepository extends JpaRepository<AcceptanceFee, UUID> {
}
