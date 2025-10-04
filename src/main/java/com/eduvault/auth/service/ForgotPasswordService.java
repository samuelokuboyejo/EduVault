package com.eduvault.auth.service;

import com.eduvault.repositories.ForgotPasswordRepository;
import com.eduvault.services.EmailService;
import com.eduvault.user.User;
import com.eduvault.user.repo.UserRepository;
import com.eduvault.user.utils.ForgotPassword;
import com.eduvault.user.utils.ResetLinkResponse;
import com.eduvault.user.utils.ResetPasswordResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final ForgotPasswordRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public ResetLinkResponse sendResetLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ForgotPassword existing = tokenRepository.findByUser(user).orElse(null);


        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        if (existing != null) {
            existing.setOtp(token);
            existing.setExpirationTime(expiry);
            existing.setUsed(false);
            existing.setUpdatedAt(LocalDateTime.now());
            tokenRepository.save(existing);
        } else {
            ForgotPassword resetToken = ForgotPassword.builder()
                    .otp(token)
                    .user(user)
                    .expirationTime(expiry)
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            tokenRepository.save(resetToken);
        }



        String resetLink = baseUrl + "auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
        return ResetLinkResponse.builder()
                .resetLink(resetLink)
                .build();
    }

    public ResetPasswordResponse resetPassword(String token, String newPassword) {
        ForgotPassword resetToken = tokenRepository.findByOtp(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isUsed() || resetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getFirstName());

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        return ResetPasswordResponse.builder()
                .message("Password successfully reset!")
                .build();
    }
}
