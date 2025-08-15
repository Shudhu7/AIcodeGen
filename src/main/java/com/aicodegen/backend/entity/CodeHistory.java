package com.aicodegen.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_prompt", nullable = false, length = 1000)
    private String userPrompt;
    
    @Column(name = "programming_language", nullable = false, length = 50)
    private String programmingLanguage;
    
    @Column(name = "generated_code", columnDefinition = "TEXT")
    private String generatedCode;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "success", nullable = false)
    private Boolean success = true;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
}