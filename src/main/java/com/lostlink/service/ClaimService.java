package com.lostlink.service;

import com.lostlink.dto.request.ClaimRequest;
import com.lostlink.dto.response.ClaimResponse;
import com.lostlink.entity.*;
import com.lostlink.exception.BadRequestException;
import com.lostlink.exception.ForbiddenException;
import com.lostlink.exception.ResourceNotFoundException;
import com.lostlink.repository.ClaimRepository;
import com.lostlink.repository.ItemRepository;
import com.lostlink.repository.UserRepository;
import com.lostlink.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClaimResponse createClaim(ClaimRequest request, UserPrincipal currentUser) {
        // 1. Verify item exists
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        // 2. Verify item is claimable
        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new BadRequestException("This item is not currently active for claims (Status: " + item.getStatus() + ").");
        }

        if (item.getUserId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot claim an item you posted yourself.");
        }

        // 3. Prevent duplicate pending claims from the same claimant on this item
        Optional<Claim> existingClaim = claimRepository.findByItemIdAndClaimantIdAndStatus(
                item.getId(), currentUser.getId(), ClaimStatus.PENDING
        );
        if (existingClaim.isPresent()) {
            throw new BadRequestException("You already have an active pending claim for this item.");
        }

        // 4. Secure verification comparison check (for log/audit review)
        String storedAnswer = item.getVerificationAnswer();
        String submittedAnswer = request.getAnswer().trim();
        boolean answersMatch = storedAnswer != null && storedAnswer.trim().equalsIgnoreCase(submittedAnswer);

        log.info("Claim submitted on Item #{} by User #{}. Verification preliminary match: {}",
                item.getId(), currentUser.getId(), answersMatch);

        // 5. Create the claim (stored answer not exposed)
        Claim claim = Claim.builder()
                .itemId(item.getId())
                .claimantId(currentUser.getId())
                .answer(submittedAnswer)
                .status(ClaimStatus.PENDING)
                .build();

        Claim savedClaim = claimRepository.save(claim);
        return mapToResponse(savedClaim, item, currentUser.getName(), currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getMyClaims(Long claimantId) {
        List<Claim> claims = claimRepository.findByClaimantId(claimantId);
        return claims.stream()
                .map(this::enrichClaimResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(Long claimId, UserPrincipal currentUser) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        Item item = itemRepository.findById(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item associated with claim not found"));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isClaimant = claim.getClaimantId().equals(currentUser.getId());
        boolean isItemOwner = item.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isClaimant && !isItemOwner) {
            throw new ForbiddenException("You are not authorized to view this claim.");
        }

        return enrichClaimResponse(claim);
    }

    @Transactional
    public ClaimResponse approveClaim(Long claimId, UserPrincipal currentUser) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        Item item = itemRepository.findById(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item associated with claim not found"));

        authorizeClaimAction(item, currentUser);

        claim.setStatus(ClaimStatus.APPROVED);
        claimRepository.save(claim);

        // Mark item as RESOLVED when claim is approved
        item.setStatus(ItemStatus.RESOLVED);
        itemRepository.save(item);

        log.info("Claim #{} APPROVED. Item #{} marked as RESOLVED.", claimId, item.getId());
        return enrichClaimResponse(claim);
    }

    @Transactional
    public ClaimResponse rejectClaim(Long claimId, UserPrincipal currentUser) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        Item item = itemRepository.findById(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item associated with claim not found"));

        authorizeClaimAction(item, currentUser);

        claim.setStatus(ClaimStatus.REJECTED);
        claimRepository.save(claim);

        log.info("Claim #{} REJECTED.", claimId);
        return enrichClaimResponse(claim);
    }

    private void authorizeClaimAction(Item item, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isItemOwner = item.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isItemOwner) {
            throw new ForbiddenException("Only the item owner or an administrator can approve/reject claims.");
        }
    }

    public ClaimResponse enrichClaimResponse(Claim claim) {
        Item item = itemRepository.findById(claim.getItemId()).orElse(null);
        User claimant = userRepository.findById(claim.getClaimantId()).orElse(null);

        String itemTitle = item != null ? item.getTitle() : "Item #" + claim.getItemId();
        ItemType itemType = item != null ? item.getType() : ItemType.LOST;
        String claimantName = claimant != null ? claimant.getName() : "Unknown User";
        String claimantEmail = claimant != null ? claimant.getEmail() : "Unknown Email";

        return mapToResponse(claim, itemTitle, itemType, claimantName, claimantEmail);
    }

    public ClaimResponse mapToResponse(Claim claim, Item item, String claimantName, String claimantEmail) {
        return mapToResponse(claim, item.getTitle(), item.getType(), claimantName, claimantEmail);
    }

    public ClaimResponse mapToResponse(Claim claim, String itemTitle, ItemType itemType, String claimantName, String claimantEmail) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .itemId(claim.getItemId())
                .itemTitle(itemTitle)
                .itemType(itemType)
                .claimantId(claim.getClaimantId())
                .claimantName(claimantName)
                .claimantEmail(claimantEmail)
                .answer(claim.getAnswer())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
