package com.victorqueiroga.serverwatch.controller.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de teste para verificar se as APIs estão funcionando
 */
@RestController
@RequestMapping("/api/test")
public class TestApiController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API está funcionando!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/monitoring")
    public ResponseEntity<Map<String, Object>> testMonitoring() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API de monitoramento está acessível");
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoints", new String[]{
            "/api/monitoring/servers",
            "/api/monitoring/summary", 
            "/api/monitoring/servers/{id}/refresh"
        });
        return ResponseEntity.ok(response);
    }
}