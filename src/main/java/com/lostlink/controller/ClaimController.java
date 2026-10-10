package com.lostlink.controller;

import com.lostlink.dto.request.ClaimRequest;
import com.lostlink.dto.response.ClaimResponse;
import com.lostlink.security.UserPrincipal;
import com.lostlink.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Ownership claims submission and verification endpoints")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @Operation(summary = "Submit an ownership claim with a verification answer")
    public ResponseEntity<ClaimResponse> createClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ClaimResponse response = claimService.createClaim(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @Operation(summary = "Get all claims submitted by the current authenticated user")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(claimService.getMyClaims(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a specific claim (Claimant, Owner, or Admin only)")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(claimService.getClaimById(id, currentUser));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve an ownership claim (Item owner or Admin)")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(claimService.approveClaim(id, currentUser));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject an ownership claim (Item owner or Admin)")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(claimService.rejectClaim(id, currentUser));
    }
}
