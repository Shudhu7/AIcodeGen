package com.aicodegen.backend.repository;

import com.aicodegen.backend.entity.CodeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeHistoryRepository extends JpaRepository<CodeHistory, Long> {
    
    List<CodeHistory> findByProgrammingLanguageOrderByCreatedAtDesc(String programmingLanguage);
    
    @Query("SELECT ch FROM CodeHistory ch WHERE ch.userPrompt LIKE %:keyword% ORDER BY ch.createdAt DESC")
    List<CodeHistory> findByPromptContaining(@Param("keyword") String keyword);
    
    @Query("SELECT ch FROM CodeHistory ch WHERE ch.createdAt >= :since ORDER BY ch.createdAt DESC")
    List<CodeHistory> findRecentHistory(@Param("since") LocalDateTime since);
    
    @Query("SELECT ch FROM CodeHistory ch ORDER BY ch.createdAt DESC LIMIT 10")
    List<CodeHistory> findTop10ByOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(ch) FROM CodeHistory ch WHERE ch.success = true")
    Long countSuccessfulGenerations();
}