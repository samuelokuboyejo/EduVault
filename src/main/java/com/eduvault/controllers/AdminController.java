package com.eduvault.controllers;

import com.eduvault.auth.service.AuthService;

import com.eduvault.auth.utils.DeleteResponse;
import com.eduvault.auth.utils.UserRoleProfileResponse;
import com.eduvault.dto.AccountStatusResponse;
import com.eduvault.dto.InvitationRequest;
import com.eduvault.dto.InvitationResponse;
import com.eduvault.entities.RoleChangeLog;
import com.eduvault.services.InvitationService;
import com.eduvault.user.enums.AccountStatus;
import com.eduvault.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Privileges", description = "Endpoints for admin related privileges")
public class AdminController {

    private final InvitationService invitationService;
    private final AuthService authService;
    private final UserService userService;


    @Operation(
            summary = "Create a new invitation",
            description = "Creates a new invitation for a user with a specific role. Only ADMINs can perform this action."

    )
    @PostMapping("/create-invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponse> createInvitation(@RequestBody InvitationRequest request) {
        InvitationResponse invitation = invitationService.createInvitation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }


    @Operation(
            summary = "Delete a user by email",
            description = "Deletes a user account from the system using their email address. Only ADMINs can perform this action.",
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "Email of the user to delete",
                            required = true,
                            example = "john.doe@example.com"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User deleted successfully",
                            content = @Content(schema = @Schema(implementation = DeleteResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only admins can delete users"
                    )
            }
    )
    @DeleteMapping("/delete/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeleteResponse> deleteUser(@PathVariable String email) {
        DeleteResponse response = authService.deleteUser(email);
        return ResponseEntity.ok().body(response);
    }


    @Operation(
            summary = "Get all privileged users (ADMIN & STAFF)",
            description = "Retrieves a list of users who have ADMIN or STAFF roles."

    )
    @GetMapping("/users/privileged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserRoleProfileResponse> > getAllPrivilegedUsers() {
        List<UserRoleProfileResponse> response = userService.getAllPrivilegedUsers();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "View all role change logs", description = "Lists all recorded user role changes with admin, old/new role, and timestamps.")
    @ApiResponse(responseCode = "200", description = "List of all role changes")
    @GetMapping("/audit/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleChangeLog>> getAllRoleChangeLogs() {
        return ResponseEntity.ok(authService.getAllRoleChangeLogs());
    }

    @PutMapping("/account/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Change account status",
            description = "Deactivate, suspend, or reactivate a user account by email."
    )
    @ApiResponse(responseCode = "200", description = "Account status updated successfully")
    public ResponseEntity<AccountStatusResponse> changeAccountStatus(@RequestParam String email,
                                                                     @RequestParam AccountStatus status) {
        AccountStatusResponse response = userService.changeAccountStatus(email, status);
        return ResponseEntity.ok(response);
    }
}