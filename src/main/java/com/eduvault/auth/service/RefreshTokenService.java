package com.eduvault.auth.service;


import com.eduvault.auth.utils.RefreshToken;
import com.eduvault.auth.utils.RefreshTokenResponse;
import com.eduvault.user.User;
import com.eduvault.user.repo.RefreshTokenRepository;
import com.eduvault.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    private static final long REFRESH_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 hours

    public RefreshToken createRefreshToken(String identifier) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByMatricNumber(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        if (user.getRefreshToken() != null) {
            refreshTokenRepository.delete(user.getRefreshToken());
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(UUID.randomUUID().toString())
                .expirationTime(Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY))
                .build();

        refreshToken.setUser(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return refreshToken;
    }


    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken refToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refToken.getExpirationTime().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refToken);
            throw new RuntimeException("Refresh Token expired");
        }

        return refToken;
    }

    public RefreshTokenResponse refreshAccessToken(String requestRefreshToken) {
        // validate refresh token
        RefreshToken refToken = verifyRefreshToken(requestRefreshToken);
        User user = refToken.getUser();

        // delete the old one
        refreshTokenRepository.delete(refToken);

        // create new refresh token
        RefreshToken newRefreshToken = createRefreshToken(user.getEmail());

        // generate new access token
        String newAccessToken = jwtService.generateToken(user);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken.getRefreshToken());

    }

}
