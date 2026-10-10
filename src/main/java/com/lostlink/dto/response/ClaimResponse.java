package com.lostlink.dto.response;

import com.lostlink.entity.ClaimStatus;
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
public class ClaimResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private ItemType itemType;
    private Long claimantId;
    private String claimantName;
    private String claimantEmail;
    private String answer;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
