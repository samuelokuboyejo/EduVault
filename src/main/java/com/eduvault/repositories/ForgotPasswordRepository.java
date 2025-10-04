package com.eduvault.repositories;

import com.eduvault.user.User;
import com.eduvault.user.utils.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, UUID> {
    Optional<ForgotPassword> findByOtp(String token);

    Optional<ForgotPassword> findByUser(User user);
}