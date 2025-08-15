package com.aicodegen.backend.repository;

import com.aicodegen.backend.entity.CodeHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeHistoryRepository extends JpaRepository<CodeHistory, Long> {
    
    /**
     * Find code history by programming language, ordered by creation date
     */
    List<CodeHistory> findByProgrammingLanguageOrderByCreatedAtDesc(String programmingLanguage);
    
    /**
     * Search code history by keyword in user prompt
     */
    @Query("SELECT ch FROM CodeHistory ch WHERE LOWER(ch.userPrompt) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY ch.createdAt DESC")
    List<CodeHistory> findByPromptContaining(@Param("keyword") String keyword);
    
    /**
     * Find recent code history since a specific date
     */
    @Query("SELECT ch FROM CodeHistory ch WHERE ch.createdAt >= :since ORDER BY ch.createdAt DESC")
    List<CodeHistory> findRecentHistory(@Param("since") LocalDateTime since);
    
    /**
     * Find top N recent entries with pagination support (RECOMMENDED METHOD)
     */
    @Query("SELECT ch FROM CodeHistory ch ORDER BY ch.createdAt DESC")
    List<CodeHistory> findRecentHistory(Pageable pageable);
    
    /**
     * Find recent entries with limit using Spring Data method naming (ALTERNATIVE 1)
     */
    List<CodeHistory> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * Find recent entries with limit using Spring Data method naming with dynamic limit (ALTERNATIVE 2)
     */
    List<CodeHistory> findTopByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Count successful code generations
     */
    @Query("SELECT COUNT(ch) FROM CodeHistory ch WHERE ch.success = true")
    Long countSuccessfulGenerations();
    
    /**
     * Count failed code generations
     */
    @Query("SELECT COUNT(ch) FROM CodeHistory ch WHERE ch.success = false")
    Long countFailedGenerations();
    
    /**
     * Get average execution time for successful generations
     */
    @Query("SELECT AVG(ch.executionTimeMs) FROM CodeHistory ch WHERE ch.success = true AND ch.executionTimeMs IS NOT NULL")
    Double getAverageExecutionTime();
    
    /**
     * Find code history by language and success status
     */
    List<CodeHistory> findByProgrammingLanguageAndSuccessOrderByCreatedAtDesc(String language, Boolean success);
    
    /**
     * Get statistics by programming language
     */
    @Query("SELECT ch.programmingLanguage, COUNT(ch), AVG(ch.executionTimeMs) " +
           "FROM CodeHistory ch WHERE ch.success = true " +
           "GROUP BY ch.programmingLanguage ORDER BY COUNT(ch) DESC")
    List<Object[]> getLanguageStatistics();
}