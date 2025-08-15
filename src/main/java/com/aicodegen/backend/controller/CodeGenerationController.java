package com.aicodegen.backend.controller;

import com.aicodegen.backend.dto.CodeGenerationRequest;
import com.aicodegen.backend.dto.CodeGenerationResponse;
import com.aicodegen.backend.entity.CodeHistory;
import com.aicodegen.backend.service.CodeGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/codegen")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationController {
    
    private final CodeGenerationService codeGenerationService;
    
    @PostMapping
    public ResponseEntity<CodeGenerationResponse> generateCode(@Valid @RequestBody CodeGenerationRequest request) {
        log.info("Received code generation request for language: {}", request.getLanguage());
        
        try {
            CodeGenerationResponse response = codeGenerationService.generateCode(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Unexpected error in generateCode endpoint", e);
            CodeGenerationResponse errorResponse = CodeGenerationResponse.error(
                request.getPrompt(), 
                request.getLanguage(), 
                "Internal server error: " + e.getMessage(), 
                0L
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<CodeHistory>> getAllHistory() {
        log.info("Fetching all code generation history");
        
        try {
            List<CodeHistory> history = codeGenerationService.getAllHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history/recent")
    public ResponseEntity<List<CodeHistory>> getRecentHistory(@RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching recent code generation history with limit: {}", limit);
        
        try {
            List<CodeHistory> history = codeGenerationService.getRecentHistory(limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching recent history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history/language/{language}")
    public ResponseEntity<List<CodeHistory>> getHistoryByLanguage(@PathVariable String language) {
        log.info("Fetching code generation history for language: {}", language);
        
        try {
            List<CodeHistory> history = codeGenerationService.getHistoryByLanguage(language);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching history by language", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history/search")
    public ResponseEntity<List<CodeHistory>> searchHistory(@RequestParam String keyword) {
        log.info("Searching code generation history with keyword: {}", keyword);
        
        try {
            List<CodeHistory> history = codeGenerationService.searchHistory(keyword);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error searching history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("Fetching code generation statistics");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalGenerations", codeGenerationService.getTotalGenerations());
            stats.put("successfulGenerations", codeGenerationService.getSuccessfulGenerations());
            
            Long total = codeGenerationService.getTotalGenerations();
            Long successful = codeGenerationService.getSuccessfulGenerations();
            
            if (total > 0) {
                double successRate = (successful.doubleValue() / total.doubleValue()) * 100;
                stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            } else {
                stats.put("successRate", 0.0);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AI Code Generator");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        List<String> languages = List.of(
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", 
            "Rust", "Kotlin", "Swift", "PHP", "Ruby", "Scala", "R", "SQL"
        );
        
        return ResponseEntity.ok(languages);
    }
}