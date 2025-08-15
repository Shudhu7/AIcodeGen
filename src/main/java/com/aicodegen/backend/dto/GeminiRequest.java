package com.aicodegen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Gemini API request
 */
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