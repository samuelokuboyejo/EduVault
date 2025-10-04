package com.eduvault.bootstrap;

import com.eduvault.auth.service.JwtService;
import com.eduvault.auth.service.RefreshTokenService;
import com.eduvault.user.User;
import com.eduvault.user.enums.UserRole;
import com.eduvault.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            if (userRepository.findByRole(UserRole.ADMIN).isEmpty()) {
                User admin = User.builder()
                        .email("admin@university.edu")
                        .password(encoder.encode("SuperSecurePassword123"))
                        .role(UserRole.ADMIN)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                var accessToken = jwtService.generateToken(admin);
                var refreshToken = refreshTokenService.createRefreshToken(admin.getEmail());
                System.out.println("✅ Default admin account created! " + "/n accessToken: " + accessToken + "/n refreshToken: " + refreshToken);
            }
        };
    }
}

