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
            
            // Prepare request with updated structure
            GeminiRequest request = GeminiRequest.create(enhancedPrompt);
            HttpHeaders headers = createHeaders();
            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("Sending request to Gemini API: {}", enhancedPrompt);
            log.debug("Using API URL: {}", apiUrl);
            
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
                if (generatedText != null && !generatedText.trim().isEmpty()) {
                    return cleanGeneratedCode(generatedText);
                } else {
                    throw new RuntimeException("Empty response from Gemini API");
                }
            } else {
                throw new RuntimeException("Failed to get response from Gemini API. Status: " + response.getStatusCode());
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
            "Only return the code without explanations or markdown formatting. " +
            "Do not include ```%s``` code blocks in your response.\n\n" +
            "Requirement: %s\n\n" +
            "Programming Language: %s\n\n" +
            "Please provide only the code without any explanatory text before or after.",
            language, language.toLowerCase(), userPrompt, language
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
        
        // Remove code block markers
        if (cleaned.startsWith("```")) {
            int firstNewLine = cleaned.indexOf('\n');
            if (firstNewLine > 0) {
                cleaned = cleaned.substring(firstNewLine + 1);
            }
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.lastIndexOf("```"));
        }
        
        // Remove common prefixes
        cleaned = cleaned.replaceAll("^(java|python|javascript|typescript|cpp|csharp)\\s*\\n", "");
        
        // Remove any leading/trailing explanatory text
        String[] lines = cleaned.split("\n");
        StringBuilder codeBuilder = new StringBuilder();
        boolean codeStarted = false;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip explanatory lines before code
            if (!codeStarted && (trimmedLine.isEmpty() || 
                trimmedLine.startsWith("Here") || 
                trimmedLine.startsWith("This") || 
                trimmedLine.startsWith("The following"))) {
                continue;
            }
            
            codeStarted = true;
            codeBuilder.append(line).append("\n");
        }
        
        return codeBuilder.toString().trim();
    }
    
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && 
               !apiKey.equals("your_gemini_api_key_here") &&
               !apiKey.equals("AIzaSyBrRhXjfPTkj6E0W9YV2hbbwJf4Cxx-Py8"); // This seems to be a placeholder
    }
}