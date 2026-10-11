package com.lostlink.service;

import com.lostlink.dto.request.ItemRequest;
import com.lostlink.dto.response.ItemResponse;
import com.lostlink.entity.Item;
import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import com.lostlink.exception.ForbiddenException;
import com.lostlink.exception.ResourceNotFoundException;
import com.lostlink.repository.ItemRepository;
import com.lostlink.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public ItemResponse createItem(ItemRequest request, UserPrincipal currentUser) {
        Item item = Item.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory().trim())
                .type(request.getType())
                .date(request.getDate())
                .location(request.getLocation().trim())
                .imageUrl(request.getImageUrl())
                .status(ItemStatus.ACTIVE)
                .verificationQuestion(request.getVerificationQuestion().trim())
                .verificationAnswer(request.getVerificationAnswer().trim())
                .userId(currentUser.getId())
                .postedByName(currentUser.getName())
                .build();

        Item saved = itemRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .filter(i -> i.getStatus() != ItemStatus.REMOVED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        if (item.getStatus() == ItemStatus.REMOVED) {
            throw new ResourceNotFoundException("This listing is no longer available.");
        }

        return mapToResponse(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> searchItems(ItemType type, String category, ItemStatus status, String location, String keyword) {
        // If status not specified, default to not showing REMOVED
        List<Item> results = itemRepository.searchItems(type, category, status, location, keyword);
        return results.stream()
                .filter(i -> i.getStatus() != ItemStatus.REMOVED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemResponse updateItem(Long id, com.lostlink.dto.request.ItemUpdateRequest request, UserPrincipal currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!item.getUserId().equals(currentUser.getId()) && !isAdmin) {
            throw new ForbiddenException("You are not authorized to update this listing.");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            item.setDescription(request.getDescription().trim());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            item.setCategory(request.getCategory().trim());
        }
        if (request.getType() != null) {
            item.setType(request.getType());
        }
        if (request.getDate() != null && !request.getDate().isBlank()) {
            item.setDate(request.getDate());
        }
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            item.setLocation(request.getLocation().trim());
        }
        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getVerificationQuestion() != null && !request.getVerificationQuestion().isBlank()) {
            item.setVerificationQuestion(request.getVerificationQuestion().trim());
        }
        if (request.getVerificationAnswer() != null && !request.getVerificationAnswer().isBlank()) {
            item.setVerificationAnswer(request.getVerificationAnswer().trim());
        }

        Item updated = itemRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request, UserPrincipal currentUser) {
        com.lostlink.dto.request.ItemUpdateRequest updateRequest = com.lostlink.dto.request.ItemUpdateRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .type(request.getType())
                .date(request.getDate())
                .location(request.getLocation())
                .imageUrl(request.getImageUrl())
                .verificationQuestion(request.getVerificationQuestion())
                .verificationAnswer(request.getVerificationAnswer())
                .build();
        return updateItem(id, updateRequest, currentUser);
    }

    @Transactional
    public void deleteItem(Long id, UserPrincipal currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!item.getUserId().equals(currentUser.getId()) && !isAdmin) {
            throw new ForbiddenException("You are not authorized to delete this listing.");
        }

        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByUserId(Long userId) {
        return itemRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .category(item.getCategory())
                .type(item.getType())
                .date(item.getDate())
                .location(item.getLocation())
                .imageUrl(item.getImageUrl())
                .status(item.getStatus())
                .verificationQuestion(item.getVerificationQuestion())
                // verificationAnswer is deliberately NOT mapped to prevent disclosure
                .userId(item.getUserId())
                .postedByName(item.getPostedByName())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
