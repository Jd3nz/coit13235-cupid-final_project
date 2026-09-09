package com.cupid.matching.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the five-second persistence timeout.
 *
 * Supports:
 * NFR_Persistence_Timeout
 * Reliability
 * NFR_Traceability
 */
@SpringBootTest
@ActiveProfiles("test")
class PersistenceTimeoutIntegrationTest {

        @Autowired
        private EntityManager entityManager;

    /**
        * MySQL is asked to sleep for ten seconds.
     *
     * JPA must cancel the statement after approximately five seconds
     * instead of waiting for the full ten seconds.
     */
    @Test
    @Transactional
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void shouldCancelQueryThatExceedsFiveSeconds() {
        long startedAt = System.nanoTime();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> entityManager.createNativeQuery(
                        "SELECT SLEEP(10)"
                ).getResultList()
        );

        long elapsedMilliseconds =
                TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - startedAt
                );

        assertTrue(
                containsTimeoutOrCancellation(exception),
                "The database operation should fail because "
                        + "the statement was cancelled."
        );

        assertTrue(
                elapsedMilliseconds < 7_000,
                "The query should fail near the configured "
                        + "five-second timeout. Actual time: "
                        + elapsedMilliseconds
                        + " ms"
        );
    }

        private boolean containsTimeoutOrCancellation(Throwable exception) {
                Throwable current = exception;
                while (current != null) {
                        String message = current.getMessage();
                        if (message != null) {
                                String lowerCaseMessage = message.toLowerCase();
                                if (lowerCaseMessage.contains("cancel")
                                                || lowerCaseMessage.contains("timeout")) {
                                        return true;
                                }
                        }
                        current = current.getCause();
                }
                return false;
        }
}
