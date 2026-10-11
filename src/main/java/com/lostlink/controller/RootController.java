package com.lostlink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Root", description = "API health, information, and quick links")
public class RootController {

    @GetMapping(value = {"/", "/api", "/api/"})
    @Operation(summary = "LostLink service health and navigation metadata")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", "LostLink - Campus Lost & Found System API");
        info.put("status", "ONLINE");
        info.put("swaggerDocs", "http://localhost:8088/swagger-ui.html");
        info.put("frontendApp", "http://localhost:3000");
        info.put("h2DatabaseConsole", "http://localhost:8088/h2-console");
        info.put("endpoints", Map.of(
                "items", "/api/items",
                "matches", "/api/matches",
                "authLogin", "/api/auth/login",
                "authRegister", "/api/auth/register"
        ));
        info.put("message", "Backend is running and accepting requests.");
        return ResponseEntity.ok(info);
    }
}
