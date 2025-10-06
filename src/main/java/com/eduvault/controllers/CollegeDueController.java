package com.eduvault.controllers;

import com.eduvault.dto.FileDownloadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eduvault.auth.utils.LevelRequest;
import com.eduvault.dto.CollegeDueResponse;
import com.eduvault.services.CollegeDueService;
import com.eduvault.user.UserInfoUserDetails;
import com.eduvault.user.enums.Level;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/college-due")
@Tag(name = "College Due", description = "Endpoints for handling college due receipts")
public class CollegeDueController {
    private final CollegeDueService collegeDueService;

    @Operation(
            summary = "Upload a college due receipt",
            description = "Uploads a PDF receipt for processing and validation. A user can only upload a new receipt if their last one was rejected.",
            parameters = {
                    @Parameter(
                            name = "file",
                            description = "The receipt PDF file to upload",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Receipt uploaded successfully",
                            content = @Content(schema = @Schema(implementation = CollegeDueResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user not logged in"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request - A valid file must be provided"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - User already has a pending/approved receipt"
                    )
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CollegeDueResponse> uploadReceipt(@RequestPart("file") MultipartFile file,
                                                            @RequestPart("data") String requestJson,
                                                            @AuthenticationPrincipal UserInfoUserDetails principal) throws IOException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getUsername();
        LevelRequest request = new ObjectMapper().readValue(requestJson, LevelRequest.class);
        CollegeDueResponse response = collegeDueService.uploadReceipt(file, email, request.getStudentLevel());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    @Operation(
            summary = "Get all receipts created by user",
            description = "Allows an authenticated user to retrieve all receipts" + "⚠️ Requires a valid Bearer access token in the Authorization header.",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    public ResponseEntity<List<CollegeDueResponse>> getMyListings(@AuthenticationPrincipal UserInfoUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getUsername();
        return ResponseEntity.ok(collegeDueService.getAllReceiptsByUser(email));
    }

    @GetMapping("/{studentLevel}/me")
    @Operation(
            summary = "Get all receipts by level created by user",
            description = "Allows an authenticated user to retrieve all receipts by level" + "⚠️ Requires a valid Bearer access token in the Authorization header.",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    public ResponseEntity<List<CollegeDueResponse>> getMyReceiptByLevel(@AuthenticationPrincipal UserInfoUserDetails principal, @PathVariable Level studentLevel) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getUsername();
        return ResponseEntity.ok(collegeDueService.getAllReceiptsByLevelByUser(studentLevel, email));
    }

    @GetMapping("/me/download")
    public ResponseEntity<byte[]> downloadUserReceipt(@AuthenticationPrincipal UserInfoUserDetails principal)
            throws IOException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getUsername();
        FileDownloadResponse file = collegeDueService.downloadReceiptByUser(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.getFileContent());
    }

    @Operation(
            summary = "Download approved receipts as ZIP",
            description = "Fetches all receipts with status APPROVED from the database, downloads their PDFs from Cloudinary, and returns them as a single ZIP file.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ZIP file containing all approved receipts",
                            content = @Content(mediaType = "application/octet-stream")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user not logged in"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - only ADMIN and STAFF can access this endpoint"
                    )
            }
    )
    @GetMapping("/approved/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> downloadApprovedReceipts() throws IOException {
        byte[] zipBytes = collegeDueService.downloadApprovedReceiptsAsZip();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=approved-college-due-receipts.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }
}
