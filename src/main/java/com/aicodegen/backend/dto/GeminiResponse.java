package com.aicodegen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Gemini API response
 */
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