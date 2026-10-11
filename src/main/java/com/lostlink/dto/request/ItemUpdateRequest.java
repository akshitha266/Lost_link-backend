package com.lostlink.dto.request;

import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemUpdateRequest {
    private String title;
    private String description;
    private String category;
    private ItemType type;
    private String date;
    private String location;
    private String imageUrl;
    private ItemStatus status;
    private String verificationQuestion;
    private String verificationAnswer;
}
