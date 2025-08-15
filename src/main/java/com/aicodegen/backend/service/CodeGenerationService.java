package com.aicodegen.backend.service;

import com.aicodegen.backend.dto.CodeGenerationRequest;
import com.aicodegen.backend.dto.CodeGenerationResponse;
import com.aicodegen.backend.entity.CodeHistory;
import com.aicodegen.backend.repository.CodeHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationService {
    
    private final GeminiService geminiService;
    private final CodeHistoryRepository codeHistoryRepository;
    
    @Transactional
    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        long startTime = System.currentTimeMillis();
        
        // Validate request
        if (!isValidRequest(request)) {
            String errorMsg = "Invalid request: prompt and language are required";
            log.warn(errorMsg);
            return CodeGenerationResponse.error(request.getPrompt(), request.getLanguage(), errorMsg, 0L);
        }
        
        try {
            // Check if Gemini service is properly configured
            if (!geminiService.isConfigured()) {
                String errorMsg = "Gemini API key not configured. Please set GEMINI_API_KEY environment variable.";
                log.error(errorMsg);
                saveHistory(request, null, false, errorMsg, System.currentTimeMillis() - startTime);
                return CodeGenerationResponse.error(request.getPrompt(), request.getLanguage(), errorMsg, System.currentTimeMillis() - startTime);
            }
            
            log.info("Generating code for prompt: '{}' in language: '{}'", 
                     sanitizePromptForLogging(request.getPrompt()), 
                     request.getLanguage());
            
            // Generate code using Gemini AI
            String generatedCode = geminiService.generateCode(request.getPrompt(), request.getLanguage());
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Validate generated code
            if (!StringUtils.hasText(generatedCode)) {
                String errorMsg = "Generated code is empty or invalid";
                log.warn(errorMsg);
                saveHistory(request, null, false, errorMsg, executionTime);
                return CodeGenerationResponse.error(request.getPrompt(), request.getLanguage(), errorMsg, executionTime);
            }
            
            // Save successful generation to database
            saveHistory(request, generatedCode, true, null, executionTime);
            
            log.info("Code generation successful in {}ms for language: {}", executionTime, request.getLanguage());
            return CodeGenerationResponse.success(generatedCode, request.getPrompt(), request.getLanguage(), executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Code generation failed: " + e.getMessage();
            
            log.error("Code generation failed for prompt: '{}', language: '{}', error: {}", 
                      sanitizePromptForLogging(request.getPrompt()), request.getLanguage(), e.getMessage(), e);
            
            // Save failed generation to database
            saveHistory(request, null, false, errorMessage, executionTime);
            
            return CodeGenerationResponse.error(request.getPrompt(), request.getLanguage(), errorMessage, executionTime);
        }
    }
    
    private boolean isValidRequest(CodeGenerationRequest request) {
        return request != null 
            && StringUtils.hasText(request.getPrompt()) 
            && StringUtils.hasText(request.getLanguage())
            && request.getPrompt().length() <= 1000
            && request.getLanguage().length() <= 50;
    }
    
    private String sanitizePromptForLogging(String prompt) {
        if (prompt == null) return "null";
        return prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt;
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
    
    @Cacheable(value = "codeHistory", key = "'all'")
    public List<CodeHistory> getAllHistory() {
        try {
            return codeHistoryRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch code generation history", e);
            return List.of();
        }
    }
    
    public List<CodeHistory> getRecentHistory(int limit) {
        try {
            if (limit <= 0) limit = 10;
            if (limit > 100) limit = 100; // Prevent excessive data retrieval
            
            Pageable pageable = PageRequest.of(0, limit);
            return codeHistoryRepository.findRecentHistory(pageable);
        } catch (Exception e) {
            log.error("Failed to fetch recent code generation history", e);
            return List.of();
        }
    }
    
    @Cacheable(value = "historyByLanguage", key = "#language")
    public List<CodeHistory> getHistoryByLanguage(String language) {
        try {
            if (!StringUtils.hasText(language)) {
                return List.of();
            }
            return codeHistoryRepository.findByProgrammingLanguageOrderByCreatedAtDesc(language);
        } catch (Exception e) {
            log.error("Failed to fetch history by language: {}", language, e);
            return List.of();
        }
    }
    
    public List<CodeHistory> searchHistory(String keyword) {
        try {
            if (!StringUtils.hasText(keyword)) {
                return List.of();
            }
            return codeHistoryRepository.findByPromptContaining(keyword.trim());
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
    
    @Cacheable(value = "statistics", key = "'total'")
    public Long getTotalGenerations() {
        try {
            return codeHistoryRepository.count();
        } catch (Exception e) {
            log.error("Failed to count total generations", e);
            return 0L;
        }
    }
    
    @Cacheable(value = "statistics", key = "'successful'")
    public Long getSuccessfulGenerations() {
        try {
            return codeHistoryRepository.countSuccessfulGenerations();
        } catch (Exception e) {
            log.error("Failed to count successful generations", e);
            return 0L;
        }
    }
    
    public Long getFailedGenerations() {
        try {
            return codeHistoryRepository.countFailedGenerations();
        } catch (Exception e) {
            log.error("Failed to count failed generations", e);
            return 0L;
        }
    }
    
    public Double getAverageExecutionTime() {
        try {
            return codeHistoryRepository.getAverageExecutionTime();
        } catch (Exception e) {
            log.error("Failed to get average execution time", e);
            return 0.0;
        }
    }
    
    public Map<String, Long> getLanguageUsageStatistics() {
        try {
            List<Object[]> stats = codeHistoryRepository.getLanguageStatistics();
            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> (String) stat[0],
                            stat -> (Long) stat[1]
                    ));
        } catch (Exception e) {
            log.error("Failed to get language usage statistics", e);
            return Map.of();
        }
    }
}