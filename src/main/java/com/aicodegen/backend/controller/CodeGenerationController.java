package com.aicodegen.backend.controller;

import com.aicodegen.backend.dto.CodeGenerationRequest;
import com.aicodegen.backend.dto.CodeGenerationResponse;
import com.aicodegen.backend.entity.CodeHistory;
import com.aicodegen.backend.service.CodeGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<List<CodeHistory>> getRecentHistory(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
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
        if (!StringUtils.hasText(language)) {
            return ResponseEntity.badRequest().build();
        }
        
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
    public ResponseEntity<List<CodeHistory>> searchHistory(
            @RequestParam String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ResponseEntity.badRequest().build();
        }
        
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
            
            Long total = codeGenerationService.getTotalGenerations();
            Long successful = codeGenerationService.getSuccessfulGenerations();
            Long failed = codeGenerationService.getFailedGenerations();
            Double avgExecutionTime = codeGenerationService.getAverageExecutionTime();
            
            stats.put("totalGenerations", total);
            stats.put("successfulGenerations", successful);
            stats.put("failedGenerations", failed);
            stats.put("averageExecutionTimeMs", avgExecutionTime != null ? Math.round(avgExecutionTime * 100.0) / 100.0 : 0.0);
            
            if (total > 0) {
                double successRate = (successful.doubleValue() / total.doubleValue()) * 100;
                stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            } else {
                stats.put("successRate", 0.0);
            }
            
            // Add language usage statistics
            Map<String, Long> languageStats = codeGenerationService.getLanguageUsageStatistics();
            stats.put("languageUsage", languageStats);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        log.info("Fetching detailed code generation statistics");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Basic stats
            stats.put("totalGenerations", codeGenerationService.getTotalGenerations());
            stats.put("successfulGenerations", codeGenerationService.getSuccessfulGenerations());
            stats.put("failedGenerations", codeGenerationService.getFailedGenerations());
            stats.put("averageExecutionTime", codeGenerationService.getAverageExecutionTime());
            
            // Language usage
            stats.put("languageUsage", codeGenerationService.getLanguageUsageStatistics());
            
            // Recent activity
            stats.put("recentHistory", codeGenerationService.getRecentHistory());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching detailed statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AI Code Generator");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("version", "1.0.0");
        
        try {
            // Check database connectivity
            Long totalCount = codeGenerationService.getTotalGenerations();
            health.put("database", "Connected");
            health.put("totalGenerations", totalCount);
        } catch (Exception e) {
            health.put("database", "Error: " + e.getMessage());
            health.put("status", "DEGRADED");
        }
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        Map<String, Object> response = new HashMap<>();
        
        List<String> languages = List.of(
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", 
            "Rust", "Kotlin", "Swift", "PHP", "Ruby", "Scala", "R", "SQL",
            "HTML", "CSS", "React", "Angular", "Vue", "Node.js", "Spring Boot"
        );
        
        response.put("supportedLanguages", languages);
        response.put("count", languages.size());
        
        // Add usage statistics for each language
        try {
            Map<String, Long> usageStats = codeGenerationService.getLanguageUsageStatistics();
            response.put("usageStatistics", usageStats);
        } catch (Exception e) {
            log.warn("Could not fetch language usage statistics", e);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> version = new HashMap<>();
        version.put("application", "AI Code Generator");
        version.put("version", "1.0.0");
        version.put("buildTime", LocalDateTime.now().toString());
        version.put("apiVersion", "v1");
        
        return ResponseEntity.ok(version);
    }
}