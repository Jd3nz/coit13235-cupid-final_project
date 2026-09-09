package com.cupid.matching.service;

import com.cupid.matching.dto.SwipeHistorySummary;
import com.cupid.matching.model.SwipeHistory;
import com.cupid.matching.model.User;
import com.cupid.matching.repository.SwipeHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for retrieving swipe history.
 *
 * Supports:
 * FR_Swipe_History
 * FR_Web_UI
 */
@Service
public class SwipeHistoryService {

        private final SwipeHistoryRepository swipeHistoryRepository;

    public SwipeHistoryService(
            SwipeHistoryRepository swipeHistoryRepository
    ) {
        this.swipeHistoryRepository = swipeHistoryRepository;
    }

    public List<SwipeHistorySummary> findHistory(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }

        return swipeHistoryRepository.findBySwiperIdOrderBySwipedAtDesc(userId)
            .stream()
                .map(this::toSummary)
                .toList();
    }

    private SwipeHistorySummary toSummary(SwipeHistory history) {
        User targetUser = history.getTargetUser();

        return new SwipeHistorySummary(
                history.getId(),
                targetUser.getId(),
                targetUser.getDisplayName(),
                targetUser.getAge(),
                targetUser.getBio(),
                history.getDecision(),
                history.getSwipedAt()
        );
    }
}