package com.aicodegen.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// Request DTO for code generation
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenerationRequest {
    
    @NotBlank(message = "Prompt cannot be empty")
    @Size(max = 1000, message = "Prompt cannot exceed 1000 characters")
    private String prompt;
    
    @NotBlank(message = "Programming language must be specified")
    @Size(max = 50, message = "Language name cannot exceed 50 characters")
    private String language;
}

// Response DTO for code generation
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

// DTO for Gemini API request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    
    private Contents[] contents;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contents {
        private Parts[] parts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parts {
        private String text;
    }
    
    public static GeminiRequest create(String prompt) {
        Parts part = new Parts(prompt);
        Contents content = new Contents(new Parts[]{part});
        return new GeminiRequest(new Contents[]{content});
    }
}

// DTO for Gemini API response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    
    private Candidates[] candidates;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidates {
        private Content content;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private Parts[] parts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parts {
        private String text;
    }
    
    public String getGeneratedText() {
        if (candidates != null && candidates.length > 0 
            && candidates[0].content != null 
            && candidates[0].content.parts != null 
            && candidates[0].content.parts.length > 0) {
            return candidates[0].content.parts[0].text;
        }
        return null;
    }
}