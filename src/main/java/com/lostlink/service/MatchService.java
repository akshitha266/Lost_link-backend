package com.lostlink.service;

import com.lostlink.dto.response.MatchResponse;
import com.lostlink.entity.Item;
import com.lostlink.entity.ItemStatus;
import com.lostlink.entity.ItemType;
import com.lostlink.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final ItemRepository itemRepository;
    private final ItemService itemService;

    /**
     * Compute matches between active LOST and FOUND items.
     * Weights:
     * - Category match: 30%
     * - Title similarity: 25%
     * - Description similarity: 25%
     * - Location similarity: 20%
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> findMatches() {
        List<Item> lostItems = itemRepository.findByTypeAndStatus(ItemType.LOST, ItemStatus.ACTIVE);
        List<Item> foundItems = itemRepository.findByTypeAndStatus(ItemType.FOUND, ItemStatus.ACTIVE);

        List<MatchResponse> matches = new ArrayList<>();

        for (Item lost : lostItems) {
            for (Item found : foundItems) {
                int score = calculateSimilarity(lost, found);

                // Threshold for suggesting a match candidate
                if (score >= 30) {
                    matches.add(MatchResponse.builder()
                            .lostItem(itemService.mapToResponse(lost))
                            .foundItem(itemService.mapToResponse(found))
                            .similarityScore(score)
                            .build());
                }
            }
        }

        // Sort by highest similarity score first
        matches.sort((a, b) -> Integer.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        return matches;
    }

    public int calculateSimilarity(Item lost, Item found) {
        int totalScore = 0;

        // 1. Category Match (30%)
        if (lost.getCategory() != null && found.getCategory() != null &&
                lost.getCategory().trim().equalsIgnoreCase(found.getCategory().trim())) {
            totalScore += 30;
        }

        // 2. Title Similarity (up to 25%)
        double titleOverlap = computeTokenJaccard(lost.getTitle(), found.getTitle());
        totalScore += (int) Math.round(titleOverlap * 25.0);

        // 3. Description Similarity (up to 25%)
        double descOverlap = computeTokenJaccard(lost.getDescription(), found.getDescription());
        totalScore += (int) Math.round(descOverlap * 25.0);

        // 4. Location Similarity (up to 20%)
        double locOverlap = computeTokenJaccard(lost.getLocation(), found.getLocation());
        totalScore += (int) Math.round(locOverlap * 20.0);

        return Math.min(100, Math.max(0, totalScore));
    }

    /**
     * Helper to compute token overlap / Jaccard similarity between two strings
     */
    private double computeTokenJaccard(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;

        Set<String> set1 = tokenize(s1);
        Set<String> set2 = tokenize(s2);

        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / (double) union.size();
    }

    private Set<String> tokenize(String input) {
        if (input == null) return Collections.emptySet();
        String cleaned = input.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
        String[] words = cleaned.split("\\s+");
        Set<String> tokens = new HashSet<>();
        // Filter out tiny common words
        for (String w : words) {
            if (w.length() > 2) {
                tokens.add(w);
            }
        }
        return tokens;
    }
}
