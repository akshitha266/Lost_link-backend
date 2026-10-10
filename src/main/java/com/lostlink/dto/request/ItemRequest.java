package com.lostlink.dto.request;

import com.lostlink.entity.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @NotBlank(message = "Item title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Item type is required (LOST or FOUND)")
    private ItemType type;

    @NotBlank(message = "Date is required")
    private String date;

    @NotBlank(message = "Location is required")
    private String location;

    private String imageUrl;

    @NotBlank(message = "Verification question is required")
    private String verificationQuestion;

    @NotBlank(message = "Verification answer is required")
    private String verificationAnswer;
}
