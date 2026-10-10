package com.lostlink.controller;

import com.lostlink.dto.response.MatchResponse;
import com.lostlink.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Smart text-based similarity matches between lost and found items")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @Operation(summary = "Get suggested matches between active lost and found items")
    public ResponseEntity<List<MatchResponse>> getMatches() {
        return ResponseEntity.ok(matchService.findMatches());
    }
}
