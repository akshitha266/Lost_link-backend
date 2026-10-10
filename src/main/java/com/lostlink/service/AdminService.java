package com.lostlink.service;

import com.lostlink.dto.response.*;
import com.lostlink.entity.*;
import com.lostlink.exception.ResourceNotFoundException;
import com.lostlink.repository.ClaimRepository;
import com.lostlink.repository.ItemRepository;
import com.lostlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final AuthService authService;
    private final ItemService itemService;
    private final ClaimService claimService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalLost = itemRepository.countByType(ItemType.LOST);
        long totalFound = itemRepository.countByType(ItemType.FOUND);
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long resolvedItems = itemRepository.countByStatus(ItemStatus.RESOLVED);

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalLostItems(totalLost)
                .totalFoundItems(totalFound)
                .pendingClaims(pendingClaims)
                .resolvedItems(resolvedItems)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(active);
        User updated = userRepository.save(user);
        return authService.mapToUserResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(itemService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        // Soft remove
        item.setStatus(ItemStatus.REMOVED);
        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(claimService::enrichClaimResponse)
                .collect(Collectors.toList());
    }
}
