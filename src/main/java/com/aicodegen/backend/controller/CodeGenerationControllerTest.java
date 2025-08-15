package com.aicodegen.backend.controller;

import com.aicodegen.backend.dto.CodeGenerationRequest;
import com.aicodegen.backend.dto.CodeGenerationResponse;
import com.aicodegen.backend.service.CodeGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CodeGenerationController.class)
class CodeGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CodeGenerationService codeGenerationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/codegen/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("AI Code Generator"));
    }

    @Test
    void testGetSupportedLanguages() throws Exception {
        mockMvc.perform(get("/api/codegen/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Java"));
    }

    @Test
    void testCodeGenerationValidation() throws Exception {
        CodeGenerationRequest invalidRequest = new CodeGenerationRequest("", "Java");

        mockMvc.perform(post("/api/codegen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.prompt").exists());
    }

    @Test
    void testSuccessfulCodeGeneration() throws Exception {
        // Given
        CodeGenerationRequest request = new CodeGenerationRequest("Create a simple Java class", "Java");
        CodeGenerationResponse mockResponse = CodeGenerationResponse.success(
                "public class Test { }", 
                "Create a simple Java class", 
                "Java", 
                100L
        );
        
        when(codeGenerationService.generateCode(any(CodeGenerationRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/codegen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedCode").value("public class Test { }"))
                .andExpect(jsonPath("$.language").value("Java"));
    }
}