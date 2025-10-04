package com.eduvault.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.eduvault.auth.service.AuthService;
import com.eduvault.auth.service.ForgotPasswordService;
import com.eduvault.auth.service.RefreshTokenService;
import com.eduvault.auth.utils.*;
import com.eduvault.user.utils.ResetLinkRequest;
import com.eduvault.user.utils.ResetLinkResponse;
import com.eduvault.user.utils.ResetPasswordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {
    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns authentication tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "User already exists", content = @Content)
    })
    @PostMapping("/register/student")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody StudentRegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerStudent(request));
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates user and returns JWT tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }


    @Operation(
            summary = "Login with Google OAuth2",
            description = "Authenticates a user via Google Sign-In. The client (Flutter app) must obtain a valid Google ID token from the Google Sign-In SDK and send it to this endpoint. " +
                    "The backend verifies the token with Google's servers, creates a user account if it does not exist, and returns an authentication response containing access/refresh tokens."
    )
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody Map<String, String> body) throws Exception {
        String idToken = body.get("idToken");
        AuthResponse response = authService.loginWithGoogle(idToken);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Takes a valid refresh token and issues a new access token and refresh token pair."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully refreshed access token",
            content = @Content(
                    schema = @Schema(implementation = RefreshTokenResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired refresh token",
            content = @Content
    )
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The refresh token request containing the refresh token string",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RefreshTokenRequest.class)
                    )
            )
            @RequestBody RefreshTokenRequest request
    ) {
        RefreshTokenResponse tokenPair = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenPair);
    }


    @Operation(
            summary = "Register using invitation token",
            description = "Registers a new user based on an invitation link. " +
                    "The invitation must be valid (not expired or used). " +
                    "The role is automatically assigned from the invitation.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully registered and JWT tokens returned",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or expired invitation token"
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerRole(@RequestPart("data") String requestJson, @RequestParam("token") String token, @RequestPart("file") MultipartFile file) throws IOException {
        RegisterRequest request = new ObjectMapper().readValue(requestJson, RegisterRequest.class);
        AuthResponse response = authService.registerWithInvitation(token, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Request password reset", description = "Send a password reset link to the user's email.")
    @ApiResponse(responseCode = "200", description = "Password reset link sent")
    @PostMapping("/forgot-password")
    public ResponseEntity<ResetLinkResponse> forgotPassword(@RequestBody ResetLinkRequest request) {
        ResetLinkResponse response = forgotPasswordService.sendResetLink(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password", description = "Reset password using the reset token.")
    @ApiResponse(responseCode = "200", description = "Password successfully reset")
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestParam String token, @RequestBody ResetPasswordPasswordRequest request) {
        ResetPasswordResponse response = forgotPasswordService.resetPassword(token, request.getNewPassword());
        return ResponseEntity.ok(response);
    }
}
