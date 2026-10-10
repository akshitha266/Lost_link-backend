package com.lostlink.service;

import com.lostlink.dto.request.ClaimRequest;
import com.lostlink.dto.response.ClaimResponse;
import com.lostlink.entity.*;
import com.lostlink.exception.BadRequestException;
import com.lostlink.repository.ClaimRepository;
import com.lostlink.repository.ItemRepository;
import com.lostlink.repository.UserRepository;
import com.lostlink.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private UserPrincipal claimant;
    private Item activeFoundItem;
    private ClaimRequest claimRequest;

    @BeforeEach
    void setUp() {
        claimant = UserPrincipal.builder()
                .id(2L)
                .name("Bob")
                .email("bob@college.edu")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .active(true)
                .build();

        activeFoundItem = Item.builder()
                .id(10L)
                .title("Blue Umbrella")
                .status(ItemStatus.ACTIVE)
                .userId(1L) // Posted by user 1
                .verificationAnswer("Wooden handle with duck head")
                .build();

        claimRequest = ClaimRequest.builder()
                .itemId(10L)
                .answer("Wooden handle with duck head")
                .build();
    }

    @Test
    void createClaim_Success() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(activeFoundItem));
        when(claimRepository.findByItemIdAndClaimantIdAndStatus(10L, 2L, ClaimStatus.PENDING))
                .thenReturn(Optional.empty());

        Claim savedClaim = Claim.builder()
                .id(100L)
                .itemId(10L)
                .claimantId(2L)
                .answer("Wooden handle with duck head")
                .status(ClaimStatus.PENDING)
                .build();

        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponse response = claimService.createClaim(claimRequest, claimant);

        assertNotNull(response);
        assertEquals(ClaimStatus.PENDING, response.getStatus());
        assertEquals("Blue Umbrella", response.getItemTitle());
    }

    @Test
    void createClaim_OnOwnItem_ThrowsBadRequestException() {
        // User 2 tries to claim an item posted by User 2
        activeFoundItem.setUserId(2L);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(activeFoundItem));

        assertThrows(BadRequestException.class, () -> claimService.createClaim(claimRequest, claimant));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void approveClaim_MarksItemAsResolved() {
        Claim claim = Claim.builder()
                .id(100L)
                .itemId(10L)
                .claimantId(2L)
                .status(ClaimStatus.PENDING)
                .build();

        when(claimRepository.findById(100L)).thenReturn(Optional.of(claim));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(activeFoundItem));

        UserPrincipal adminUser = UserPrincipal.builder()
                .id(99L)
                .name("Admin")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(itemRepository.save(any(Item.class))).thenReturn(activeFoundItem);

        ClaimResponse response = claimService.approveClaim(100L, adminUser);

        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        assertEquals(ItemStatus.RESOLVED, activeFoundItem.getStatus());
        verify(itemRepository, times(1)).save(activeFoundItem);
    }
}
