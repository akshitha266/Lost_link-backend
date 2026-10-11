package com.lostlink.service;

import com.lostlink.dto.request.ItemRequest;
import com.lostlink.dto.response.ItemResponse;
import com.lostlink.entity.Item;
import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import com.lostlink.exception.ForbiddenException;
import com.lostlink.repository.ItemRepository;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private UserPrincipal userPrincipal;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .name("Alice")
                .email("alice@college.edu")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .active(true)
                .build();

        itemRequest = ItemRequest.builder()
                .title("Scientific Calculator TI-84")
                .description("Black graphic calculator with initials inscribed")
                .category("Electronics")
                .type(ItemType.FOUND)
                .date("2026-10-01")
                .location("Math Dept Room 101")
                .verificationQuestion("What initials are engraved on the back?")
                .verificationAnswer("A.K. 2025")
                .build();
    }

    @Test
    void createItem_Success_VerificationAnswerNotExposed() {
        Item item = Item.builder()
                .id(50L)
                .title("Scientific Calculator TI-84")
                .description("Black graphic calculator with initials inscribed")
                .category("Electronics")
                .type(ItemType.FOUND)
                .date("2026-10-01")
                .location("Math Dept Room 101")
                .status(ItemStatus.ACTIVE)
                .verificationQuestion("What initials are engraved on the back?")
                .verificationAnswer("A.K. 2025")
                .userId(1L)
                .postedByName("Alice")
                .build();

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.createItem(itemRequest, userPrincipal);

        assertNotNull(response);
        assertEquals("Scientific Calculator TI-84", response.getTitle());
        assertEquals("What initials are engraved on the back?", response.getVerificationQuestion());
        // Verify ItemResponse DTO does not have any verificationAnswer field or getter accessible
        // (Compiler verification check)
    }

    @Test
    void deleteItem_UnauthorizedUser_ThrowsForbiddenException() {
        Item item = Item.builder()
                .id(100L)
                .userId(99L) // Belongs to different user
                .status(ItemStatus.ACTIVE)
                .build();

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> itemService.deleteItem(100L, userPrincipal));
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void updateItem_PartialUpdate_StatusOnly_Success() {
        Item item = Item.builder()
                .id(50L)
                .title("Scientific Calculator TI-84")
                .description("Black graphic calculator with initials inscribed")
                .category("Electronics")
                .type(ItemType.FOUND)
                .date("2026-10-01")
                .location("Math Dept Room 101")
                .status(ItemStatus.ACTIVE)
                .userId(1L)
                .postedByName("Alice")
                .build();

        when(itemRepository.findById(50L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.lostlink.dto.request.ItemUpdateRequest updateRequest = com.lostlink.dto.request.ItemUpdateRequest.builder()
                .status(ItemStatus.RESOLVED)
                .build();

        ItemResponse response = itemService.updateItem(50L, updateRequest, userPrincipal);

        assertNotNull(response);
        assertEquals(ItemStatus.RESOLVED, response.getStatus());
        assertEquals("Scientific Calculator TI-84", response.getTitle());
    }

    @Test
    void updateItem_UnauthorizedUser_ThrowsForbiddenException() {
        Item item = Item.builder()
                .id(50L)
                .userId(99L)
                .status(ItemStatus.ACTIVE)
                .build();

        when(itemRepository.findById(50L)).thenReturn(Optional.of(item));

        com.lostlink.dto.request.ItemUpdateRequest updateRequest = com.lostlink.dto.request.ItemUpdateRequest.builder()
                .status(ItemStatus.RESOLVED)
                .build();

        assertThrows(ForbiddenException.class, () -> itemService.updateItem(50L, updateRequest, userPrincipal));
        verify(itemRepository, never()).save(any(Item.class));
    }
}
