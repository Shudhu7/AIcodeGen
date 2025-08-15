package com.aicodegen.backend.service;

import com.aicodegen.backend.dto.GeminiRequest;
import com.aicodegen.backend.dto.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    
    public GeminiService() {
        this.restTemplate = new RestTemplate();
    }
    
    public String generateCode(String prompt, String language) {
        try {
            // Enhance the prompt for better code generation
            String enhancedPrompt = buildEnhancedPrompt(prompt, language);
            
            // Prepare request
            GeminiRequest request = GeminiRequest.create(enhancedPrompt);
            HttpHeaders headers = createHeaders();
            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("Sending request to Gemini API: {}", enhancedPrompt);
            
            // Make API call
            String urlWithKey = apiUrl + "?key=" + apiKey;
            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                urlWithKey, 
                HttpMethod.POST, 
                entity, 
                GeminiResponse.class
            );
            
            // Extract and clean the generated code
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String generatedText = response.getBody().getGeneratedText();
                return cleanGeneratedCode(generatedText);
            } else {
                throw new RuntimeException("Failed to get response from Gemini API");
            }
            
        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to generate code: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during code generation", e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }
    
    private String buildEnhancedPrompt(String userPrompt, String language) {
        return String.format(
            "Generate clean, production-ready %s code for the following requirement. " +
            "Include proper error handling, comments, and follow best practices. " +
            "Only return the code without explanations or markdown formatting.\n\n" +
            "Requirement: %s\n\n" +
            "Programming Language: %s",
            language, userPrompt, language
        );
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", "AI-Code-Generator/1.0");
        return headers;
    }
    
    private String cleanGeneratedCode(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            return "// No code generated";
        }
        
        // Remove markdown code blocks if present
        String cleaned = rawCode.trim();
        if (cleaned.startsWith("```")) {
            int firstNewLine = cleaned.indexOf('\n');
            if (firstNewLine > 0) {
                cleaned = cleaned.substring(firstNewLine + 1);
            }
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.lastIndexOf("```"));
        }
        
        return cleaned.trim();
    }
    
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_gemini_api_key_here");
    }
}