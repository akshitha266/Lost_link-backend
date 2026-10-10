package com.lostlink.dto.response;

import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private ItemType type;
    private String date;
    private String location;
    private String imageUrl;
    private ItemStatus status;
    private String verificationQuestion; // Public challenge question
    // Note: verificationAnswer is intentionally excluded for security
    private Long userId;
    private String postedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
