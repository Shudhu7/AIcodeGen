package com.aicodegen.backend.repository;

import com.aicodegen.backend.entity.CodeHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CodeHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CodeHistoryRepository repository;

    @Test
    void testFindByProgrammingLanguage() {
        // Given
        CodeHistory history1 = createTestHistory("Java", "Test prompt 1");
        CodeHistory history2 = createTestHistory("Python", "Test prompt 2");
        CodeHistory history3 = createTestHistory("Java", "Test prompt 3");

        entityManager.persistAndFlush(history1);
        entityManager.persistAndFlush(history2);
        entityManager.persistAndFlush(history3);

        // When
        List<CodeHistory> javaHistory = repository
                .findByProgrammingLanguageOrderByCreatedAtDesc("Java");

        // Then
        assertEquals(2, javaHistory.size());
        assertTrue(javaHistory.stream()
                .allMatch(h -> "Java".equals(h.getProgrammingLanguage())));
    }

    @Test
    void testFindRecentHistory() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime today = LocalDateTime.now();

        CodeHistory oldHistory = createTestHistory("Java", "Old prompt");
        oldHistory.setCreatedAt(yesterday.minusHours(2));

        CodeHistory recentHistory = createTestHistory("Python", "Recent prompt");
        recentHistory.setCreatedAt(today.minusHours(1));

        entityManager.persistAndFlush(oldHistory);
        entityManager.persistAndFlush(recentHistory);

        // When
        List<CodeHistory> recent = repository.findRecentHistory(yesterday);

        // Then
        assertEquals(1, recent.size());
        assertEquals("Recent prompt", recent.get(0).getUserPrompt());
    }

    @Test
    void testCountSuccessfulGenerations() {
        // Given
        CodeHistory success1 = createTestHistory("Java", "Success 1");
        success1.setSuccess(true);

        CodeHistory success2 = createTestHistory("Python", "Success 2");
        success2.setSuccess(true);

        CodeHistory failure = createTestHistory("JavaScript", "Failure");
        failure.setSuccess(false);

        entityManager.persistAndFlush(success1);
        entityManager.persistAndFlush(success2);
        entityManager.persistAndFlush(failure);

        // When
        Long count = repository.countSuccessfulGenerations();

        // Then
        assertEquals(2L, count);
    }

    private CodeHistory createTestHistory(String language, String prompt) {
        CodeHistory history = new CodeHistory();
        history.setProgrammingLanguage(language);
        history.setUserPrompt(prompt);
        history.setGeneratedCode("// Generated code");
        history.setSuccess(true);
        history.setExecutionTimeMs(1000L);
        return history;
    }
}