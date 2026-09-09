package com.cupid.matching.integration;

import com.cupid.matching.dto.SwipeRequest;
import com.cupid.matching.dto.SwipeResult;
import com.cupid.matching.model.SwipeDecision;
import com.cupid.matching.model.SwipeHistory;
import com.cupid.matching.repository.SwipeHistoryRepository;
import com.cupid.matching.service.SwipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Integration tests using a real MySQL database.
 *
 * Requirements:
 * FR_Swipe
 * FR_Swipe_History
 * FR_Match
 * NFR_Persistence_Timeout
 * NFR_Traceability
 */
@SpringBootTest
@ActiveProfiles("test")
class SwipePersistenceIntegrationTest {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

        @MockitoSpyBean
        private SwipeHistoryRepository swipeHistoryRepository;

    private Long alexId;
    private Long jordanId;

    @BeforeEach
    void setUp() {
                jdbcTemplate.execute("TRUNCATE TABLE matches");
                jdbcTemplate.execute("TRUNCATE TABLE swipe_history");
                jdbcTemplate.execute("TRUNCATE TABLE current_swipes");

        alexId = findUserId("Alex");
        jordanId = findUserId("Jordan");
    }

    /**
     * FR_Swipe and FR_Swipe_History:
     * A Like must be persisted in both swipe tables.
     */
    @Test
    void shouldPersistCurrentSwipeAndHistory() {
        SwipeResult result = swipeService.recordSwipe(
                new SwipeRequest(
                        alexId,
                        jordanId,
                        SwipeDecision.LIKE
                )
        );

        assertFalse(result.matched());

        Long currentSwipeCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM current_swipes
                WHERE swiper_id = ?
                  AND target_user_id = ?
                """,
                Long.class,
                alexId,
                jordanId
        );

        Long historyCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM swipe_history
                WHERE swiper_id = ?
                  AND target_user_id = ?
                """,
                Long.class,
                alexId,
                jordanId
        );

        String decision = jdbcTemplate.queryForObject(
                """
                SELECT decision
                FROM current_swipes
                WHERE swiper_id = ?
                  AND target_user_id = ?
                """,
                String.class,
                alexId,
                jordanId
        );

        assertEquals(1L, currentSwipeCount);
        assertEquals(1L, historyCount);
        assertEquals("LIKE", decision);
    }

    /**
     * FR_Match:
     * Two reciprocal Likes must create one active match.
     */
    @Test
    void shouldCreateMatchForMutualLikes() {
        SwipeResult firstSwipe = swipeService.recordSwipe(
                new SwipeRequest(
                        alexId,
                        jordanId,
                        SwipeDecision.LIKE
                )
        );

        SwipeResult secondSwipe = swipeService.recordSwipe(
                new SwipeRequest(
                        jordanId,
                        alexId,
                        SwipeDecision.LIKE
                )
        );

        assertFalse(firstSwipe.matched());
        assertTrue(secondSwipe.matched());

        Long matchCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM matches
                WHERE user_one_id = ?
                  AND user_two_id = ?
                  AND status = 'ACTIVE'
                """,
                Long.class,
                Math.min(alexId, jordanId),
                Math.max(alexId, jordanId)
        );

        assertEquals(1L, matchCount);
    }

    /**
     * FR_Match:
     * Repeated reciprocal Likes must not create duplicate matches.
     */
    @Test
    void shouldPreventDuplicateMatches() {
        swipeService.recordSwipe(
                new SwipeRequest(
                        alexId,
                        jordanId,
                        SwipeDecision.LIKE
                )
        );

        swipeService.recordSwipe(
                new SwipeRequest(
                        jordanId,
                        alexId,
                        SwipeDecision.LIKE
                )
        );

        SwipeResult repeatedSwipe = swipeService.recordSwipe(
                new SwipeRequest(
                        alexId,
                        jordanId,
                        SwipeDecision.LIKE
                )
        );

        assertTrue(repeatedSwipe.matched());

        Long matchCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM matches
                WHERE user_one_id = ?
                  AND user_two_id = ?
                """,
                Long.class,
                Math.min(alexId, jordanId),
                Math.max(alexId, jordanId)
        );

        assertEquals(1L, matchCount);
    }

    /**
     * Reliability test:
     * If swipe-history persistence fails, current_swipes must also
     * be rolled back because both writes belong to one transaction.
     */
    @Test
    void shouldRollbackCurrentSwipeWhenHistoryInsertFails() {
        doThrow(new DataAccessException("Forced swipe history failure") {})
                .when(swipeHistoryRepository)
                .save(any(SwipeHistory.class));

        assertThrows(
                DataAccessException.class,
                () -> swipeService.recordSwipe(
                        new SwipeRequest(
                                alexId,
                                jordanId,
                                SwipeDecision.LIKE
                        )
                )
        );

        Long currentSwipeCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM current_swipes
                WHERE swiper_id = ?
                  AND target_user_id = ?
                """,
                Long.class,
                alexId,
                jordanId
        );

        Long historyCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM swipe_history
                WHERE swiper_id = ?
                  AND target_user_id = ?
                """,
                Long.class,
                alexId,
                jordanId
        );

        assertEquals(0L, currentSwipeCount);
        assertEquals(0L, historyCount);
    }

    private Long findUserId(String displayName) {
        Long userId = jdbcTemplate.queryForObject(
                """
                SELECT id
                FROM users
                WHERE display_name = ?
                """,
                Long.class,
                displayName
        );

        return Objects.requireNonNull(userId);
    }

}