package com.eduvault.user.service;

import com.eduvault.auth.utils.AuthResponse;
import com.eduvault.auth.utils.UserRoleProfileResponse;
import com.eduvault.dto.AccountStatusResponse;
import com.eduvault.exceptions.UserNotFoundException;
import com.eduvault.services.EmailService;
import com.eduvault.user.User;
import com.eduvault.user.dto.UpdateProfileRequest;
import com.eduvault.user.dto.UserProfileDto;
import com.eduvault.user.enums.AccountStatus;
import com.eduvault.user.enums.UserRole;
import com.eduvault.user.repo.UserRepository;
import com.eduvault.user.utils.UploadResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;


    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return mapToUserProfileResponse(user);
    }

    private UserProfileDto mapToUserProfileResponse(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getImageUrl(),
                user.getRole()
        );
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest req, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setFirstName(req.firstName());

        if (file != null && !file.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.upload(file);
            user.setImageUrl(uploadResponse.getSecureUrl());
        }

        userRepository.save(user);

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getImageUrl(),
                user.getRole()
        );
    }

    public List<UserRoleProfileResponse> getAllPrivilegedUsers(){
        List<User> users = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.STAFF));
        return users.stream()
                .map(user -> UserRoleProfileResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .imageUrl(user.getImageUrl())
                        .role(user.getRole())
                        .lastLogin(user.getLastLogin())
                        .dateJoined(user.getCreatedAt())
                        .build())

                .toList();
    }

    public AccountStatusResponse changeAccountStatus(String email, AccountStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setAccountStatus(status);
        userRepository.save(user);
        emailService.sendAccountStatusEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                status
        );
        return AccountStatusResponse.builder()
                .message( "User account " + status.name().toLowerCase() + " successfully.")
                .build();
    }
}
