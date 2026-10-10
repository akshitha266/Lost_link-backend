package com.lostlink.controller;

import com.lostlink.dto.request.UserStatusRequest;
import com.lostlink.dto.response.*;
import com.lostlink.security.UserPrincipal;
import com.lostlink.service.AdminService;
import com.lostlink.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints for platform moderation, statistics, and user management")
public class AdminController {

    private final AdminService adminService;
    private final ClaimService claimService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform statistical dashboard metrics")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    @Operation(summary = "Get list of all registered users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request.getActive()));
    }

    @GetMapping("/items")
    @Operation(summary = "Get all platform items including soft-removed ones")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(adminService.getAllItems());
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Moderator remove inappropriate item from public listing")
    public ResponseEntity<ApiResponse> removeItem(@PathVariable Long id) {
        adminService.removeItem(id);
        return ResponseEntity.ok(new ApiResponse(true, "Listing successfully removed by administrator."));
    }

    @GetMapping("/claims")
    @Operation(summary = "Get all submitted ownership claims across the system")
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        return ResponseEntity.ok(adminService.getAllClaims());
    }

    @PutMapping("/claims/{id}/approve")
    @Operation(summary = "Administrator approval of a claim")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(claimService.approveClaim(id, currentUser));
    }

    @PutMapping("/claims/{id}/reject")
    @Operation(summary = "Administrator rejection of a claim")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(claimService.rejectClaim(id, currentUser));
    }
}
