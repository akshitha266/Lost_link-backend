package com.lostlink.controller;

import com.lostlink.dto.request.ItemRequest;
import com.lostlink.dto.response.ApiResponse;
import com.lostlink.dto.response.ItemResponse;
import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import com.lostlink.security.UserPrincipal;
import com.lostlink.service.ItemService;
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
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Lost and Found items catalog and management")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Get all active items or search by filters")
    public ResponseEntity<List<ItemResponse>> getItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword) {

        if (type != null || category != null || status != null || location != null || keyword != null) {
            return ResponseEntity.ok(itemService.searchItems(type, category, status, location, keyword));
        }
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/search")
    @Operation(summary = "Explicit search endpoint with filter query parameters")
    public ResponseEntity<List<ItemResponse>> searchItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(itemService.searchItems(type, category, status, location, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific item details (excludes private verification answer)")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    @Operation(summary = "Post a new lost or found item")
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ItemResponse response = itemService.createItem(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an item (Owner or Admin only)")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(itemService.updateItem(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item listing (Owner or Admin only)")
    public ResponseEntity<ApiResponse> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        itemService.deleteItem(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Item deleted successfully."));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all items posted by the logged-in user")
    public ResponseEntity<List<ItemResponse>> getMyItems(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(itemService.getItemsByUserId(currentUser.getId()));
    }
}
