package com.cupid.matching.service;

import com.cupid.matching.dto.SwipeRequest;
import com.cupid.matching.dto.SwipeResult;
import com.cupid.matching.exception.InvalidSwipeException;
import com.cupid.matching.model.CurrentSwipe;
import com.cupid.matching.model.Match;
import com.cupid.matching.model.SwipeDecision;
import com.cupid.matching.model.SwipeHistory;
import com.cupid.matching.model.User;
import com.cupid.matching.repository.CurrentSwipeRepository;
import com.cupid.matching.repository.MatchRepository;
import com.cupid.matching.repository.SwipeHistoryRepository;
import com.cupid.matching.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the matching business logic.
 *
 * Requirements:
 * FR_Swipe
 * FR_Swipe_History
 * FR_Match
 * NFR_Input_Sanitise
 * NFR_Traceability
 */
@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
        private CurrentSwipeRepository currentSwipeRepository;

    @Mock
        private SwipeHistoryRepository swipeHistoryRepository;

        @Mock
        private MatchRepository matchRepository;

    private SwipeService swipeService;

    private User alex;
    private User jordan;

    @BeforeEach
    void setUp() {
        swipeService = new SwipeService(
                userRepository,
                currentSwipeRepository,
                swipeHistoryRepository,
                matchRepository
        );

        alex = createUser(
                1L,
                "Alex",
                24
        );

        jordan = createUser(
                2L,
                "Jordan",
                23
        );
    }

    /**
     * FR_Swipe and FR_Swipe_History:
     * A valid Like must be stored in both current swipe
     * and swipe history.
     */
    @Test
    void frSwipe_shouldStoreLikeAndHistory() {
        SwipeRequest request = new SwipeRequest(
                alex.getId(),
                jordan.getId(),
                SwipeDecision.LIKE
        );

        mockActiveUsers();
        when(currentSwipeRepository.hasReciprocalLike(
                alex.getId(),
                jordan.getId()
        )).thenReturn(false);

        SwipeResult result = swipeService.recordSwipe(request);

        assertFalse(result.matched());

        verify(userRepository).lockUserPair(
                alex.getId(),
                jordan.getId()
        );

        verify(currentSwipeRepository).save(any(CurrentSwipe.class));
        verify(swipeHistoryRepository).save(any(SwipeHistory.class));

        verify(matchRepository, never()).save(any(Match.class));
    }

    /**
     * FR_Match:
     * Reciprocal Likes must create a match.
     */
    @Test
    void frMatch_shouldCreateMatchForReciprocalLikes() {
        SwipeRequest request = new SwipeRequest(
                alex.getId(),
                jordan.getId(),
                SwipeDecision.LIKE
        );

        mockActiveUsers();

        when(currentSwipeRepository.hasReciprocalLike(
                alex.getId(),
                jordan.getId()
        )).thenReturn(true);

        SwipeResult result = swipeService.recordSwipe(request);

        assertTrue(result.matched());

        verify(matchRepository).save(any(Match.class));
    }

    /**
     * FR_Match:
     * A Dislike must not perform reciprocal Like detection
     * or create a match.
     */
    @Test
    void frMatch_shouldNotCreateMatchForDislike() {
        SwipeRequest request = new SwipeRequest(
                alex.getId(),
                jordan.getId(),
                SwipeDecision.DISLIKE
        );

        mockActiveUsers();

        SwipeResult result = swipeService.recordSwipe(request);

        assertFalse(result.matched());

        verify(currentSwipeRepository).save(any(CurrentSwipe.class));
        verify(swipeHistoryRepository).save(any(SwipeHistory.class));

        verify(currentSwipeRepository, never()).hasReciprocalLike(
                alex.getId(),
                jordan.getId()
        );

        verify(matchRepository, never()).save(any(Match.class));
    }

    /**
     * NFR_Input_Sanitise:
     * A user must not swipe on their own profile.
     */
    @Test
    void inputValidation_shouldRejectSelfSwipe() {
        SwipeRequest request = new SwipeRequest(
                alex.getId(),
                alex.getId(),
                SwipeDecision.LIKE
        );

        InvalidSwipeException exception = assertThrows(
                InvalidSwipeException.class,
                () -> swipeService.recordSwipe(request)
        );

        assertTrue(
                exception.getMessage()
                        .contains("cannot swipe on their own profile")
        );

        verify(currentSwipeRepository, never()).save(any(CurrentSwipe.class));
    }

    /**
     * NFR_Input_Sanitise:
     * A decision must be supplied.
     */
    @Test
    void inputValidation_shouldRejectMissingDecision() {
        SwipeRequest request = new SwipeRequest(
                alex.getId(),
                jordan.getId(),
                null
        );

        InvalidSwipeException exception = assertThrows(
                InvalidSwipeException.class,
                () -> swipeService.recordSwipe(request)
        );

        assertTrue(
                exception.getMessage()
                        .contains("decision is required")
        );

        verify(swipeHistoryRepository, never()).save(any(SwipeHistory.class));
    }

    /**
     * FR_Swipe:
     * An inactive or missing viewer must be rejected.
     */
    @Test
    void frSwipe_shouldRejectMissingViewer() {
        SwipeRequest request = new SwipeRequest(
                999L,
                jordan.getId(),
                SwipeDecision.LIKE
        );

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        InvalidSwipeException exception = assertThrows(
                InvalidSwipeException.class,
                () -> swipeService.recordSwipe(request)
        );

        assertTrue(
                exception.getMessage()
                        .contains("viewer does not exist")
        );

        verify(currentSwipeRepository, never()).save(any(CurrentSwipe.class));
    }

    /**
     * FR_Swipe:
     * An inactive or missing target must be rejected.
     */
    @Test
    void frSwipe_shouldRejectMissingTargetProfile() {
        SwipeRequest request = new SwipeRequest(
                alex.getId(),
                999L,
                SwipeDecision.LIKE
        );

        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        InvalidSwipeException exception = assertThrows(
                InvalidSwipeException.class,
                () -> swipeService.recordSwipe(request)
        );

        assertTrue(
                exception.getMessage()
                        .contains("target profile does not exist")
        );

        verify(swipeHistoryRepository, never()).save(any(SwipeHistory.class));
    }

    private void mockActiveUsers() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        when(userRepository.findById(jordan.getId()))
                .thenReturn(Optional.of(jordan));
    }

    private User createUser(
            Long id,
            String displayName,
            int age
    ) {
        return new User(
                id,
                displayName,
                age,
                "Test biography",
                true,
                OffsetDateTime.now()
        );
    }
}