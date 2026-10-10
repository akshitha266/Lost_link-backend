package com.lostlink.controller;

import com.lostlink.dto.request.PasswordChangeRequest;
import com.lostlink.dto.request.UserUpdateRequest;
import com.lostlink.dto.response.ApiResponse;
import com.lostlink.dto.response.UserResponse;
import com.lostlink.security.UserPrincipal;
import com.lostlink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Users & Profile", description = "User profile and account management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get currently authenticated user's profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user's profile information")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), request));
    }

    @PutMapping("/profile/password")
    @Operation(summary = "Change account password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully."));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user information by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
