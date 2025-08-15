package com.aicodegen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for code generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenerationResponse {
    
    private String generatedCode;
    private String prompt;
    private String language;
    private LocalDateTime timestamp;
    private Long executionTimeMs;
    private boolean success;
    private String errorMessage;
    
    public static CodeGenerationResponse success(String code, String prompt, String language, Long executionTime) {
        return new CodeGenerationResponse(code, prompt, language, LocalDateTime.now(), executionTime, true, null);
    }
    
    public static CodeGenerationResponse error(String prompt, String language, String errorMessage, Long executionTime) {
        return new CodeGenerationResponse(null, prompt, language, LocalDateTime.now(), executionTime, false, errorMessage);
    }
}