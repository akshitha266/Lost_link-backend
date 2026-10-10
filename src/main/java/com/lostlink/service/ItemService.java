package com.lostlink.service;

import com.lostlink.dto.request.ItemRequest;
import com.lostlink.dto.response.ItemResponse;
import com.lostlink.entity.Item;
import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import com.lostlink.entity.Role;
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
    public ItemResponse updateItem(Long id, ItemRequest request, UserPrincipal currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!item.getUserId().equals(currentUser.getId()) && !isAdmin) {
            throw new ForbiddenException("You are not authorized to update this listing.");
        }

        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription().trim());
        item.setCategory(request.getCategory().trim());
        item.setDate(request.getDate());
        item.setLocation(request.getLocation().trim());
        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl());
        }
        if (request.getVerificationQuestion() != null) {
            item.setVerificationQuestion(request.getVerificationQuestion().trim());
        }
        if (request.getVerificationAnswer() != null) {
            item.setVerificationAnswer(request.getVerificationAnswer().trim());
        }

        Item updated = itemRepository.save(item);
        return mapToResponse(updated);
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
