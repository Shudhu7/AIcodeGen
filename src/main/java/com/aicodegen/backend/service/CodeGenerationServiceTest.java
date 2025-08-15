package com.aicodegen.backend.service;

import com.aicodegen.backend.repository.CodeHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        // Setup common test data
    }

    @Test
    void testGenerateCodeSuccess() {
        // Given
        when(geminiService.isConfigured()).thenReturn(true);
        when(geminiService.generateCode(anyString(), anyString()))
                .thenReturn("public class Test { }");

        // When
        var request = new com.aicodegen.backend.dto.CodeGenerationRequest(
                "Create a simple class", "Java");
        var response = codeGenerationService.generateCode(request);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getGeneratedCode());
        verify(codeHistoryRepository).save(any());
    }

    @Test
    void testGenerateCodeWhenNotConfigured() {
        // Given
        when(geminiService.isConfigured()).thenReturn(false);

        // When
        var request = new com.aicodegen.backend.dto.CodeGenerationRequest(
                "Create a simple class", "Java");
        var response = codeGenerationService.generateCode(request);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Gemini API key not configured", response.getErrorMessage());
    }
}