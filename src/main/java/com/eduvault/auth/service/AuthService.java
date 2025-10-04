package com.eduvault.auth.service;

import com.eduvault.auth.utils.*;
import com.eduvault.entities.Invitation;
import com.eduvault.repositories.InvitationRepository;
import com.eduvault.user.User;
import com.eduvault.user.enums.UserRole;
import com.eduvault.user.repo.UserRepository;
import com.eduvault.user.service.CloudinaryService;
import com.eduvault.user.utils.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final GoogleAuthService googleAuthService;
    private final CloudinaryService cloudinaryService;
    private final InvitationRepository invitationRepository;


    public AuthResponse registerStudent(StudentRegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        if (userRepository.existsByMatricNumber(request.getMatricNumber())) {
            throw new RuntimeException("Name already exists!");
        }
        var user = User.builder()
                .matricNumber(request.getMatricNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);
        var accessToken = jwtService.generateToken(savedUser);
        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }

    public AuthResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getIdentifier(),
                        loginRequest.getPassword())
        );

        var user = userRepository.findByEmail(loginRequest.getIdentifier())
                .or(() -> userRepository.findByMatricNumber(loginRequest.getIdentifier()))
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());

        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(loginRequest.getIdentifier());

        return AuthResponse.builder()

                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }



    public AuthResponse loginWithGoogle(String idToken) throws Exception {
        var payload = googleAuthService.verifyToken(idToken);

        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Google account has no email");
        }
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String picture = (String) payload.get("picture");

        var user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        user.setLastLogin(LocalDateTime.now());

        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }

    public AuthResponse registerWithInvitation(String token, RegisterRequest request, MultipartFile file)throws IOException {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation"));

        if (invitation.isUsed() || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation expired or already used");
        }
        UploadResponse uploadResponse = cloudinaryService.upload(file);
        User user = User.builder()
                .email(invitation.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(invitation.getInvitedRole())
                .imageUrl(uploadResponse.getSecureUrl())
                .createdAt(LocalDateTime.now())
                .build();

        invitation.setUsed(true);
        invitationRepository.save(invitation);

        User savedUser = userRepository.save(user);
        var accessToken = jwtService.generateToken(savedUser);
        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();

    }

    public DeleteResponse deleteUser(String email){
        var user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
        return DeleteResponse.builder()
                .message("Account deleted successfully")
                .build();
    }

}

