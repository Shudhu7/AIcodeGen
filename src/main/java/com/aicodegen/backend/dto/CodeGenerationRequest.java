package com.aicodegen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for code generation
 */
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