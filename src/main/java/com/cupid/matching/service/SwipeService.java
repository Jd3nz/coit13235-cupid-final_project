package com.cupid.matching.service;

import com.cupid.matching.dto.SwipeRequest;
import com.cupid.matching.dto.SwipeResult;
import com.cupid.matching.exception.InvalidSwipeException;
import com.cupid.matching.model.CurrentSwipe;
import com.cupid.matching.model.Match;
import com.cupid.matching.model.MatchStatus;
import com.cupid.matching.model.SwipeDecision;
import com.cupid.matching.model.SwipeHistory;
import com.cupid.matching.model.User;
import com.cupid.matching.repository.CurrentSwipeRepository;
import com.cupid.matching.repository.MatchRepository;
import com.cupid.matching.repository.SwipeHistoryRepository;
import com.cupid.matching.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for recording swipes and creating matches.
 *
 * Supports:
 * FR_Swipe
 * FR_Swipe_History
 * FR_Match
 * NFR_Input_Sanitise
 * NFR_Traceability
 */
@Service
public class SwipeService {

    // Repositories separate current state, immutable audit history, and matches.
    private final UserRepository userRepository;
    private final CurrentSwipeRepository currentSwipeRepository;
    private final SwipeHistoryRepository swipeHistoryRepository;
    private final MatchRepository matchRepository;

    public SwipeService(
            UserRepository userRepository,
            CurrentSwipeRepository currentSwipeRepository,
            SwipeHistoryRepository swipeHistoryRepository,
            MatchRepository matchRepository
    ) {
        this.userRepository = userRepository;
        this.currentSwipeRepository = currentSwipeRepository;
        this.swipeHistoryRepository = swipeHistoryRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Records a swipe and creates a match when both users like each other.
     *
     * All database operations execute inside one transaction.
     */
    @Transactional
    public SwipeResult recordSwipe(SwipeRequest request) {
        validateRequest(request);

        User viewer = userRepository
                .findById(request.getViewerId())
                .filter(User::isActive)
                .orElseThrow(() ->
                        new InvalidSwipeException(
                                "The selected viewer does not exist or is inactive."
                        )
                );

        User target = userRepository
                .findById(request.getTargetUserId())
                .filter(User::isActive)
                .orElseThrow(() ->
                        new InvalidSwipeException(
                                "The target profile does not exist or is inactive."
                        )
                );

        /*
         * Lock the pair before changing swipe information.
         * The consistent ordering inside the SQL reduces deadlock risk.
         */
        userRepository.lockUserPair(
                viewer.getId(),
                target.getId()
        );

        // CurrentSwipe is the latest decision; it is updated instead of duplicated.
        CurrentSwipe currentSwipe = currentSwipeRepository
                .findBySwiperIdAndTargetUserId(
                        viewer.getId(),
                        target.getId()
                )
                .orElseGet(() -> new CurrentSwipe(
                        viewer,
                        target,
                        request.getDecision()
                ));
        currentSwipe.setDecision(request.getDecision());
        currentSwipeRepository.save(currentSwipe);

        // SwipeHistory is append-only evidence for FR_Swipe_History.
        swipeHistoryRepository.save(new SwipeHistory(
                viewer,
                target,
                request.getDecision()
        ));

        if (request.getDecision() != SwipeDecision.LIKE) {
            return SwipeResult.recorded();
        }

        boolean reciprocalLike = currentSwipeRepository.hasReciprocalLike(
                viewer.getId(),
                target.getId()
        );

        if (!reciprocalLike) {
            return SwipeResult.recorded();
        }

        // Canonical ordering makes the database uniqueness rule direction-independent.
        User userOne = viewer.getId() < target.getId() ? viewer : target;
        User userTwo = viewer.getId() < target.getId() ? target : viewer;

        Match match = matchRepository.findByUserOneIdAndUserTwoId(
                        userOne.getId(),
                        userTwo.getId()
                )
                .orElseGet(() -> new Match(
                        userOne,
                        userTwo,
                        MatchStatus.ACTIVE
                ));
        match.setStatus(MatchStatus.ACTIVE);
        matchRepository.save(match);

        return SwipeResult.matchCreated();
    }

    private void validateRequest(SwipeRequest request) {
        // This deliberately repeats browser validation to protect direct HTTP calls.
        if (request == null) {
            throw new InvalidSwipeException(
                    "The swipe request cannot be empty."
            );
        }

        if (request.getViewerId() == null
                || request.getViewerId() <= 0) {
            throw new InvalidSwipeException(
                    "The viewer ID is invalid."
            );
        }

        if (request.getTargetUserId() == null
                || request.getTargetUserId() <= 0) {
            throw new InvalidSwipeException(
                    "The target user ID is invalid."
            );
        }

        if (request.getDecision() == null) {
            throw new InvalidSwipeException(
                    "A swipe decision is required."
            );
        }

        if (request.getViewerId()
                .equals(request.getTargetUserId())) {
            throw new InvalidSwipeException(
                    "A user cannot swipe on their own profile."
            );
        }
    }
}
