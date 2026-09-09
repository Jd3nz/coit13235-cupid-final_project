package com.cupid.matching.dto;

import com.cupid.matching.model.SwipeDecision;

import java.time.OffsetDateTime;

/**
 * Information displayed for one swipe-history record.
 *
 * Supports:
 * FR_Swipe_History
 * FR_Web_UI
 */
public record SwipeHistorySummary(
        Long historyId,
        Long targetUserId,
        String displayName,
        int age,
        String bio,
        SwipeDecision decision,
        OffsetDateTime swipedAt
) {
}