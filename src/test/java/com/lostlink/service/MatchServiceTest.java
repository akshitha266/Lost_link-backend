package com.lostlink.service;

import com.lostlink.entity.Item;
import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchServiceTest {

    private MatchService matchService;

    @BeforeEach
    void setUp() {
        matchService = new MatchService(null, null);
    }

    @Test
    void calculateSimilarity_IdenticalAttributes_HighSimilarity() {
        Item lost = Item.builder()
                .title("MacBook Pro 14 inch")
                .description("Space gray laptop with stickers")
                .category("Electronics")
                .location("Central Library Room 204")
                .type(ItemType.LOST)
                .status(ItemStatus.ACTIVE)
                .build();

        Item found = Item.builder()
                .title("MacBook Pro 14 inch")
                .description("Space gray laptop with stickers")
                .category("Electronics")
                .location("Central Library Room 204")
                .type(ItemType.FOUND)
                .status(ItemStatus.ACTIVE)
                .build();

        int score = matchService.calculateSimilarity(lost, found);

        // Category (30) + Title (25) + Description (25) + Location (20) = 100
        assertTrue(score >= 80, "Expected high score for identical attributes but was " + score);
    }

    @Test
    void calculateSimilarity_CompletelyDifferentAttributes_LowSimilarity() {
        Item lost = Item.builder()
                .title("Golden necklace chain")
                .description("Fine jewelry gift from grandmother")
                .category("Accessories")
                .location("Dormitory laundry room")
                .type(ItemType.LOST)
                .status(ItemStatus.ACTIVE)
                .build();

        Item found = Item.builder()
                .title("Toyota Car Key Fob")
                .description("Electronic black remote clicker")
                .category("Keys")
                .location("South Stadium parking lot")
                .type(ItemType.FOUND)
                .status(ItemStatus.ACTIVE)
                .build();

        int score = matchService.calculateSimilarity(lost, found);

        assertEquals(0, score, "Expected 0 score for completely different items");
    }
}
