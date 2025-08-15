package com.aicodegen.backend.service;

import com.aicodegen.backend.dto.CodeGenerationRequest;
import com.aicodegen.backend.dto.CodeGenerationResponse;
import com.aicodegen.backend.repository.CodeHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeGenerationServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private CodeHistoryRepository codeHistoryRepository;

    @InjectMocks
    private CodeGenerationService codeGenerationService;

    @BeforeEach
    void setUp() {
        // Setup common test data if needed
    }

    @Test
    void testGenerateCodeSuccess() {
        // Given
        when(geminiService.isConfigured()).thenReturn(true);
        when(geminiService.generateCode(anyString(), anyString()))
                .thenReturn("public class Test { }");

        CodeGenerationRequest request = new CodeGenerationRequest(
                "Create a simple class", "Java");

        // When
        CodeGenerationResponse response = codeGenerationService.generateCode(request);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getGeneratedCode());
        assertEquals("public class Test { }", response.getGeneratedCode());
        assertEquals("Create a simple class", response.getPrompt());
        assertEquals("Java", response.getLanguage());
        verify(codeHistoryRepository).save(any());
        verify(geminiService).generateCode("Create a simple class", "Java");
    }

    @Test
    void testGenerateCodeWhenNotConfigured() {
        // Given
        when(geminiService.isConfigured()).thenReturn(false);

        CodeGenerationRequest request = new CodeGenerationRequest(
                "Create a simple class", "Java");

        // When
        CodeGenerationResponse response = codeGenerationService.generateCode(request);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Gemini API key not configured", response.getErrorMessage());
        assertNull(response.getGeneratedCode());
        verify(codeHistoryRepository).save(any()); // Should still save failed attempt
        verify(geminiService, never()).generateCode(anyString(), anyString());
    }

    @Test
    void testGenerateCodeWhenGeminiServiceThrowsException() {
        // Given
        when(geminiService.isConfigured()).thenReturn(true);
        when(geminiService.generateCode(anyString(), anyString()))
                .thenThrow(new RuntimeException("API error"));

        CodeGenerationRequest request = new CodeGenerationRequest(
                "Create a simple class", "Java");

        // When
        CodeGenerationResponse response = codeGenerationService.generateCode(request);

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Code generation failed"));
        assertTrue(response.getErrorMessage().contains("API error"));
        assertNull(response.getGeneratedCode());
        verify(codeHistoryRepository).save(any());
    }

    @Test
    void testGetTotalGenerations() {
        // Given
        when(codeHistoryRepository.count()).thenReturn(10L);

        // When
        Long total = codeGenerationService.getTotalGenerations();

        // Then
        assertEquals(10L, total);
        verify(codeHistoryRepository).count();
    }

    @Test
    void testGetSuccessfulGenerations() {
        // Given
        when(codeHistoryRepository.countSuccessfulGenerations()).thenReturn(8L);

        // When
        Long successful = codeGenerationService.getSuccessfulGenerations();

        // Then
        assertEquals(8L, successful);
        verify(codeHistoryRepository).countSuccessfulGenerations();
    }
}