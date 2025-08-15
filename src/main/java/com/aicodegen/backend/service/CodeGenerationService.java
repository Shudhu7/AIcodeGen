package com.aicodegen.backend.service;

import com.aicodegen.backend.dto.CodeGenerationRequest;
import com.aicodegen.backend.dto.CodeGenerationResponse;
import com.aicodegen.backend.entity.CodeHistory;
import com.aicodegen.backend.repository.CodeHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationService {
    
    private final GeminiService geminiService;
    private final CodeHistoryRepository codeHistoryRepository;
    
    @Transactional
    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if Gemini service is properly configured
            if (!geminiService.isConfigured()) {
                String errorMsg = "Gemini API key not configured";
                log.error(errorMsg);
                saveHistory(request, null, false, errorMsg, System.currentTimeMillis() - startTime);
                return CodeGenerationResponse.error(request.getPrompt(), request.getLanguage(), errorMsg, System.currentTimeMillis() - startTime);
            }
            
            log.info("Generating code for prompt: '{}' in language: '{}'", 
                     request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())), 
                     request.getLanguage());
            
            // Generate code using Gemini AI
            String generatedCode = geminiService.generateCode(request.getPrompt(), request.getLanguage());
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Save successful generation to database
            saveHistory(request, generatedCode, true, null, executionTime);
            
            log.info("Code generation successful in {}ms", executionTime);
            return CodeGenerationResponse.success(generatedCode, request.getPrompt(), request.getLanguage(), executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Code generation failed: " + e.getMessage();
            
            log.error("Code generation failed for prompt: '{}', language: '{}', error: {}", 
                      request.getPrompt(), request.getLanguage(), e.getMessage());
            
            // Save failed generation to database
            saveHistory(request, null, false, errorMessage, executionTime);
            
            return CodeGenerationResponse.error(request.getPrompt(), request.getLanguage(), errorMessage, executionTime);
        }
    }
    
    private void saveHistory(CodeGenerationRequest request, String generatedCode, boolean success, String errorMessage, long executionTime) {
        try {
            CodeHistory history = new CodeHistory();
            history.setUserPrompt(request.getPrompt());
            history.setProgrammingLanguage(request.getLanguage());
            history.setGeneratedCode(generatedCode);
            history.setSuccess(success);
            history.setErrorMessage(errorMessage);
            history.setExecutionTimeMs(executionTime);
            
            codeHistoryRepository.save(history);
            log.debug("Saved code generation history with ID: {}", history.getId());
            
        } catch (Exception e) {
            log.error("Failed to save code generation history", e);
            // Don't throw exception here to avoid affecting the main response
        }
    }
    
    public List<CodeHistory> getAllHistory() {
        try {
            return codeHistoryRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch code generation history", e);
            return List.of(); // Return empty list instead of throwing exception
        }
    }
    
    public List<CodeHistory> getRecentHistory(int limit) {
        try {
            if (limit <= 0) limit = 10;
            return codeHistoryRepository.findTop10ByOrderByCreatedAtDesc();
        } catch (Exception e) {
            log.error("Failed to fetch recent code generation history", e);
            return List.of();
        }
    }
    
    public List<CodeHistory> getHistoryByLanguage(String language) {
        try {
            return codeHistoryRepository.findByProgrammingLanguageOrderByCreatedAtDesc(language);
        } catch (Exception e) {
            log.error("Failed to fetch history by language: {}", language, e);
            return List.of();
        }
    }
    
    public List<CodeHistory> searchHistory(String keyword) {
        try {
            return codeHistoryRepository.findByPromptContaining(keyword);
        } catch (Exception e) {
            log.error("Failed to search history with keyword: {}", keyword, e);
            return List.of();
        }
    }
    
    public List<CodeHistory> getRecentHistory() {
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            return codeHistoryRepository.findRecentHistory(yesterday);
        } catch (Exception e) {
            log.error("Failed to fetch recent history", e);
            return List.of();
        }
    }
    
    public Long getTotalGenerations() {
        try {
            return codeHistoryRepository.count();
        } catch (Exception e) {
            log.error("Failed to count total generations", e);
            return 0L;
        }
    }
    
    public Long getSuccessfulGenerations() {
        try {
            return codeHistoryRepository.countSuccessfulGenerations();
        } catch (Exception e) {
            log.error("Failed to count successful generations", e);
            return 0L;
        }
    }
}